package com.ps.bluechat.domain.repository

import com.ps.bluechat.domain.chat.BluetoothMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessagesByAddress(address: String) : Flow<List<BluetoothMessage>>
    fun upsertMessage(bluetoothMessage: BluetoothMessage)
    fun clearMessagesWithUserByAddress(address: String)
}