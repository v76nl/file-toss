package com.filetoss.ui.viewmodel

import com.filetoss.data.repository.ProfileRepository
import com.filetoss.data.security.CredentialStore
import com.filetoss.domain.client.TransferClient
import com.filetoss.domain.model.AuthCredentials
import com.filetoss.domain.model.RemoteFileInfo
import com.filetoss.domain.model.TransferProfile
import com.filetoss.domain.model.TransferProgress
import com.filetoss.domain.model.TransferResult
import com.filetoss.domain.usecase.CatchFilesUseCase
import com.filetoss.domain.usecase.ListRemoteFilesUseCase
import com.filetoss.domain.usecase.TossFilesUseCase
import com.filetoss.ui.components.AppMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Paths

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class TransferStatus {
    IDLE,
    TRANSFERRING,
    COMPLETED,
    FAILED
}

data class MainUiState(
    val mode: AppMode = AppMode.TOSS,
    val profiles: List<TransferProfile> = emptyList(),
    val activeProfile: TransferProfile? = null,
    val isSidebarOpen: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val transferStatus: TransferStatus = TransferStatus.IDLE,
    val transferProgress: TransferProgress? = null,
    val statusMessage: String? = null,
    val remoteFiles: List<RemoteFileInfo> = emptyList(),
    val currentRemotePath: String = "",
    val isLoadingRemoteFiles: Boolean = false,
    val isDraggingOver: Boolean = false,
    val testConnectionResult: Boolean? = null,
    val isTestingConnection: Boolean = false
)

