package app

import androidx.compose.runtime.Composable
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
