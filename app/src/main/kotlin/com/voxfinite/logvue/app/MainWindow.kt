package com.voxfinite.logvue.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import com.voxfinite.logvue.adb.ddmlib.AdbHelper
import com.voxfinite.logvue.storage.Db
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.utils.AppLog
import com.voxfinite.logvue.utils.CustomExceptionHandler
import com.voxfinite.logvue.utils.SentryHelper
import java.awt.Desktop

@Composable
fun ApplicationScope.appWindow() {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
        desktop.setQuitHandler { e, response ->
            closeApp(e.source.toString())
            response.performQuit()
        }
    }
    val onCloseRequest = {
        closeApp("User Close")
        exitApplication()
    }
    Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
    SentryHelper.init()
    val windowState = rememberWindowState(WindowPlacement.Maximized)
    Window(onCloseRequest = onCloseRequest, title = CustomTheme.strings.appName, state = windowState) {
        App()
    }
}

fun closeApp(source: String) {
    AppLog.d("QuitHandler", "Quiting : $source")
    AdbHelper.close()
    Db.close()
}
