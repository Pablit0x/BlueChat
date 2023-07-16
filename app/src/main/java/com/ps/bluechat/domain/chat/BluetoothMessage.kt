package com.ps.bluechat.domain.chat

data class BluetoothMessage(
    val message: String,
    val time: String,
    val isFromLocalUser: Boolean,
    val address: String
)
