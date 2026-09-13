package com.filetoss.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.ui.components.AppMode
import com.filetoss.ui.components.BauhausButton
import com.filetoss.ui.components.BauhausProfileIcon
import com.filetoss.ui.components.DropZoneComponent
import com.filetoss.ui.components.MechanicalProgressBar
import com.filetoss.ui.components.ModeToggle
import com.filetoss.ui.components.ProfileSidebar
import com.filetoss.ui.components.RemoteBrowserComponent
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens
import com.filetoss.ui.viewmodel.ConnectionStatus
import com.filetoss.ui.viewmodel.MainViewModel
import com.filetoss.ui.viewmodel.TransferStatus

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BauhausColors.Chalk)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. 上部ヘッダー
            HeaderBar(
                isSidebarOpen = state.isSidebarOpen,
                currentMode = state.mode,
                connectionStatus = state.connectionStatus,
                onToggleSidebar = { viewModel.toggleSidebar() },
                onModeChange = { viewModel.setMode(it) }
            )

            // 2. 現在の宛先表示バー
            TargetBanner(
                activeProfile = state.activeProfile,
                currentMode = state.mode
            )

            // 3. メイン作業エリア (TOSS または CATCH)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.mode) {
                    AppMode.TOSS -> {
                        DropZoneComponent(
                            isDraggingOver = state.isDraggingOver,
                            isTransferring = state.transferStatus == TransferStatus.TRANSFERRING,
                            isCompleted = state.transferStatus == TransferStatus.COMPLETED,
                            progress = state.transferProgress,
                            onFilesSelected = { viewModel.tossFiles(it) }
                        )
                    }
                    AppMode.CATCH -> {
                        RemoteBrowserComponent(
                            currentRemotePath = state.currentRemotePath,
                            files = state.remoteFiles,
                            isLoading = state.isLoadingRemoteFiles,
                            onRefresh = { viewModel.refreshRemoteFiles() },
                            onFileCatch = { viewModel.catchFile(it) },
                            onDirectoryNavigate = { viewModel.navigateRemotePath(it) }
                        )
                    }
                }
            }

            // 4. 下部メカニカル進捗ステータスバー
            MechanicalProgressBar(
                progress = state.transferProgress,
                isTransferring = state.transferStatus == TransferStatus.TRANSFERRING,
                statusText = state.statusMessage
            )
        }

        // 5. プロファイル・スライドインサイドバー
        ProfileSidebar(
            isOpen = state.isSidebarOpen,
            profiles = state.profiles,
            activeProfileId = state.activeProfile?.id,
            onSelectProfile = { viewModel.selectProfile(it) },
            onSaveProfile = { profile, credentials -> viewModel.saveProfile(profile, credentials) },
            onDeleteProfile = { viewModel.deleteProfile(it) },
            onTestConnection = { profile, credentials -> viewModel.testConnection(profile, credentials) },
            testConnectionResult = state.testConnectionResult,
            isTestingConnection = state.isTestingConnection,
            onClose = { viewModel.closeSidebar() },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun HeaderBar(
    isSidebarOpen: Boolean,
    currentMode: AppMode,
    connectionStatus: ConnectionStatus,
    onToggleSidebar: () -> Unit,
    onModeChange: (AppMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BauhausColors.Chalk)
            .border(
                width = BauhausDimens.BorderWidth,
                color = BauhausColors.FrameBlack,
                shape = RectangleShape
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [≡ PROFILES]
        BauhausButton(
            onClick = onToggleSidebar,
            backgroundColor = if (isSidebarOpen) BauhausColors.CrimsonRed else BauhausColors.PaperWhite,
            contentColor = if (isSidebarOpen) BauhausColors.PaperWhite else BauhausColors.FrameBlack
        ) {
            Text(
                text = "≡ PROFILES",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                color = if (isSidebarOpen) BauhausColors.PaperWhite else BauhausColors.FrameBlack
            )
        }

        // [ T O S S ] [ C A T C H ] モード切替
        ModeToggle(
            currentMode = currentMode,
            onModeChanged = onModeChange
        )

        // 接続状態インジケーター
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(BauhausColors.PaperWhite)
                .border(2.dp, BauhausColors.FrameBlack, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val (indicatorColor, statusLabel) = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> BauhausColors.CobaltBlue to "ONLINE"
                ConnectionStatus.CONNECTING -> BauhausColors.CadmiumYellow to "CONNECTING"
                ConnectionStatus.DISCONNECTED -> BauhausColors.MutedText to "OFFLINE"
                ConnectionStatus.ERROR -> BauhausColors.CrimsonRed to "ERR"
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(indicatorColor, CircleShape)
                    .border(1.dp, BauhausColors.FrameBlack, CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = statusLabel,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = BauhausColors.FrameBlack
            )
        }
    }
}

@Composable
private fun TargetBanner(
    activeProfile: com.filetoss.domain.model.TransferProfile?,
    currentMode: AppMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BauhausColors.PaperWhite)
            .border(
                width = 1.5.dp,
                color = BauhausColors.FrameBlack,
                shape = RectangleShape
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (currentMode == AppMode.TOSS) "TARGET:" else "SOURCE:",
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = BauhausColors.FrameBlack
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (activeProfile != null) {
            BauhausProfileIcon(colorTag = activeProfile.colorTag, size = 12.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "[ ${activeProfile.name} ]",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BauhausColors.FrameBlack
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "-> ${activeProfile.remoteDirectory}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = BauhausColors.MutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = "NO ACTIVE PROFILE SELECTED (CLICK PROFILES TO CONFIGURE)",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = BauhausColors.CrimsonRed
            )
        }
    }
}
