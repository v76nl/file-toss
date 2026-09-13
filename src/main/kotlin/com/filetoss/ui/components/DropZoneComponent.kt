package com.filetoss.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.domain.model.TransferProgress
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens
import java.io.File
import javax.swing.JFileChooser

@Composable
fun DropZoneComponent(
    isDraggingOver: Boolean,
    isTransferring: Boolean,
    isCompleted: Boolean,
    progress: TransferProgress?,
    onFilesSelected: (List<File>) -> Unit,
    modifier: Modifier = Modifier
) {
    // ドラッグオーバー時のアニメーション
    val borderColor by animateColorAsState(
        targetValue = when {
            isDraggingOver -> BauhausColors.CrimsonRed
            isTransferring -> BauhausColors.CobaltBlue
            isCompleted -> BauhausColors.CadmiumYellow
            else -> BauhausColors.FrameBlack
        },
        animationSpec = tween(durationMillis = 200),
        label = "BorderColorAnim"
    )

    val cornerOffset by animateDpAsState(
        targetValue = if (isDraggingOver) 24.dp else 12.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "CornerOffsetAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isTransferring) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val completionScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.1f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "CompletionScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(BauhausColors.PaperWhite, RectangleShape)
            .scale(if (isTransferring) pulseScale else completionScale)
    ) {
        // バウハウス幾何学外枠 (Canvas描画で点線/実線の切り替え)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(
                width = if (isDraggingOver) 4f else 3f,
                pathEffect = if (isDraggingOver || isTransferring) null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
            drawRect(
                color = borderColor,
                topLeft = Offset(0f, 0f),
                size = size,
                style = stroke
            )
        }

        // 四隅の十字マーク (+)
        CrosshairMark(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = cornerOffset, y = cornerOffset),
            color = borderColor
        )
        CrosshairMark(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -cornerOffset, y = cornerOffset),
            color = borderColor
        )
        CrosshairMark(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = cornerOffset, y = -cornerOffset),
            color = borderColor
        )
        CrosshairMark(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = -cornerOffset, y = -cornerOffset),
            color = borderColor
        )

        // 中央コンテンツ
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                isCompleted -> {
                    // 完了演出: TOSSED!
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BauhausColors.CadmiumYellow, RectangleShape)
                                .border(2.dp, BauhausColors.FrameBlack)
                        )
                        GeometricTriangle(
                            modifier = Modifier.size(24.dp),
                            color = BauhausColors.CrimsonRed,
                            borderColor = BauhausColors.FrameBlack
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BauhausColors.CobaltBlue, CircleShape)
                                .border(2.dp, BauhausColors.FrameBlack)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "TOSSED!",
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        color = BauhausColors.FrameBlack
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "TRANSFER COMPLETED SUCCESSFULLY",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = BauhausColors.CobaltBlue
                    )
                }

                isTransferring -> {
                    // 転送中演出
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(BauhausColors.CobaltBlue, RectangleShape)
                                .border(2.dp, BauhausColors.FrameBlack)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "TOSSING...",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = BauhausColors.CobaltBlue
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = progress?.currentFileName ?: "SENDING TO REMOTE...",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = BauhausColors.FrameBlack
                    )
                }

                isDraggingOver -> {
                    // ドラッグオーバー中演出
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(BauhausColors.CrimsonRed, RectangleShape)
                                .border(2.5.dp, BauhausColors.FrameBlack)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "RELEASE TO TOSS",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        color = BauhausColors.CrimsonRed
                    )
                }

                else -> {
                    // 通常待機時: ■ ▲ ●
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 赤い正方形
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BauhausColors.CrimsonRed, RectangleShape)
                                .border(2.dp, BauhausColors.FrameBlack)
                        )
                        // 黄色い正三角形
                        GeometricTriangle(
                            modifier = Modifier.size(24.dp),
                            color = BauhausColors.CadmiumYellow,
                            borderColor = BauhausColors.FrameBlack
                        )
                        // 青い円
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BauhausColors.CobaltBlue, CircleShape)
                                .border(2.dp, BauhausColors.FrameBlack)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "DROP FILES HERE TO TOSS",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp,
                        color = BauhausColors.FrameBlack
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "OR CLICK TO BROWSE LOCAL FILES",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = BauhausColors.MutedText
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BauhausButton(
                        onClick = {
                            val chooser = JFileChooser()
                            chooser.isMultiSelectionEnabled = true
                            chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
                            val ret = chooser.showOpenDialog(null)
                            if (ret == JFileChooser.APPROVE_OPTION) {
                                val selected = chooser.selectedFiles.toList().ifEmpty {
                                    listOfNotNull(chooser.selectedFile)
                                }
                                if (selected.isNotEmpty()) {
                                    onFilesSelected(selected)
                                }
                            }
                        },
                        backgroundColor = BauhausColors.Chalk
                    ) {
                        Text(
                            text = "[ SELECT FILES / FOLDERS ]",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = BauhausColors.FrameBlack
                        )
                    }
                }
            }
        }
    }
}
