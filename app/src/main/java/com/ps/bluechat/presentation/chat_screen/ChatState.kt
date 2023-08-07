package com.ps.bluechat.presentation.chat_screen

import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.chat.ConnectionState

data class ChatState(
    val messages: List<BluetoothMessage> = emptyList(),
    val recipient: BluetoothDeviceDomain? = null,
    val connectionState: ConnectionState = ConnectionState.ACTIVE
)