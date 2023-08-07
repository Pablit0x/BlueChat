package com.ps.bluechat.domain.chat
sealed interface ConnectionResult{
    object ConnectionOpen : ConnectionResult
    object ConnectionRequest : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}