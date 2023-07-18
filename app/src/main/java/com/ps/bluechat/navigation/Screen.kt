package com.ps.bluechat.navigation

import com.ps.bluechat.util.Constants

sealed class Screen(val route : String){
    object HomeScreen : Screen(Constants.HOME_SCREEN)
    object ChangeDeviceNameScreen : Screen(Constants.CHANGE_DEVICE_NAME_SCREEN)
    object ChatScreen : Screen(Constants.CHAT_SCREEN)
}
