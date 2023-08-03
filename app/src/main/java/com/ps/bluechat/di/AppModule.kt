package com.ps.bluechat.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.ps.bluechat.data.chat.AndroidBluetoothController
import com.ps.bluechat.data.data_source.ChatDatabase
import com.ps.bluechat.data.repository.ChatRepositoryImpl
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideChatDatabase(context: Application): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            ChatDatabase.DATABASE_NAME,
        ).build()
    }

    @Provides
    @Singleton
    fun provideChatRepository(db: ChatDatabase): ChatRepository {
        return ChatRepositoryImpl(db.chatDao)
    }

    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context, chatRepository: ChatRepository) : BluetoothController {
        return AndroidBluetoothController(context, chatRepository = chatRepository)
    }
}