package com.ps.bluechat.domain.repository

import com.ps.bluechat.domain.chat.BluetoothMessage

interface ChatRepository {
    fun getMessagesByAddress(address: String) : List<BluetoothMessage>
    suspend fun insertMessage(bluetoothMessage: BluetoothMessage)
    fun clearMessagesWithUserByAddress(address: String)
    fun getLatestMessageByAddress(address: String) : BluetoothMessage
}