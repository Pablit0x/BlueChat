package com.ps.bluechat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeToggleField(
    modifier: Modifier = Modifier,
    modeName : String,
    isOn: Boolean,
    onEnable : () -> Unit,
    onDisable: () -> Unit
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = modeName,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )

        Switch(
            modifier = modifier.scale(1.5f),
            checked = isOn,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.secondaryVariant,
                checkedTrackColor = MaterialTheme.colors.primary,
                checkedTrackAlpha = 0.6f
            ),
            onCheckedChange = { currentState ->
                if(currentState) onEnable() else onDisable()
            }
        )
    }
}