package com.ps.bluechat.domain.chat

sealed interface ConnectionResult{
    object ConnectionOpen : ConnectionResult
    object ConnectionRequest : ConnectionResult
    data class ConnectionEstablished(val messages: List<BluetoothMessage>) : ConnectionResult
    data class TransferSucceeded(val message: BluetoothMessage) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}