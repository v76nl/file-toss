package com.filetoss.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.domain.model.AuthCredentials
import com.filetoss.domain.model.AuthType
import com.filetoss.domain.model.BauhausColor
import com.filetoss.domain.model.TransferProfile
import com.filetoss.domain.model.TransferProtocol
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens
import java.io.File
import java.util.UUID
import javax.swing.JFileChooser

@Composable
fun ProfileSidebar(
    isOpen: Boolean,
    profiles: List<TransferProfile>,
    activeProfileId: String?,
    onSelectProfile: (String) -> Unit,
    onSaveProfile: (TransferProfile, AuthCredentials) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onTestConnection: (TransferProfile, AuthCredentials) -> Unit,
    testConnectionResult: Boolean?,
    isTestingConnection: Boolean,
    onGetCredentials: (TransferProfile) -> AuthCredentials,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<TransferProfile?>(null) }
    var inputPassword by remember { mutableStateOf("") }
    var inputPassphrase by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(420.dp)
                .background(BauhausColors.Chalk, RectangleShape)
                .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, RectangleShape)
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                // ヘッダー
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BauhausColors.FrameBlack)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "PROFILE: EDIT" else "PROFILES",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = BauhausColors.PaperWhite
                    )

                    BauhausButton(
                        onClick = {
                            if (isEditing) {
                                isEditing = false
                                editingProfile = null
                            } else {
                                onClose()
                            }
                        },
                        backgroundColor = BauhausColors.CrimsonRed,
                        contentColor = BauhausColors.PaperWhite,
                        borderWidth = 1.dp,
                        shadowOffset = 0.dp
                    ) {
                        Text(
                            text = "X",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = BauhausColors.PaperWhite
                        )
                    }
                }

                if (!isEditing) {
                    // プロファイル一覧表示
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        BauhausButton(
                            onClick = {
                                editingProfile = TransferProfile(
                                    id = UUID.randomUUID().toString(),
                                    name = "New Profile",
                                    protocol = TransferProtocol.SFTP,
                                    host = "localhost",
                                    port = 22,
                                    username = "user",
                                    authType = AuthType.PASSWORD,
                                    remoteDirectory = "/home/username",
                                    localDirectory = System.getProperty("user.home", "") + File.separator + "Downloads",
                                    colorTag = BauhausColor.RED
                                )
                                inputPassword = ""
                                inputPassphrase = ""
                                isEditing = true
                            },
                            backgroundColor = BauhausColors.CadmiumYellow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "+ ADD NEW PROFILE",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                color = BauhausColors.FrameBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(profiles) { profile ->
                                val isActive = profile.id == activeProfileId
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isActive) BauhausColors.PaperWhite else BauhausColors.Chalk,
                                            RectangleShape
                                        )
                                        .border(
                                            if (isActive) 3.dp else 1.5.dp,
                                            if (isActive) BauhausColors.CrimsonRed else BauhausColors.FrameBlack,
                                            RectangleShape
                                        )
                                        .clickable { onSelectProfile(profile.id) }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            BauhausProfileIcon(colorTag = profile.colorTag, size = 18.dp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = profile.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = BauhausColors.FrameBlack
                                                )
                                                Text(
                                                    text = "${profile.username}@${profile.host}:${profile.port}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = BauhausColors.MutedText
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            BauhausButton(
                                                onClick = {
                                                    editingProfile = profile
                                                    val creds = onGetCredentials(profile)
                                                    inputPassword = creds.password ?: ""
                                                    inputPassphrase = creds.passphrase ?: ""
                                                    isEditing = true
                                                },
                                                backgroundColor = BauhausColors.Chalk,
                                                borderWidth = 1.5.dp,
                                                shadowOffset = 1.dp
                                            ) {
                                                Text(
                                                    text = "EDIT",
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = BauhausColors.FrameBlack
                                                )
                                            }

                                            if (profiles.size > 1) {
                                                BauhausButton(
                                                    onClick = { onDeleteProfile(profile.id) },
                                                    backgroundColor = BauhausColors.CrimsonRed,
                                                    borderWidth = 1.5.dp,
                                                    shadowOffset = 1.dp
                                                ) {
                                                    Text(
                                                        text = "DEL",
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 10.sp,
                                                        color = BauhausColors.PaperWhite
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // プロファイル編集フォーム
                    editingProfile?.let { prof ->
                        ProfileEditForm(
                            profile = prof,
                            password = inputPassword,
                            passphrase = inputPassphrase,
                            onPasswordChange = { inputPassword = it },
                            onPassphraseChange = { inputPassphrase = it },
                            isTesting = isTestingConnection,
                            testResult = testConnectionResult,
                            onTest = { updated ->
                                onTestConnection(updated, AuthCredentials(inputPassword, inputPassphrase))
                            },
                            onSave = { updated ->
                                onSaveProfile(updated, AuthCredentials(inputPassword, inputPassphrase))
                                isEditing = false
                                editingProfile = null
                            },
                            onCancel = {
                                isEditing = false
                                editingProfile = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditForm(
    profile: TransferProfile,
    password: String,
    passphrase: String,
    onPasswordChange: (String) -> Unit,
    onPassphraseChange: (String) -> Unit,
    isTesting: Boolean,
    testResult: Boolean?,
    onTest: (TransferProfile) -> Unit,
    onSave: (TransferProfile) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var host by remember(profile) { mutableStateOf(profile.host) }
    var portText by remember(profile) { mutableStateOf(profile.port.toString()) }
    var username by remember(profile) { mutableStateOf(profile.username) }
    var authType by remember(profile) { mutableStateOf(profile.authType) }
    var privateKeyPath by remember(profile) { mutableStateOf(profile.privateKeyPath ?: "") }
    var remoteDir by remember(profile) { mutableStateOf(profile.remoteDirectory) }
    var catchRemoteDir by remember(profile) { mutableStateOf(profile.effectiveCatchRemoteDirectory) }
    var localDir by remember(profile) { mutableStateOf(profile.localDirectory) }
    var colorTag by remember(profile) { mutableStateOf(profile.colorTag) }

    val currentProfile = profile.copy(
        name = name,
        host = host,
        port = portText.toIntOrNull() ?: 22,
        username = username,
        authType = authType,
        privateKeyPath = privateKeyPath.ifBlank { null },
        remoteDirectory = remoteDir,
        catchRemoteDirectory = catchRemoteDir.ifBlank { null },
        localDirectory = localDir,
        colorTag = colorTag
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // プロファイル名
        BauhausTextField(label = "PROFILE NAME", value = name, onValueChange = { name = it })

        // ホスト & ポート
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BauhausTextField(
                label = "HOST / IP",
                value = host,
                onValueChange = { host = it },
                modifier = Modifier.weight(3f)
            )
            BauhausTextField(
                label = "PORT",
                value = portText,
                onValueChange = { portText = it },
                modifier = Modifier.weight(1.5f)
            )
        }

        // ユーザー名
        BauhausTextField(label = "USERNAME", value = username, onValueChange = { username = it })

        // 認証方式選択
        Text(
            text = "AUTHENTICATION TYPE",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = BauhausColors.FrameBlack
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { authType = AuthType.PASSWORD }
            ) {
                RadioButton(
                    selected = authType == AuthType.PASSWORD,
                    onClick = { authType = AuthType.PASSWORD }
                )
                Text("Password", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { authType = AuthType.PRIVATE_KEY }
            ) {
                RadioButton(
                    selected = authType == AuthType.PRIVATE_KEY,
                    onClick = { authType = AuthType.PRIVATE_KEY }
                )
                Text("Private Key", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (authType == AuthType.PASSWORD) {
            BauhausTextField(
                label = "PASSWORD (DPAPI ENCRYPTED)",
                value = password,
                onValueChange = onPasswordChange,
                isPassword = true
            )
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BauhausTextField(
                    label = "PRIVATE KEY PATH",
                    value = privateKeyPath,
                    onValueChange = { privateKeyPath = it },
                    modifier = Modifier.weight(1f)
                )
                BauhausButton(
                    onClick = {
                        val chooser = JFileChooser()
                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            privateKeyPath = chooser.selectedFile.absolutePath
                        }
                    },
                    backgroundColor = BauhausColors.Chalk,
                    borderWidth = 1.5.dp,
                    shadowOffset = 1.dp
                ) {
                    Text("...", fontWeight = FontWeight.Bold)
                }
            }

            BauhausTextField(
                label = "PASSPHRASE (OPTIONAL)",
                value = passphrase,
                onValueChange = onPassphraseChange,
                isPassword = true
            )
        }

        // ディレクトリ設定
        BauhausTextField(
            label = "TOSS TARGET (送信先リモートディレクトリ)",
            value = remoteDir,
            onValueChange = { remoteDir = it }
        )

        BauhausTextField(
            label = "CATCH ROOT (受信元リモートディレクトリ / 根本)",
            value = catchRemoteDir,
            onValueChange = { catchRemoteDir = it }
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BauhausTextField(
                label = "CATCH LOCAL (保存先ローカルディレクトリ)",
                value = localDir,
                onValueChange = { localDir = it },
                modifier = Modifier.weight(1f)
            )
            BauhausButton(
                onClick = {
                    val chooser = JFileChooser()
                    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        localDir = chooser.selectedFile.absolutePath
                    }
                },
                backgroundColor = BauhausColors.Chalk,
                borderWidth = 1.5.dp,
                shadowOffset = 1.dp
            ) {
                Text("...", fontWeight = FontWeight.Bold)
            }
        }

        // 識別シンボル（幾何学タグ）選択
        Text(
            text = "COLOR TAG",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = BauhausColors.FrameBlack
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val shapes = listOf(
                BauhausColor.RED,
                BauhausColor.YELLOW,
                BauhausColor.BLUE
            )
            shapes.forEach { color ->
                val isSelected = colorTag == color
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isSelected) BauhausColors.PaperWhite else BauhausColors.Chalk, RectangleShape)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) BauhausColors.FrameBlack else BauhausColors.BorderGray,
                            shape = RectangleShape
                        )
                        .clickable { colorTag = color }
                ) {
                    BauhausProfileIcon(colorTag = color, size = 18.dp)
                }
            }
        }

        // 接続テスト結果表示
        if (testResult != null) {
            Text(
                text = if (testResult) "✓ CONNECTION SUCCESSFUL" else "✗ CONNECTION FAILED",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = if (testResult) BauhausColors.CobaltBlue else BauhausColors.CrimsonRed
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 下部アクションボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BauhausButton(
                onClick = onCancel,
                backgroundColor = BauhausColors.Chalk,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "CANCEL",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = BauhausColors.FrameBlack
                )
            }

            BauhausButton(
                onClick = { onTest(currentProfile) },
                backgroundColor = BauhausColors.CadmiumYellow,
                enabled = !isTesting,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isTesting) "TESTING..." else "TEST",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = BauhausColors.FrameBlack
                )
            }

            BauhausButton(
                onClick = { onSave(currentProfile) },
                backgroundColor = BauhausColors.CobaltBlue,
                contentColor = BauhausColors.PaperWhite,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "SAVE",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = BauhausColors.PaperWhite
                )
            }
        }

        // スクロール時に最下部が見切れないよう余白を確保
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun BauhausTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = BauhausColors.FrameBlack
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = BauhausColors.FrameBlack
            ),
            shape = RectangleShape,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = BauhausColors.PaperWhite,
                unfocusedContainerColor = BauhausColors.PaperWhite,
                focusedIndicatorColor = BauhausColors.FrameBlack,
                unfocusedIndicatorColor = BauhausColors.BorderGray,
                focusedTextColor = BauhausColors.FrameBlack,
                unfocusedTextColor = BauhausColors.FrameBlack,
                cursorColor = BauhausColors.FrameBlack
            )
        )
    }
}
