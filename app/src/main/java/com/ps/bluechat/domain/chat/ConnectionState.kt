package com.ps.bluechat.domain.chat

enum class ConnectionState {
    IDLE,
    CONNECTION_REQUEST,
    CONNECTION_OPEN,
    CONNECTION_ACTIVE
}

fun ConnectionState.isActive() : Boolean{
    return this == ConnectionState.CONNECTION_ACTIVE
}

fun ConnectionState.isIdle() : Boolean{
    return this == ConnectionState.IDLE
}