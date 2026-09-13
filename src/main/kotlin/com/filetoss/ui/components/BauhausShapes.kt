package com.filetoss.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun BauhausSquare(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = BauhausColors.CrimsonRed,
    borderColor: Color = BauhausColors.FrameBlack,
    borderWidth: Dp = BauhausDimens.BorderWidth
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, RectangleShape)
            .border(borderWidth, borderColor, RectangleShape)
    )
}

@Composable
fun BauhausCircle(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = BauhausColors.CobaltBlue,
    borderColor: Color = BauhausColors.FrameBlack,
    borderWidth: Dp = BauhausDimens.BorderWidth
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
            .border(borderWidth, borderColor, CircleShape)
    )
}

@Composable
fun BauhausTriangle(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = BauhausColors.CadmiumYellow,
    borderColor: Color = BauhausColors.FrameBlack,
    borderWidth: Dp = BauhausDimens.BorderWidth
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = borderWidth.toPx()
        val halfStroke = strokeWidth / 2f

        // ストロークがキャンバスからはみ出さないよう内側にインセット
        // 頂角の Miter（先端）が上端で切れないよう、strokeWidth 分インセット
        val topX = this.size.width / 2f
        val topY = strokeWidth
        val rightX = this.size.width - halfStroke
        val rightY = this.size.height - halfStroke
        val leftX = halfStroke
        val leftY = this.size.height - halfStroke

        val path = Path().apply {
            moveTo(topX, topY)
            lineTo(rightX, rightY)
            lineTo(leftX, leftY)
            close()
        }

        drawPath(path, color = color)
        drawPath(
            path,
            color = borderColor,
            style = Stroke(
                width = strokeWidth,
                join = StrokeJoin.Miter,
                cap = StrokeCap.Square
            )
        )
    }
}

@Composable
fun BauhausTrio(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    borderWidth: Dp = 2.dp,
    spacing: Dp = 16.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BauhausSquare(size = size, color = BauhausColors.CrimsonRed, borderWidth = borderWidth)
        BauhausTriangle(size = size, color = BauhausColors.CadmiumYellow, borderWidth = borderWidth)
        BauhausCircle(size = size, color = BauhausColors.CobaltBlue, borderWidth = borderWidth)
    }
}

@Composable
fun BauhausProfileIcon(
    colorTag: BauhausColor,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    borderWidth: Dp = BauhausDimens.BorderWidth
) {
    when (colorTag) {
        BauhausColor.RED -> {
            // バウハウス幾何学: 赤＝正方形
            BauhausSquare(
                modifier = modifier,
                size = size,
                color = BauhausColors.CrimsonRed,
                borderWidth = borderWidth
            )
        }
        BauhausColor.YELLOW -> {
            // バウハウス幾何学: 黄＝三角形
            BauhausTriangle(
                modifier = modifier,
                size = size,
                color = BauhausColors.CadmiumYellow,
                borderWidth = borderWidth
            )
        }
        BauhausColor.BLUE -> {
            // バウハウス幾何学: 青＝円
            BauhausCircle(
                modifier = modifier,
                size = size,
                color = BauhausColors.CobaltBlue,
                borderWidth = borderWidth
            )
        }
    }
}
