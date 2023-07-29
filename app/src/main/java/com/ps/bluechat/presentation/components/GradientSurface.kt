package com.ps.bluechat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import com.ps.bluechat.presentation.theme.BlueChatColors.Companion.DarkSurfaceEnd
import com.ps.bluechat.presentation.theme.BlueChatColors.Companion.DarkSurfaceStart

fun Modifier.gradientSurface(): Modifier = composed {
    if(isSystemInDarkTheme()){
        Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    DarkSurfaceStart,
                    DarkSurfaceEnd
                )
            )
        )
    } else {
        Modifier.background(MaterialTheme.colors.surface)
    }
}