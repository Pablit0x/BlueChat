package com.ps.bluechat.domain.chat

import kotlinx.coroutines.flow.Flow

sealed interface ConnectionResult{
    object ConnectionOpen : ConnectionResult
    object ConnectionRequest : ConnectionResult
    data class ConnectionEstablished(val messages: Flow<List<BluetoothMessage>>) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}