package com.filetoss.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.domain.model.RemoteFileInfo
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RemoteBrowserComponent(
    currentRemotePath: String,
    files: List<RemoteFileInfo>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNavigateRoot: () -> Unit,
    onNavigateUp: () -> Unit,
    onFileCatch: (RemoteFileInfo) -> Unit,
    onDirectoryNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(BauhausColors.PaperWhite, RectangleShape)
            .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, RectangleShape)
            .padding(16.dp)
    ) {
        // パス表示バー & 操作ボタン (ROOT / UP / REFRESH)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BauhausColors.Chalk)
                .border(2.dp, BauhausColors.FrameBlack, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(BauhausColors.CobaltBlue, RectangleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REMOTE: $currentRemotePath",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = BauhausColors.FrameBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BauhausButton(
                    onClick = onNavigateRoot,
                    backgroundColor = BauhausColors.Chalk,
                    borderWidth = 1.5.dp,
                    shadowOffset = 1.dp,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "ROOT",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = BauhausColors.FrameBlack
                    )
                }

                BauhausButton(
                    onClick = onNavigateUp,
                    backgroundColor = BauhausColors.Chalk,
                    borderWidth = 1.5.dp,
                    shadowOffset = 1.dp,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "UP",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = BauhausColors.FrameBlack
                    )
                }

                BauhausButton(
                    onClick = onRefresh,
                    backgroundColor = BauhausColors.CadmiumYellow,
                    borderWidth = 1.5.dp,
                    shadowOffset = 1.dp,
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (isLoading) "SYNCING..." else "REFRESH",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = BauhausColors.FrameBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RETRIEVING REMOTE FILES...",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = BauhausColors.CobaltBlue
                )
            }
        } else if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO FILES FOUND IN REMOTE DIRECTORY",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = BauhausColors.MutedText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Drop files in TOSS mode or verify the path",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = BauhausColors.MutedText
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files) { fileInfo ->
                    RemoteFileCard(
                        file = fileInfo,
                        onCatch = { onFileCatch(fileInfo) },
                        onNavigate = {
                            if (fileInfo.isDirectory) {
                                onDirectoryNavigate(fileInfo.fullPath)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RemoteFileCard(
    file: RemoteFileInfo,
    onCatch: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(file.modifiedTime))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BauhausColors.Chalk, RectangleShape)
            .border(2.dp, BauhausColors.FrameBlack, RectangleShape)
            .clickable(onClick = if (file.isDirectory) onNavigate else onCatch)
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // ファイル / ディレクトリの幾何学アイコン
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (file.isDirectory) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(BauhausColors.CadmiumYellow, RectangleShape)
                                .border(1.5.dp, BauhausColors.FrameBlack)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DIR",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = BauhausColors.FrameBlack
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(BauhausColors.CobaltBlue, CircleShape)
                                .border(1.5.dp, BauhausColors.FrameBlack)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FILE",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = BauhausColors.FrameBlack
                        )
                    }
                }

                Text(
                    text = file.formattedSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = BauhausColors.MutedText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = file.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BauhausColors.FrameBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formattedDate,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = BauhausColors.MutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (file.isDirectory) {
                BauhausButton(
                    onClick = onNavigate,
                    backgroundColor = BauhausColors.PaperWhite,
                    borderWidth = 1.5.dp,
                    shadowOffset = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OPEN DIR ->",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = BauhausColors.FrameBlack
                    )
                }
            } else {
                BauhausButton(
                    onClick = onCatch,
                    backgroundColor = BauhausColors.CobaltBlue,
                    contentColor = BauhausColors.PaperWhite,
                    borderWidth = 1.5.dp,
                    shadowOffset = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CATCH",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = BauhausColors.PaperWhite
                    )
                }
            }
        }
    }
}
