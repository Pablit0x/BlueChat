package com.ps.bluechat.presentation.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.chat.MessageType
import com.ps.bluechat.domain.chat.getType
import com.ps.bluechat.presentation.theme.BlueChatColors

@Composable
fun BluetoothPairedDeviceItem(
    deviceName: String,
    onClick: () -> Unit,
    onRemoveBond: () -> Unit
) {

    var isContextMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }
    var itemHeight by remember {
        mutableStateOf(0.dp)
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val density = LocalDensity.current

    Box(modifier = Modifier.onSizeChanged {
        itemHeight = with(density) { it.height.toDp() }
    }) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(20))
            .gradientSurface()
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(true) {
                detectTapGestures(onLongPress = {
                    isContextMenuVisible = true
                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                }, onPress = {
                    val press = PressInteraction.Press(it)
                    interactionSource.emit(press)
                    tryAwaitRelease()
                    interactionSource.emit(PressInteraction.Release(press))
                })
            }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
        DropdownMenu(
            modifier = Modifier.fillMaxWidth(0.5f),
            expanded = isContextMenuVisible,
            onDismissRequest = {
                isContextMenuVisible = false
            },
            offset = pressOffset.copy(
                y = pressOffset.y - itemHeight
            ),
        ) {
            DropdownMenuItem(onClick = {}) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.info))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            Divider(color = BlueChatColors.NormalGrey, thickness = 1.dp)

            DropdownMenuItem(
                onClick = onRemoveBond
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.forget_device))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

fun createLastMessage(lastMsg: BluetoothMessage, deviceName: String): String {
    val result = StringBuilder()
    if (lastMsg.isFromLocalUser) {
        result.append("You: ")
    } else {
        result.append("$deviceName: ")
    }
    if (lastMsg.getType() == MessageType.TEXT) {
        result.append(lastMsg.message)
    } else {
        result.append("send an image...")
    }
    return result.toString()
}