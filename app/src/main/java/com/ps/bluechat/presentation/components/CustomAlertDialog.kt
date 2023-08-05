package com.ps.bluechat.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ps.bluechat.R

@Composable
fun CustomAlertDialog(
    onCloseDialog: () -> Unit, title: String, description: String, onConfirmAction: () -> Unit
) {
    AlertDialog(onDismissRequest = {
        onCloseDialog()
    }, title = {
        Text(text = title, fontWeight = FontWeight.SemiBold)
    }, text = {
        Text(text = description)
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmAction()
            onCloseDialog()
        }) {
            Text(
                stringResource(id = R.string.confirm), color = MaterialTheme.colors.onBackground
            )
        }
    }, dismissButton = {
        TextButton(onClick = {
            onCloseDialog()
        }) {
            Text(
                stringResource(id = R.string.cancel), color = MaterialTheme.colors.onBackground
            )
        }
    }, modifier = Modifier
        .padding(16.dp)
        .gradientSurface()
        .border(
            1.dp, color = MaterialTheme.colors.onBackground, shape = RoundedCornerShape(20)
        )
    )
}