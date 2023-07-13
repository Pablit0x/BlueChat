package com.ps.bluechat.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@ExperimentalMaterialApi
@Composable
fun BluetoothActionSelector(
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit
) {
    var expandedState by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }


    Box(modifier = Modifier
        .animateContentSize(
            animationSpec = tween(
                durationMillis = 300, easing = LinearOutSlowInEasing
            )
        )
        .clickable(
            interactionSource = interactionSource, indication = null
        ) {
            expandedState = !expandedState
        }) {
        Column {
            if (!expandedState) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                    tint = MaterialTheme.colors.onSecondary,
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colors.secondary, shape = CircleShape).padding(8.dp)
                )
            }
            if (expandedState) {
                Column(
                    horizontalAlignment = Alignment.End
                ){
                    Button(onClick = {
                        onStartScan()
                        expandedState = !expandedState
                    },
                        modifier = Modifier.fillMaxWidth(0.3f)){
                        Text("Start Scan")
                    }

                    Button(onClick = {
                        onStopScan()
                        expandedState = !expandedState
                    },
                    modifier = Modifier.fillMaxWidth(0.3f)
                    ){
                        Text("Stop Scan")
                    }

                    Button(onClick = {
                        onStartScan()
                        expandedState = !expandedState
                    },
                        modifier = Modifier.fillMaxWidth(0.3f)
                    ){
                        Text("Start Server")
                    }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(48.dp).padding(8.dp)
                        )
                }
            }
        }
    }
}