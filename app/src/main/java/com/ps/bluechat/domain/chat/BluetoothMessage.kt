package com.ps.bluechat.domain.chat

import android.net.Uri

data class BluetoothMessage(
    val imageUri: Uri? = null,
    val message: String = "",
    val time: String,
    val isFromLocalUser: Boolean,
    val address: String
)