class MainViewModel(
    private val profileRepository: ProfileRepository,
    private val credentialStore: CredentialStore,
    private val transferClient: TransferClient
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val tossFilesUseCase = TossFilesUseCase(transferClient)
    private val catchFilesUseCase = CatchFilesUseCase(transferClient)
    private val listRemoteFilesUseCase = ListRemoteFilesUseCase(transferClient)

    private var connectionJob: Job? = null

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            val profiles = profileRepository.getProfiles()
            val active = profileRepository.getActiveProfile()
            _uiState.update {
                it.copy(
                    profiles = profiles,
                    activeProfile = active,
                    currentRemotePath = active?.remoteDirectory ?: ""
                )
            }
            if (active != null) {
                ensureConnected(active)
            }
        }
    }

    fun setMode(mode: AppMode) {
        _uiState.update { it.copy(mode = mode) }
        if (mode == AppMode.CATCH) {
            refreshRemoteFiles()
        }
    }

    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarOpen = !it.isSidebarOpen) }
    }

    fun closeSidebar() {
        _uiState.update { it.copy(isSidebarOpen = false) }
    }

    fun setDraggingOver(isDragging: Boolean) {
        _uiState.update { it.copy(isDraggingOver = isDragging) }
    }

    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.setActiveProfileId(profileId)
            val active = profileRepository.getActiveProfile()
            _uiState.update {
                it.copy(
                    activeProfile = active,
                    currentRemotePath = active?.remoteDirectory ?: "",
                    isSidebarOpen = false
                )
            }
            if (active != null) {
                ensureConnected(active)
            }
        }
    }

    fun saveProfile(profile: TransferProfile, credentials: AuthCredentials) {
        viewModelScope.launch {
            val encryptedPassword = if (!credentials.password.isNullOrEmpty()) {
                credentialStore.encrypt(credentials.password)
            } else {
                profile.encryptedPassword
            }

            val encryptedPassphrase = if (!credentials.passphrase.isNullOrEmpty()) {
                credentialStore.encrypt(credentials.passphrase)
            } else {
                profile.encryptedPassphrase
            }

            val updatedProfile = profile.copy(
                encryptedPassword = encryptedPassword,
                encryptedPassphrase = encryptedPassphrase
            )

            profileRepository.saveProfile(updatedProfile)
            val profiles = profileRepository.getProfiles()
            val active = profileRepository.getActiveProfile()

            _uiState.update {
                it.copy(
                    profiles = profiles,
                    activeProfile = active,
                    currentRemotePath = active?.remoteDirectory ?: it.currentRemotePath
                )
            }

            if (active?.id == updatedProfile.id) {
                ensureConnected(updatedProfile, credentials)
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
            val profiles = profileRepository.getProfiles()
            val active = profileRepository.getActiveProfile()
            _uiState.update {
                it.copy(
                    profiles = profiles,
                    activeProfile = active,
                    currentRemotePath = active?.remoteDirectory ?: ""
                )
            }
            if (active != null) {
                ensureConnected(active)
            }
        }
    }

    fun testConnection(profile: TransferProfile, credentials: AuthCredentials) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, testConnectionResult = null) }
            val creds = resolveCredentials(profile, credentials)
            val result = transferClient.testConnection(profile, creds)
            _uiState.update { it.copy(isTestingConnection = false, testConnectionResult = result) }
        }
    }

    fun getDecryptedCredentials(profile: TransferProfile): AuthCredentials {
        return resolveCredentials(profile)
    }

    private fun resolveCredentials(profile: TransferProfile, inputCreds: AuthCredentials? = null): AuthCredentials {
        val pwd = inputCreds?.password?.takeIf { it.isNotEmpty() }
            ?: profile.encryptedPassword?.let { credentialStore.decrypt(it) }

        val passphrase = inputCreds?.passphrase?.takeIf { it.isNotEmpty() }
            ?: profile.encryptedPassphrase?.let { credentialStore.decrypt(it) }

        return AuthCredentials(password = pwd, passphrase = passphrase)
    }

    private fun ensureConnected(profile: TransferProfile, explicitCreds: AuthCredentials? = null) {
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING) }
            try {
                val creds = resolveCredentials(profile, explicitCreds)
                transferClient.connect(profile, creds)
                _uiState.update {
                    it.copy(
                        connectionStatus = ConnectionStatus.CONNECTED,
                        statusMessage = "CONNECTED TO ${profile.host}"
                    )
                }
                if (_uiState.value.mode == AppMode.CATCH) {
                    refreshRemoteFiles()
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        connectionStatus = ConnectionStatus.ERROR,
                        statusMessage = "CONNECT ERROR: ${e.message}"
                    )
                }
            }
        }
    }

    fun tossFiles(files: List<File>) {
        val active = _uiState.value.activeProfile ?: return
        if (files.isEmpty()) return

        viewModelScope.launch {
            if (!transferClient.isConnected) {
                ensureConnected(active)
                delay(500)
            }

            _uiState.update {
                it.copy(
                    transferStatus = TransferStatus.TRANSFERRING,
                    transferProgress = null,
                    statusMessage = "STARTING TOSS..."
                )
            }

            val result = tossFilesUseCase(
                localFiles = files,
                remoteDirectory = active.remoteDirectory,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(
                            transferProgress = progress,
                            statusMessage = "TOSSING: ${progress.currentFileName}"
                        )
                    }
                }
            )

            when (result) {
                is TransferResult.Success -> {
                    _uiState.update {
                        it.copy(
                            transferStatus = TransferStatus.COMPLETED,
                            statusMessage = "TOSSED ${result.transferredFiles} FILES (${TransferProgress.formatBytes(result.totalBytes)})"
                        )
                    }
                    // 3秒後にIDLE復帰
                    delay(3000)
                    _uiState.update {
                        if (it.transferStatus == TransferStatus.COMPLETED) {
                            it.copy(transferStatus = TransferStatus.IDLE)
                        } else it
                    }
                }
                is TransferResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            transferStatus = TransferStatus.FAILED,
                            statusMessage = "TRANSFER FAILED: ${result.message}"
                        )
                    }
                }
            }
        }
    }

    fun catchFile(file: RemoteFileInfo) {
        val active = _uiState.value.activeProfile ?: return
        viewModelScope.launch {
            if (!transferClient.isConnected) {
                ensureConnected(active)
                delay(500)
            }

            _uiState.update {
                it.copy(
                    transferStatus = TransferStatus.TRANSFERRING,
                    transferProgress = null,
                    statusMessage = "CATCHING ${file.name}..."
                )
            }

            val localDir = Paths.get(active.localDirectory)
            val result = catchFilesUseCase(
                remoteFilePath = file.fullPath,
                localDirectory = localDir,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(
                            transferProgress = progress,
                            statusMessage = "CATCHING: ${progress.currentFileName}"
                        )
                    }
                }
            )

            when (result) {
                is TransferResult.Success -> {
                    _uiState.update {
                        it.copy(
                            transferStatus = TransferStatus.COMPLETED,
                            statusMessage = "CAUGHT ${file.name} -> ${active.localDirectory}"
                        )
                    }
                    delay(3000)
                    _uiState.update {
                        if (it.transferStatus == TransferStatus.COMPLETED) {
                            it.copy(transferStatus = TransferStatus.IDLE)
                        } else it
                    }
                }
                is TransferResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            transferStatus = TransferStatus.FAILED,
                            statusMessage = "CATCH FAILED: ${result.message}"
                        )
                    }
                }
            }
        }
    }

    fun refreshRemoteFiles() {
        val path = _uiState.value.currentRemotePath.ifBlank {
            _uiState.value.activeProfile?.remoteDirectory ?: ""
        }
        if (path.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRemoteFiles = true) }
            try {
                val files = listRemoteFilesUseCase(path)
                _uiState.update {
                    it.copy(
                        remoteFiles = files,
                        isLoadingRemoteFiles = false,
                        currentRemotePath = path
                    )
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoadingRemoteFiles = false,
                        statusMessage = "LIST ERROR: ${e.message}"
                    )
                }
            }
        }
    }

    fun navigateRemotePath(path: String) {
        _uiState.update { it.copy(currentRemotePath = path) }
        refreshRemoteFiles()
    }
}
