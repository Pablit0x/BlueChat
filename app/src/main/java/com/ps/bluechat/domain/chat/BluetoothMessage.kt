package com.ps.bluechat.domain.chat

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BluetoothMessage(
    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,
    val imageUri: Uri? = null,
    val message: String = "",
    val time: String,
    val isFromLocalUser: Boolean,
    val address: String
)
