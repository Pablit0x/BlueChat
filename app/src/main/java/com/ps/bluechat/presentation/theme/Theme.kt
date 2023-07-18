package com.ps.bluechat.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ps.bluechat.presentation.theme.Colors.Companion.AccentViolet
import com.ps.bluechat.presentation.theme.Colors.Companion.DarkGrey
import com.ps.bluechat.presentation.theme.Colors.Companion.LightBlueGrey
import com.ps.bluechat.presentation.theme.Colors.Companion.NormalGrey
import com.ps.bluechat.presentation.theme.Colors.Companion.Purple
import com.ps.bluechat.presentation.theme.Colors.Companion.SuperLightGrey
import com.ps.bluechat.presentation.theme.Colors.Companion.TextBlack


private val LightColorPalette = lightColors(
    secondary = SuperLightGrey,
    onSecondary = TextBlack,
    secondaryVariant = Purple,
    primary = AccentViolet,
    background = LightBlueGrey,
    onPrimary = Color.White,
    onBackground = TextBlack,
    surface = Color.White,
    onSurface = TextBlack
)

private val DarkColorPalette = darkColors(
    secondary = NormalGrey,
    onSecondary = Color.White,
    secondaryVariant = Color.White,
    primary = AccentViolet,
    background = DarkGrey,
    onPrimary = Color.White,
    onBackground = Color.White,
    surface = DarkGrey,
    onSurface = Color.White
)

@Composable
fun BlueChatTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}