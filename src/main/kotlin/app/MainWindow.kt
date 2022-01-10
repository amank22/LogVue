package app

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import inputs.adb.ddmlib.AdbHelper
import storage.Db
import ui.CustomTheme
import utils.AppLog
import utils.CustomExceptionHandler
import utils.SentryHelper
import java.awt.Desktop

@Composable
fun ApplicationScope.appWindow() {
    Desktop.getDesktop().setQuitHandler { e, response ->
        closeApp(e.source.toString())
        response.performQuit()
    }
    val onCloseRequest = {
        closeApp("User Close")
        exitApplication()
    }
    Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
    SentryHelper.init()
    val windowState = rememberWindowState(WindowPlacement.Floating, size = DpSize(1440.dp, 1024.dp))
    Window(onCloseRequest = onCloseRequest, title = CustomTheme.strings.appName, state = windowState) {
        App()
    }
}

fun closeApp(source: String) {
    AppLog.d("QuitHandler", "Quiting : $source")
    AdbHelper.close()
    Db.close()
}