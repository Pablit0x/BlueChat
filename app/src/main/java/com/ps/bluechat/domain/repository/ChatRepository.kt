package com.ps.bluechat.domain.repository

import com.ps.bluechat.domain.chat.BluetoothMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllMessages(): Flow<List<BluetoothMessage>>
    fun getMessagesByAddress(address: String): Flow<List<BluetoothMessage>>
    suspend fun insertMessage(bluetoothMessage: BluetoothMessage)
    fun clearMessagesWithUserByAddress(address: String)
    fun getLatestMessageByAddress(address: String) : BluetoothMessage
}