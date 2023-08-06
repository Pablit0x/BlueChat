package com.ps.bluechat.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ps.bluechat.domain.chat.BluetoothMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM bluetoothMessage")
    fun getAllMessages(): Flow<List<BluetoothMessage>>

    @Query("SELECT * FROM bluetoothMessage WHERE address = :address")
    fun getMessagesByAddress(address: String): Flow<List<BluetoothMessage>>

    @Insert
    suspend fun insertMessage(bluetoothMessage: BluetoothMessage)

    @Query("DELETE FROM bluetoothMessage WHERE address = :address")
    fun clearMessagesWithUserByAddress(address: String)

    @Query("SELECT * FROM bluetoothMessage WHERE address = :address ORDER BY time DESC LIMIT 1")
    fun getLatestMessageByAddress(address: String) : BluetoothMessage
}