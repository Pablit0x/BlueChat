package com.ps.bluechat.domain.chat

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BluetoothMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val imageUri: Uri? = null,
    val message: String = "",
    val time: String,
    val isFromLocalUser: Boolean,
    val address: String
)

enum class MessageType {
    IMAGE,
    TEXT
}

fun BluetoothMessage.getType(): MessageType {
    return if (imageUri == null) {
        MessageType.TEXT
    } else {
        MessageType.IMAGE
    }
}
