package com.ps.bluechat.presentation.model

data class DialogState(
    val title: String = "",
    val description: String = "",
    val onConfirm: () -> Unit = {},
    val onCancel: () -> Unit = {}
)