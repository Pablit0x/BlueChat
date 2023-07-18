package com.ps.bluechat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox


@Composable
fun BluetoothPairedDeviceItem(
    deviceName: String, onClick: () -> Unit, onRemoveBond: () -> Unit, modifier: Modifier = Modifier
) {

    val archive = SwipeAction(onSwipe = {
        onRemoveBond()
    }, icon = {
        Icon(
            imageVector = Icons.Default.Delete, contentDescription = null
        )
    }, background = MaterialTheme.colors.error
    )


    SwipeableActionsBox(
        endActions = listOf(archive), swipeThreshold = 150.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(100))
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Row(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
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
                            text = deviceName,
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 24.sp
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

}