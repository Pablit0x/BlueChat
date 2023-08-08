package com.ps.bluechat.presentation.device_screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.FloatingActionButtonElevation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ps.bluechat.domain.chat.ScanningState
import com.ps.bluechat.util.Constants.DEFAULT_PADDING
import com.ps.bluechat.util.times
import com.ps.bluechat.util.transform

@Composable
fun FabGroup(
    animationProgress: Float = 0f,
    toggleAnimation: () -> Unit = { },
    scanningState: ScanningState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit

) {
    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd
    ) {

        val topButtonIcon: ImageVector?
        val topButtonAction: () -> Unit

        when (scanningState) {
            ScanningState.DISCOVERING -> {
                topButtonIcon = Icons.Default.Pause
                topButtonAction = onStopScan
            }

            else -> {
                topButtonIcon = Icons.Default.PlayArrow
                topButtonAction = onStartScan
            }
        }
        AnimatedFab(
            icon = topButtonIcon,
            modifier = Modifier.padding(
                    PaddingValues(
                        bottom = 136.dp,
                    ) * FastOutSlowInEasing.transform(0f, 0.8f, animationProgress)
                ),
            opacity = LinearEasing.transform(0.2f, 0.7f, animationProgress),
            onClick = topButtonAction
        )

        AnimatedFab(
            icon = Icons.Default.BluetoothSearching,
            modifier = Modifier.padding(
                    PaddingValues(
                        bottom = 64.dp
                    ) * FastOutSlowInEasing.transform(0.2f, 1.0f, animationProgress)
                ),
            opacity = LinearEasing.transform(0.4f, 0.9f, animationProgress),
            onClick = onStartServer
        )

        val scaleAnimationProgress = FastOutSlowInEasing.transform(0.35f, 0.65f, animationProgress)
        val scale by animateFloatAsState(
            targetValue = if (scaleAnimationProgress < 0.5f) {
                if (scaleAnimationProgress < 0.25f) {
                    1f
                } else {
                    0.5f + (1f - 0.5f) * ((scaleAnimationProgress - 0.25f) * 2)
                }
            } else {
                if (scaleAnimationProgress < 0.75f) {
                    1f - (1f - 0.5f) * ((scaleAnimationProgress - 0.5f) * 2)
                } else {
                    0.5f
                }
            }, label = ""
        )

        AnimatedFab(
            icon = Icons.Default.Add,
            modifier = Modifier
                .scale(scale)
                .rotate(
                    225 * FastOutSlowInEasing.transform(0.35f, 0.65f, animationProgress)
                ),
            onClick = toggleAnimation,
        )
    }
}

@Composable
fun AnimatedFab(
    modifier: Modifier, icon: ImageVector? = null, opacity: Float = 1f, onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 1.dp
        ),
        modifier = modifier
            .scale(1f)
            .animateContentSize(),
        containerColor = MaterialTheme.colors.primary
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Color.White.copy(alpha = opacity)
            )
        }
    }
}