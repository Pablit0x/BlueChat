package com.ps.bluechat.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.ps.bluechat.domain.chat.BluetoothMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM bluetoothMessage WHERE address = :address")
    fun getMessagesByAddress(address: String) : Flow<List<BluetoothMessage>>

    @Upsert
    fun upsertMessage(bluetoothMessage: BluetoothMessage)

    @Query("DELETE FROM bluetoothMessage WHERE address = :address")
    fun clearMessagesWithUserByAddress(address: String)
}