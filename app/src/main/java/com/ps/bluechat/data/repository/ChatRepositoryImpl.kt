package com.ps.bluechat.data.repository

import com.ps.bluechat.data.data_source.ChatDao
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(
    private val chatDao: ChatDao
) : ChatRepository{
    override fun getMessagesByAddress(address: String): Flow<List<BluetoothMessage>> {
        return chatDao.getMessagesByAddress(address = address)
    }

    override fun upsertMessage(bluetoothMessage: BluetoothMessage) {
        chatDao.upsertMessage(bluetoothMessage = bluetoothMessage)
    }

    override fun clearMessagesWithUserByAddress(address: String) {
        chatDao.clearMessagesWithUserByAddress(address = address)
    }

    override fun getLatestMessageByAddress(address: String) : BluetoothMessage {
        return chatDao.getLatestMessageByAddress(address = address)
    }

}