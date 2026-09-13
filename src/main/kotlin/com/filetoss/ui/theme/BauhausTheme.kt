package com.filetoss.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object BauhausColors {
    val Chalk = Color(0xFFF5F3EC)
    val FrameBlack = Color(0xFF1A1A1A)
    val CrimsonRed = Color(0xFFD93829)
    val CobaltBlue = Color(0xFF205493)
    val CadmiumYellow = Color(0xFFF3BE22)
    val PaperWhite = Color(0xFFFFFFFF)
    val BorderGray = Color(0xFFCCCCCC)
    val MutedText = Color(0xFF666666)
}

object BauhausDimens {
    val BorderWidth = 2.5.dp
    val ThickBorderWidth = 3.dp
    val ShadowOffset = 4.dp
}

val BauhausTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        letterSpacing = 2.sp,
        color = BauhausColors.FrameBlack
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 1.5.sp,
        color = BauhausColors.FrameBlack
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
        color = BauhausColors.FrameBlack
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = BauhausColors.FrameBlack
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = BauhausColors.FrameBlack
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp,
        color = BauhausColors.FrameBlack
    )
)

private val BauhausColorScheme = lightColorScheme(
    primary = BauhausColors.CrimsonRed,
    onPrimary = BauhausColors.PaperWhite,
    secondary = BauhausColors.CobaltBlue,
    onSecondary = BauhausColors.PaperWhite,
    tertiary = BauhausColors.CadmiumYellow,
    onTertiary = BauhausColors.FrameBlack,
    background = BauhausColors.Chalk,
    onBackground = BauhausColors.FrameBlack,
    surface = BauhausColors.PaperWhite,
    onSurface = BauhausColors.FrameBlack
)

@Composable
fun BauhausTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BauhausColorScheme,
        typography = BauhausTypography,
        content = content
    )
}
