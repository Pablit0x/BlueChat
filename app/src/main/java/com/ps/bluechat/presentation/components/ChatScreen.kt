package com.ps.bluechat.presentation.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay
import com.ps.bluechat.presentation.theme.Colors.Companion.DarkGrey
import com.ps.bluechat.presentation.theme.Colors.Companion.SuccessGreen

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    direction: Direction,
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val context: Context = LocalContext.current

    val message = rememberSaveable {
        mutableStateOf("")
    }

    var isPopupDisplayed by remember {
        mutableStateOf(false)
    }
    var popupMessage by remember {
        mutableStateOf("")
    }

    var isPopupWarning by remember {
        mutableStateOf(false)
    }

    val recipientName = rememberSaveable {
        state.connectedDevice?.deviceName ?: context.getString(
            R.string.unknown
        )
    }

    var isTextFieldEnabled by remember { mutableStateOf(true) }

    val lazyColumnListState = rememberLazyListState()

    LaunchedEffect(key1 = state.messages.size){
        if(state.messages.size > 6)
            lazyColumnListState.animateScrollToItem(state.messages.size - 1)
    }

    LaunchedEffect(key1 = state.connectionState) {
        when (state.connectionState) {
            ConnectionState.IDLE -> {
                isTextFieldEnabled = false
                popupMessage =
                    "$recipientName " + context.getString(R.string.has_disconnected_from_chat)
                isPopupWarning = true
                isPopupDisplayed = true
            }
            ConnectionState.CONNECTION_ACTIVE -> {
                isTextFieldEnabled = true
                popupMessage = context.getString(R.string.connected)
                isPopupWarning = false
                isPopupDisplayed = true
            }
            else -> {}
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colors.background
        ), title = {
            Text(
                text = recipientName, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
        }, actions = {
            IconButton(onClick = {
                onDisconnect()
                direction.navigateBackToHomeScreen()
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
            Divider(color = com.ps.bluechat.presentation.theme.Colors.NormalGrey, thickness = 1.dp)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyColumnListState
            ) {
                items(items = state.messages) { message ->
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isPopupDisplayed) {
                    ShowCustomPopupMessage(message = popupMessage,
                        isError = isPopupWarning,
                        duration = 2500L,
                        onStopDisplaying = { isPopupDisplayed = it })
                }

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
                                }
                            })
                    },
                    placeholder = { Text(text = stringResource(id = R.string.send_message)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(65.dp)
                        .gradientSurface()
                )
            }
        }
    }
}

fun formatMessage(msg: String): String {
    return msg.replace(Regex("\\s+"), " ").trim()
}

@Composable
fun ShowCustomPopupMessage(
    message: String, isError: Boolean, duration: Long, onStopDisplaying: (Boolean) -> Unit
) {
    var shouldDisplay by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(true) {
        delay(duration)
        shouldDisplay = false
        onStopDisplaying(false)
    }

    AnimatedVisibility(visible = shouldDisplay) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(shape = RoundedCornerShape(20))
                .background(color = if (isError) MaterialTheme.colors.error else SuccessGreen)
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 18.sp,
                color = if (isError) MaterialTheme.colors.onError else MaterialTheme.colors.onPrimary
            )
        }
    }
}