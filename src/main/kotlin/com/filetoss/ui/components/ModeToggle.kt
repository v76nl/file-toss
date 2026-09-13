package com.filetoss.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens

enum class AppMode {
    TOSS,
    CATCH
}

@Composable
fun ModeToggle(
    currentMode: AppMode,
    onModeChanged: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCatch = currentMode == AppMode.CATCH

    // スライドアニメーション (0f = TOSS, 1f = CATCH)
    val sliderPosition by animateFloatAsState(
        targetValue = if (isCatch) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ModeToggleSlider"
    )

    val activeColor by animateColorAsState(
        targetValue = if (isCatch) BauhausColors.CobaltBlue else BauhausColors.CrimsonRed,
        animationSpec = tween(durationMillis = 200),
        label = "ModeToggleColor"
    )

    Box(
        modifier = modifier
            .width(260.dp)
            .height(44.dp)
            .background(BauhausColors.PaperWhite, RectangleShape)
            .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, RectangleShape)
    ) {
        // スライドするカラー背景ブロック
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .align(if (sliderPosition <= 0.5f) Alignment.CenterStart else Alignment.CenterEnd)
                .background(activeColor, RectangleShape)
                .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, RectangleShape)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // TOSS ボタン
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onModeChanged(AppMode.TOSS) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TOSS",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (currentMode == AppMode.TOSS) BauhausColors.PaperWhite else BauhausColors.FrameBlack
                )
            }

            // CATCH ボタン
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onModeChanged(AppMode.CATCH) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CATCH",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (currentMode == AppMode.CATCH) BauhausColors.PaperWhite else BauhausColors.FrameBlack
                )
            }
        }
    }
}
