package com.ps.bluechat.navigation

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.ps.bluechat.R
import com.ps.bluechat.presentation.BluetoothViewModel
import com.ps.bluechat.presentation.change_name_screen.ChangeNameScreen
import com.ps.bluechat.presentation.change_name_screen.ChangeNameViewModel
import com.ps.bluechat.presentation.chat_screen.ChatScreen
import com.ps.bluechat.presentation.chat_screen.ChatViewModel
import com.ps.bluechat.presentation.device_screen.DeviceScreen

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
fun NavGraph(
    navController: NavHostController
) {
    val direction = remember(navController) { Direction(navController) }
    val context: Context = LocalContext.current
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val state by viewModel.state.collectAsState()

    AnimatedNavHost(navController = navController,
        startDestination = Screen.HomeScreen.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }) {
        composable(
            route = Screen.HomeScreen.route
        ) {
            DeviceScreen(
                direction = direction,
                state = state,
                onCreateBond = viewModel::createBond,
                onRemoveBond = viewModel::removeBond,
                onStartScan = viewModel::startScan,
                onStopScan = viewModel::stopScan,
                onBluetoothEnable = viewModel::enableBluetooth,
                onBluetoothDisable = viewModel::disableBluetooth,
                onDiscoverabilityEnable = viewModel::enableDiscoverability,
                onDiscoverabilityDisable = viewModel::disableDiscoverability,
                onStartConnecting = viewModel::connectToDevice,
                onStartServer = viewModel::observeIncomingConnections,
                clearErrorMessage = viewModel::clearErrorMessage
            )
        }
        composable(
            route = Screen.ChangeDeviceNameScreen.route
        ) {
            val changeNameViewModel = hiltViewModel<ChangeNameViewModel>()

            ChangeNameScreen(direction = direction,
                deviceName = state.deviceName ?: context.getString(R.string.no_name),
                onDeviceNameChange = {
                    changeNameViewModel.changeDeviceName(deviceName = it)
                })
        }

        composable(
            route = Screen.ChatScreen.route
        ) {

            val chatViewModel = hiltViewModel<ChatViewModel>()
            val chatState by chatViewModel.state.collectAsState()

            ChatScreen(
                direction = direction,
                state = chatState,
                onSendMessage = chatViewModel::sendMessage,
                onUriSelected = chatViewModel::sendImages,
                onDeleteAllMessages = chatViewModel::clearAllMessages,
                onDisconnect = viewModel::disconnectDevice
            )
        }
    }

}