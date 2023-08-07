package com.ps.bluechat.presentation.chat_screen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ps.bluechat.domain.chat.BluetoothController
import com.ps.bluechat.domain.chat.BluetoothDeviceDomain
import com.ps.bluechat.domain.repository.ChatRepository
import com.ps.bluechat.util.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    private var connectedDevice: BluetoothDeviceDomain? = null

    val state = combine(
        bluetoothController.messages,
        bluetoothController.connectedDevice,
        bluetoothController.connectionState,
        _state
    ) { messages, connectedDevice, connectionState, state ->
        connectedDevice?.let {
            this.connectedDevice = it
        }
        state.copy(
            messages = messages.filter { message ->
                message.address == this.connectedDevice?.address
            },
            recipient = this.connectedDevice,
            connectionState = connectionState
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun sendMessage(message: String) {
        Log.d(TAG, "sendMessage(): $message")
        viewModelScope.launch {
            bluetoothController.trySendMessage(message = message)
        }
    }

    fun sendImages(uri: Uri?) {
        Log.d(TAG, "sendImages(): $uri")
        uri?.let { imageUri ->
            viewModelScope.launch {
                bluetoothController.trySendImage(uri = imageUri)
            }
        }
    }

    fun clearAllMessages(address: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatRepository.clearMessagesWithUserByAddress(address = address)
            }
        }
    }

}