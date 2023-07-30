package com.ps.bluechat.data.repository

import com.ps.bluechat.data.data_source.ChatDao
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(
    private val chatDao: ChatDao
) : ChatRepository{
    override fun getMessagesByAddress(address: String): Flow<List<BluetoothMessage>> {
        TODO("Not yet implemented")
    }

    override fun upsertMessage(bluetoothMessage: BluetoothMessage) {
        TODO("Not yet implemented")
    }

    override fun clearMessagesWithUserByAddress(address: String) {
        TODO("Not yet implemented")
    }

}