package com.ps.bluechat.domain.chat

enum class ConnectionState {
    IDLE,
    REQUEST,
    OPEN,
    ACTIVE
}

fun ConnectionState.isActive() : Boolean{
    return this == ConnectionState.ACTIVE
}

fun ConnectionState.isIdle() : Boolean{
    return this == ConnectionState.IDLE
}