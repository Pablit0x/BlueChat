package com.ps.bluechat.presentation.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ps.bluechat.R
import com.ps.bluechat.domain.chat.ScanningState


@ExperimentalMaterialApi
@Composable
fun BluetoothActionSelector(
    scanningState: ScanningState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartServer: () -> Unit
) {
    val context: Context = LocalContext.current
    var isMenuExtended by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(visible = isMenuExtended, modifier = Modifier.padding(bottom = 16.dp)) {
            Column {

                when(scanningState){
                    ScanningState.DISCOVERING -> {
                        MenuActionButton(
                            icon = Icons.Default.Pause,
                            description = context.getString(R.string.stop_scan),
                            onClickAction = {
                                onStopScan()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    ScanningState.NOT_DISCOVERING -> {
                        MenuActionButton(
                            icon = Icons.Default.PlayArrow,
                            description = context.getString(R.string.start_scan),
                            onClickAction = {
                                onStartScan()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    else -> {}
                }


                MenuActionButton(
                    icon = Icons.Default.BluetoothSearching,
                    description = context.getString(R.string.start_server),
                    onClickAction = {
                        onStartServer()
                        isMenuExtended = !isMenuExtended
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
        IconButton(onClick = { isMenuExtended = !isMenuExtended }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "",
                tint = MaterialTheme.colors.onSecondary,
                modifier = Modifier
                    .size(54.dp)
                    .background(MaterialTheme.colors.secondary, shape = CircleShape)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun MenuActionButton(
    icon: ImageVector, description: String, onClickAction: () -> Unit, modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            onClickAction()
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {

            Text(text = description)

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colors.onSecondary,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colors.secondary, shape = CircleShape
                    )
                    .padding(8.dp)
            )
        }
    }
}
