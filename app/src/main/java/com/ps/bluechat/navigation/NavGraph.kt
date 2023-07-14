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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.ps.bluechat.R
import com.ps.bluechat.presentation.BluetoothViewModel
import com.ps.bluechat.presentation.components.ChangeNameScreen
import com.ps.bluechat.presentation.components.DeviceScreen

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalFoundationApi
fun NavGraph(
    navController: NavHostController
) {
    val direction = remember(navController) { Direction (navController) }
    val context : Context = LocalContext.current
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val state by viewModel.state.collectAsState()

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(
            route = Screen.HomeScreen.route
        ) {
            DeviceScreen(
                direction = direction,
                state = state,
                onStartScan = viewModel::startScan,
                onStopScan = viewModel::stopScan,
                onBluetoothEnable = viewModel::enableBluetooth,
                onBluetoothDisable = viewModel::disableBluetooth
            )
        }
        composable(
            route = Screen.ChangeDeviceNameScreen.route
        ){
            ChangeNameScreen(
                direction = direction,
                deviceName = state.deviceName ?: context.getString(R.string.no_name),
                onDeviceNameChange = {newName -> viewModel.changeDeviceName(newName = newName)}
            )
        }
    }

}