package com.ps.bluechat.data.chat

import android.net.Uri
import com.ps.bluechat.domain.chat.BluetoothMessage
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun String.toBluetoothMessage(isFromLocalUser: Boolean, address: String): BluetoothMessage {
    val message = substringAfterLast("±")

    return BluetoothMessage(
        message = message,
        isFromLocalUser = isFromLocalUser,
        address = address,
        time = getCurrentTime()
    )

}

fun Uri?.toBluetoothMessage(isFromLocalUser: Boolean, address: String): BluetoothMessage {
    if (this == null) {
        return BluetoothMessage(
            imageUri = null,
            message = "Failed to send an image!",
            isFromLocalUser = isFromLocalUser,
            address = address,
            time = getCurrentTime()
        )
    }

    return BluetoothMessage(
        imageUri = this,
        message = "",
        isFromLocalUser = isFromLocalUser,
        address = address,
        time = getCurrentTime()
    )
}

fun BluetoothMessage.toByteArray(): ByteArray {
    return "±$message".encodeToByteArray()
}

fun getCurrentTime(): String {
    return LocalTime.now().format(
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.ENGLISH)
    )
}