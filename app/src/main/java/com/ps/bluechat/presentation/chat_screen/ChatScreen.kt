package com.ps.bluechat.presentation.chat_screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.isActive
import com.ps.bluechat.domain.chat.isIdle
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothUiState
import com.ps.bluechat.presentation.ToastUiState
import com.ps.bluechat.presentation.components.ChatMessage
import com.ps.bluechat.presentation.components.gradientSurface
import com.ps.bluechat.presentation.theme.BlueChatColors
import com.talhafaki.composablesweettoast.util.SweetToastUtil.SweetError
import com.talhafaki.composablesweettoast.util.SweetToastUtil.SweetSuccess
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    direction: Direction,
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onDeleteAllMessages: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onUriSelected: (Uri?) -> Unit
) {

    val photoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uris ->
                onUriSelected(uris)
            })

    val context: Context = LocalContext.current

    val message = rememberSaveable {
        mutableStateOf("")
    }

    var toastState by remember { mutableStateOf(ToastUiState()) }

    val recipientName = rememberSaveable {
        state.connectedDevice?.deviceName ?: context.getString(
            R.string.unknown
        )
    }

    val lazyColumnListState = rememberLazyListState()

    LaunchedEffect(key1 = state.messages.size) {
        if (state.messages.size > 6) lazyColumnListState.animateScrollToItem(state.messages.size - 1)
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            direction.navigateBack
        }
    }

    LaunchedEffect(true){
        delay(200)
        if(state.connectionState.isActive()){
            toastState = toastState.copy(
                message = context.getString(R.string.connected),
                isWarning = false,
                isDisplayed = true
            )
        }
    }

    if(state.connectionState.isIdle()){
        toastState = toastState.copy(
            message = "$recipientName " + context.getString(R.string.has_disconnected_from_chat),
            isWarning = true,
            isDisplayed = true
        )
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colors.background
        ), title = {
            Text(
                text = recipientName, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
        }, actions = {
            AnimatedVisibility(visible = state.messages.isNotEmpty()) {
                IconButton(onClick = {
                    onDeleteAllMessages(state.messages.last().address)
                }) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever, contentDescription = null
                    )
                }
            }

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
            Divider(color = BlueChatColors.NormalGrey, thickness = 1.dp)
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
                if (toastState.isDisplayed) {
                    if (toastState.isWarning) {
                        SweetError(
                            message = toastState.message,
                            duration = Toast.LENGTH_SHORT,
                            padding = PaddingValues(bottom = 90.dp),
                            contentAlignment = Alignment.BottomCenter
                        )
                    } else {
                        SweetSuccess(
                            message = toastState.message,
                            duration = Toast.LENGTH_SHORT,
                            padding = PaddingValues(bottom = 90.dp),
                            contentAlignment = Alignment.BottomCenter
                        )
                    }
                    toastState.isDisplayed = false
                }

                TextField(
                    value = message.value,
                    onValueChange = { message.value = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.onSecondary,
                        cursorColor = MaterialTheme.colors.onSecondary
                    ),
                    enabled = state.connectionState.isActive(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            })
                    },
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
                        .height(64.dp)
                        .gradientSurface()
                        .clip(RoundedCornerShape(20))
                )
            }
        }
    }
}

fun formatMessage(msg: String): String {
    return msg.replace(Regex("\\s+"), " ").trim()
}