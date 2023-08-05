package com.ps.bluechat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.chat.MessageType
import com.ps.bluechat.domain.chat.getType
import com.ps.bluechat.presentation.theme.BlueChatColors
import com.ps.bluechat.presentation.theme.BlueChatTheme

@Composable
fun ChatMessage(
    message: BluetoothMessage, modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromLocalUser) Alignment.End else Alignment.Start
    ) {
        Text(message.time, color = MaterialTheme.colors.onBackground)

        Column(
            modifier = modifier
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isFromLocalUser) 15.dp else 0.dp,
                        topEnd = 15.dp,
                        bottomStart = 15.dp,
                        bottomEnd = if (message.isFromLocalUser) 0.dp else 15.dp
                    )
                )
                .background(
                    if (message.getType() == MessageType.IMAGE) Color.Transparent
                    else if (message.isFromLocalUser) BlueChatColors.LocalMessageBubbleColor else BlueChatColors.RemoteMessageBubbleColor
                )
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = if (message.isFromLocalUser) Alignment.End else Alignment.Start
        ) {
            if (message.getType() == MessageType.IMAGE) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                        .clip(
                            RoundedCornerShape(10)
                        )
                )
            } else {
                Text(
                    text = message.message,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .padding(start = 16.dp, end = 16.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun ChatMessagePreview() {
    BlueChatTheme {
        ChatMessage(
            message = BluetoothMessage(
                message = "Hello World!",
                isFromLocalUser = true,
                address = "121313141412",
                time = "12:33:05",
            )
        )
    }
}