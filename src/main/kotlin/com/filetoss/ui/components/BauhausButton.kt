package com.filetoss.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens

@Composable
fun BauhausButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BauhausColors.PaperWhite,
    contentColor: Color = BauhausColors.FrameBlack,
    borderColor: Color = BauhausColors.FrameBlack,
    borderWidth: Dp = BauhausDimens.BorderWidth,
    shadowOffset: Dp = BauhausDimens.ShadowOffset,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val effectiveOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = 50),
        label = "ButtonPressOffset"
    )

    Box(modifier = modifier) {
        // 固定の黒シャドウ
        if (enabled && shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(BauhausColors.FrameBlack, RectangleShape)
            )
        }

        // ボタン前面
        Box(
            modifier = Modifier
                .offset(x = effectiveOffset, y = effectiveOffset)
                .background(if (enabled) backgroundColor else BauhausColors.BorderGray, RectangleShape)
                .border(borderWidth, if (enabled) borderColor else BauhausColors.FrameBlack, RectangleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun BauhausSimpleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BauhausColors.PaperWhite,
    contentColor: Color = BauhausColors.FrameBlack,
    enabled: Boolean = true
) {
    BauhausButton(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if (enabled) contentColor else BauhausColors.MutedText,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
    }
}
