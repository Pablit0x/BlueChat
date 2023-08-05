package com.ps.bluechat.presentation

data class ToastUiState(
    var message: String = "",
    var isDisplayed: Boolean = false,
    var isWarning: Boolean = false
)
