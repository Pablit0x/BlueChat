package com.ps.bluechat.domain.chat

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BluetoothMessage(
    val imageUri: Uri? = null,
    val message: String = "",
    val time: String,
    val isFromLocalUser: Boolean,
    @PrimaryKey
    val address: String
)
