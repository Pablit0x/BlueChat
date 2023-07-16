package com.ps.bluechat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.presentation.theme.BlueChatTheme

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromLocalUser) Alignment.End else Alignment.Start
    ) {
        Text(message.time, color = MaterialTheme.colors.onBackground)
        Column(modifier = modifier
            .clip(
                shape = RoundedCornerShape(40)
            )
            .background(
                if(message.isFromLocalUser) Color(0xFF1982FC) else Color(0xFF53565B)
            )
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalAlignment = if (message.isFromLocalUser) Alignment.End else Alignment.Start) {
            Text(
                text = message.message,
                fontSize = 18.sp,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.widthIn(max = 300.dp)
            )
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