package com.filetoss.data.repository

import com.filetoss.domain.model.AuthType
import com.filetoss.domain.model.BauhausColor
import com.filetoss.domain.model.TransferProfile
import com.filetoss.domain.model.TransferProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import java.util.UUID

@Serializable
data class ProfilesData(
    val activeProfileId: String? = null,
    val profiles: List<TransferProfile> = emptyList()
)

interface ProfileRepository {
    suspend fun getProfiles(): List<TransferProfile>
    suspend fun getActiveProfile(): TransferProfile?
    suspend fun getActiveProfileId(): String?
    suspend fun setActiveProfileId(profileId: String?)
    suspend fun saveProfile(profile: TransferProfile)
    suspend fun deleteProfile(profileId: String)
}

class JsonProfileRepository(
    private val customStorageFile: File? = null,
    private val initDefault: Boolean = true
) : ProfileRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val mutex = Mutex()

    private val configFile: File by lazy {
        customStorageFile ?: resolveDefaultConfigFile()
    }

    private fun resolveDefaultConfigFile(): File {
        val appData = System.getenv("APPDATA")
        val baseDir = if (!appData.isNullOrBlank()) {
            Paths.get(appData, "file-toss").toFile()
        } else {
            val userHome = System.getProperty("user.home", ".")
            Paths.get(userHome, ".config", "file-toss").toFile()
        }
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return File(baseDir, "profiles.json")
    }

    override suspend fun getProfiles(): List<TransferProfile> = mutex.withLock {
        withContext(Dispatchers.IO) {
            loadData().profiles
        }
    }

    override suspend fun getActiveProfile(): TransferProfile? = mutex.withLock {
        withContext(Dispatchers.IO) {
            val data = loadData()
            data.profiles.firstOrNull { it.id == data.activeProfileId }
                ?: data.profiles.firstOrNull()
        }
    }

    override suspend fun getActiveProfileId(): String? = mutex.withLock {
        withContext(Dispatchers.IO) {
            loadData().activeProfileId
        }
    }

    override suspend fun setActiveProfileId(profileId: String?) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val current = loadData()
            val updated = current.copy(activeProfileId = profileId)
            saveData(updated)
        }
    }

    override suspend fun saveProfile(profile: TransferProfile) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val current = loadData()
            val index = current.profiles.indexOfFirst { it.id == profile.id }
            val updatedList = if (index >= 0) {
                current.profiles.toMutableList().apply { set(index, profile) }
            } else {
                current.profiles + profile
            }
            val newActiveId = current.activeProfileId ?: profile.id
            saveData(current.copy(activeProfileId = newActiveId, profiles = updatedList))
        }
    }

    override suspend fun deleteProfile(profileId: String) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val current = loadData()
            val updatedList = current.profiles.filterNot { it.id == profileId }
            val updatedActiveId = if (current.activeProfileId == profileId) {
                updatedList.firstOrNull()?.id
            } else {
                current.activeProfileId
            }
            saveData(current.copy(activeProfileId = updatedActiveId, profiles = updatedList))
        }
    }

    private fun loadData(): ProfilesData {
        if (!configFile.exists()) {
            val initial = if (initDefault) createDefaultData() else ProfilesData()
            saveData(initial)
            return initial
        }
        return try {
            val content = configFile.readText(Charsets.UTF_8)
            json.decodeFromString<ProfilesData>(content)
        } catch (e: Exception) {
            if (initDefault) createDefaultData() else ProfilesData()
        }
    }

    private fun saveData(data: ProfilesData) {
        try {
            configFile.parentFile?.mkdirs()
            configFile.writeText(json.encodeToString(data), Charsets.UTF_8)
        } catch (_: Exception) {
        }
    }

    private fun createDefaultData(): ProfilesData {
        val sampleProfile = TransferProfile(
            id = UUID.randomUUID().toString(),
            name = "Production-Web",
            protocol = TransferProtocol.SFTP,
            host = "192.168.1.100",
            port = 22,
            username = "deploy",
            authType = AuthType.PASSWORD,
            remoteDirectory = "/var/www/html/assets",
            localDirectory = System.getProperty("user.home", "") + File.separator + "Downloads",
            colorTag = BauhausColor.RED
        )
        return ProfilesData(
            activeProfileId = sampleProfile.id,
            profiles = listOf(sampleProfile)
        )
    }
}
