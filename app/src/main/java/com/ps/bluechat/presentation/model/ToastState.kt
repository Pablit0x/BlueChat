package com.ps.bluechat.presentation.model

data class ToastState(
    var message: String = "",
    var isDisplayed: Boolean = false,
    var isWarning: Boolean = false
)
