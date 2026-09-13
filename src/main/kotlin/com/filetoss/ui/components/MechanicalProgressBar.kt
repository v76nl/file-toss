package com.filetoss.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.domain.model.TransferProgress
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens

@Composable
fun MechanicalProgressBar(
    progress: TransferProgress?,
    isTransferring: Boolean,
    statusText: String? = null,
    modifier: Modifier = Modifier
) {
    val currentPercentage = progress?.percentage ?: 0f
    val animatedProgress by animateFloatAsState(
        targetValue = currentPercentage,
        animationSpec = tween(durationMillis = 150),
        label = "ProgressBarAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BauhausColors.PaperWhite)
            .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, RectangleShape)
            .padding(12.dp)
    ) {
        // ステータステキスト & 数値情報
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fileNameDisplay = when {
                isTransferring && progress != null -> "FILE: ${progress.currentFileName}"
                !statusText.isNullOrBlank() -> statusText
                else -> "STATUS: READY"
            }

            Text(
                text = fileNameDisplay,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = BauhausColors.FrameBlack
            )

            if (isTransferring && progress != null) {
                Text(
                    text = "${progress.formattedTransferred} / ${progress.formattedTotal} | ${progress.formattedSpeed} | ${(progress.percentage * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = BauhausColors.CobaltBlue
                )
            } else {
                Text(
                    text = "STANDBY",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = BauhausColors.MutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 目盛り付きプログレスバー
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(BauhausColors.Chalk, RectangleShape)
                .border(2.dp, BauhausColors.FrameBlack, RectangleShape)
        ) {
            // 充填ブロック (コバルトブルー)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(BauhausColors.CobaltBlue, RectangleShape)
            )

            // 目盛り線のCanvas描画
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = size.width / 20f
                for (i in 1..19) {
                    val x = i * step
                    val markHeight = if (i % 5 == 0) size.height else size.height * 0.4f
                    drawLine(
                        color = BauhausColors.FrameBlack,
                        start = Offset(x, size.height - markHeight),
                        end = Offset(x, size.height),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }
}
