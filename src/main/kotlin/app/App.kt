package app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import inputs.adb.ddmlib.AdbHelper
import processor.MainProcessor
import storage.Db
import ui.AppTheme
import ui.CustomTheme
import ui.components.BodyPanel
import ui.components.SideNavigation
import ui.components.dialogs.CrashDialog
import ui.components.dialogs.IntroDialog
import utils.AppSettings
import utils.CustomExceptionHandler
import utils.Helpers

@Composable
@Preview
fun App() {
    val processor = remember { MainProcessor() }
    val isLightTheme by Helpers.isThemeLightMode.collectAsState()
    AppTheme(isLightTheme) {
        Row(Modifier.fillMaxSize().background(CustomTheme.colors.background)) {
            var sessionId by remember { mutableStateOf(Db.sessionId()) }
            SideNavigation(
                processor, sessionId, Modifier.fillMaxHeight().weight(0.2f)
                    .background(CustomTheme.colors.componentBackground)
            ) {
                sessionId = it.orEmpty()
            }
            Divider(Modifier.fillMaxHeight().width(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
            BodyPanel(processor, sessionId, Modifier.fillMaxHeight().weight(0.8f))
        }
        LaunchedEffect(Unit) {
            AdbHelper.init()
        }
        LaunchIntroIfNeeded()
        LaunchCrashDialogIfNeeded()
    }
}

@Composable
fun LaunchIntroIfNeeded() {
    var introLaunched by remember { mutableStateOf(AppSettings.getFlag("isIntroLaunched")) }
    if (!introLaunched) {
        IntroDialog {
            AppSettings.setFlag("isIntroLaunched", true)
            introLaunched = true
        }
    }
}

@Composable
fun LaunchCrashDialogIfNeeded() {
    if (!CustomExceptionHandler.isLastTimeCrashed()) return
    var launched by remember { mutableStateOf(true) }
    if (launched) {
        CrashDialog {
            launched = false
        }
    }
}