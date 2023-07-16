package com.ps.bluechat.presentation.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothUiState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    direction: Direction,
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val message = rememberSaveable {
        mutableStateOf("")
    }

    val isTextFieldEnabled by remember { mutableStateOf(true) }

    val keyboardController = LocalSoftwareKeyboardController.current

    when(state.connectionState) {
        ConnectionState.IDLE -> {direction.navigateBackToHomeScreen()}
        else -> {}
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages", fontSize = 24.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                onDisconnect()
                direction.navigateBackToHomeScreen()
            }) {
                Icon(
                    imageVector = Icons.Default.Close, contentDescription = null
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items = state.messages) { index, message ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatMessage(
                        message = message, modifier = Modifier.align(
                            if (message.isFromLocalUser) Alignment.End else Alignment.Start
                        )
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .gradientSurface()
        ) {
            TextField(
                value = message.value,
                onValueChange = { message.value = it },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.onSecondary
                ),
                enabled = isTextFieldEnabled,
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            val cleanedMsg = message.value.replace(Regex("\\s+"), " ").trim()
                            onSendMessage(cleanedMsg)
                            message.value = ""
                            keyboardController?.hide()
                        })
                },
                placeholder = { Text(text = "Send Message...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )
        }
    }
}