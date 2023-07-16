package com.ps.bluechat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun BluetoothDeviceItem(
    deviceName: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.End) {
        Row(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp, shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .gradientSurface()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = deviceName, color = MaterialTheme.colors.onSurface, fontSize = 24.sp
                    )

                    IconButton(onClick = { onClick() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    color = MaterialTheme.colors.background, shape = CircleShape
                                )
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}