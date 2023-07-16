package com.ps.bluechat.data.chat

import android.bluetooth.BluetoothSocket
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.chat.ConnectionResult
import com.ps.bluechat.domain.chat.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            val address = socket.remoteDevice.address
            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw TransferFailedException()
                }

                emit(
                    buffer.decodeToString(endIndex = byteCount).toBluetoothMessage(isFromLocalUser = false, address = address)
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray) : Boolean{
        return withContext(Dispatchers.IO){
            try{
                socket.outputStream.write(bytes)
            } catch (e: IOException){
                return@withContext false
            }
            true
        }
    }
}