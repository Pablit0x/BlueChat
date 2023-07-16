package com.ps.bluechat.data.chat

import android.util.Log
import com.ps.bluechat.domain.chat.BluetoothMessage
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

fun String.toBluetoothMessage(isFromLocalUser: Boolean, address: String): BluetoothMessage {
    val message = substringAfterLast("&")
    return BluetoothMessage(
        message = message,
        isFromLocalUser = isFromLocalUser,
        address = address,
        time = getCurrentTime()
    )
}

fun BluetoothMessage.toByteArray(): ByteArray {
    return message.encodeToByteArray()
}

fun getCurrentTime() : String{
    return LocalTime.now().format(
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.ENGLISH)
    )
}