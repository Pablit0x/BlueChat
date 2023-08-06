package com.ps.bluechat.data.chat

import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.ps.bluechat.domain.chat.BluetoothMessage
import com.ps.bluechat.domain.chat.TransferFailedException
import com.ps.bluechat.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    fun listenForIncomingMessages(context: Context): Flow<BluetoothMessage> {
        Log.d(TAG, "listenForIncomingMessages()")
        return flow {

            if (!socket.isConnected) {
                return@flow
            }

            val buffer = ByteArray(8192)
            val address = socket.remoteDevice.address

            while (true) {

                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw TransferFailedException()
                }

                val decodedString = buffer.decodeToString()

                // This prefix indicates a text message
                if (decodedString.startsWith("±")) {
                    emit(
                        buffer.decodeToString(endIndex = byteCount)
                            .toBluetoothMessage(isFromLocalUser = false, address = address)
                    )
                } else {
                    val messageSize = String(buffer, 0, byteCount).toInt()
                    val completeByteArray = ByteArray(messageSize)
                    var offset = 0

                    while (offset < messageSize) {
                        val imageByteCount = try {
                            socket.inputStream.read(completeByteArray, offset, messageSize - offset)
                        } catch (e: IOException) {
                            throw TransferFailedException()
                        }

                        offset += imageByteCount
                    }

                    val bitmap =
                        BitmapFactory.decodeByteArray(completeByteArray, 0, completeByteArray.size)

                    // Save the Bitmap to MediaStore and get the image URI
                    val imageUri = saveBitmapToMediaStore(context, bitmap)

                    emit(imageUri.toBluetoothMessage(isFromLocalUser = false, address = address))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun generateUniqueFileName(): String {
        Log.d(TAG, "generateUniqueFileName()")
        val timestamp = System.currentTimeMillis()
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "image_${timestamp}_$randomString.jpg"
    }

    fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap): Uri? {
        Log.d(TAG, "saveBitmapToMediaStore()")
        val filename = generateUniqueFileName()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
        }
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        var imageUri: Uri? = null
        context.contentResolver?.let {
            imageUri = it.insert(contentUri, contentValues)
            imageUri?.let { uri ->
                val outputStream = it.openOutputStream(uri)
                outputStream?.use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }
            }
        }
        return imageUri
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        Log.d(TAG, "sendMessage()")
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                return@withContext false
            }
            true
        }
    }
}