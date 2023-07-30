package com.ps.bluechat.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.converters.UriTypeConverter

@Database(
    entities = [BluetoothMessage::class],
    version = 1
)

@TypeConverters(UriTypeConverter::class,)
abstract class ChatDatabase : RoomDatabase(){
    abstract val chatDao: ChatDao

    companion object {
        const val DATABASE_NAME = "chats"
    }
}