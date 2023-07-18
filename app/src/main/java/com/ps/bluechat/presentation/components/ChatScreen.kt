package com.ps.bluechat.presentation.components

import android.app.ProgressDialog.show
import android.content.Context
import android.view.Gravity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.ConnectionState
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothUiState
import es.dmoral.toasty.Toasty

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    direction: Direction,
    recipientName: String?,
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val message = rememberSaveable {
        mutableStateOf("")
    }

    val isTextFieldEnabled by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context: Context = LocalContext.current

    LaunchedEffect(key1 = state.connectionState) {
        when (state.connectionState) {
            ConnectionState.IDLE -> {
                Toasty.warning(
                    context,
                    "${recipientName ?: context.getString(R.string.unknown)} " + context.getString(R.string.has_disconnected_from_chat),
                    Toasty.LENGTH_LONG
                ).show()
            }
            ConnectionState.CONNECTION_ACTIVE -> {
                val toast = Toasty.success(context, context.getString(R.string.connected), Toasty.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM, 0, 300)
                toast.show()
            }
            else -> {}
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colors.background
        ), title = {
            Text(
                text = recipientName ?: stringResource(id = R.string.messages),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }, actions = {
            IconButton(onClick = {
                direction.navigateBackToHomeScreen()
                onDisconnect()
            }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp, contentDescription = null
                )
            }
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Divider(color = Color.DarkGray, thickness = 1.dp)
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
                        focusedBorderColor = MaterialTheme.colors.onSecondary,
                        cursorColor = MaterialTheme.colors.onSecondary
                    ),
                    enabled = isTextFieldEnabled,
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                val cleanedMsg = formatMessage(message.value)
                                if (cleanedMsg.isNotBlank()) {
                                    onSendMessage(cleanedMsg)
                                    message.value = ""
                                    keyboardController?.hide()
                                }
                            })
                    },
                    placeholder = { Text(text = stringResource(id = R.string.send_message)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                )
            }
        }
    }
}

fun formatMessage(msg: String): String {
    return msg.replace(Regex("\\s+"), " ").trim()
}