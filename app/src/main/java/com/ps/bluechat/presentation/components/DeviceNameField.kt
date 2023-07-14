package com.ps.bluechat.presentation.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ps.bluechat.navigation.Direction
import com.ps.bluechat.presentation.BluetoothViewModel

@Composable
fun DeviceNameField(
    deviceName: String,
    modifier: Modifier = Modifier,
    direction: Direction
){
    val context: Context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = context.getString(com.ps.bluechat.R.string.device_name),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            )

        IconButton(onClick = { direction.navigateToChangeDeviceNameScreen() }) {
            Row(
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = deviceName,
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }

    }
}