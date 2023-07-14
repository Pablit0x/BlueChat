package com.ps.bluechat.presentation.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ps.bluechat.R

@Composable
fun BluetoothToggleField(
    modifier: Modifier = Modifier,
    isBluetoothOn: Boolean,
    onBluetoothEnable : () -> Unit,
    onBluetoothDisable: () -> Unit
){
    val context: Context = LocalContext.current


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = context.getString(R.string.bluetooth),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )

        Switch(
            modifier = modifier.scale(1.5f),
            checked = isBluetoothOn,
            onCheckedChange = { currentState ->
                if(currentState) onBluetoothEnable() else onBluetoothDisable()
            }
        )
    }
}