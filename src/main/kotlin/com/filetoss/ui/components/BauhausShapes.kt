package com.filetoss.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.filetoss.domain.model.BauhausColor
import com.filetoss.ui.theme.BauhausColors
import com.filetoss.ui.theme.BauhausDimens

@Composable
fun CrosshairMark(
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    color: Color = BauhausColors.FrameBlack,
    strokeWidth: Float = 2.5f
) {
    Canvas(modifier = modifier.size(size)) {
        val midX = this.size.width / 2f
        val midY = this.size.height / 2f
        drawLine(
            color = color,
            start = Offset(0f, midY),
            end = Offset(this.size.width, midY),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(midX, 0f),
            end = Offset(midX, this.size.height),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun GeometricTriangle(
    modifier: Modifier = Modifier,
    color: Color = BauhausColors.CadmiumYellow,
    borderColor: Color = BauhausColors.FrameBlack
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color = color)
        drawPath(
            path,
            color = borderColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
        )
    }
}

@Composable
fun BauhausProfileIcon(
    colorTag: BauhausColor,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    when (colorTag) {
        BauhausColor.RED -> {
            Box(
                modifier = modifier
                    .size(size)
                    .background(BauhausColors.CrimsonRed, CircleShape)
                    .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, CircleShape)
            )
        }
        BauhausColor.BLUE -> {
            Box(
                modifier = modifier
                    .size(size)
                    .background(BauhausColors.CobaltBlue, RectangleShape)
                    .border(BauhausDimens.BorderWidth, BauhausColors.FrameBlack, RectangleShape)
            )
        }
        BauhausColor.YELLOW -> {
            GeometricTriangle(
                modifier = modifier.size(size),
                color = BauhausColors.CadmiumYellow,
                borderColor = BauhausColors.FrameBlack
            )
        }
    }
}
