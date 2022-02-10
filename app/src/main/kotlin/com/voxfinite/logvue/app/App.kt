package com.voxfinite.logvue.app

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
import com.voxfinite.logvue.adb.ddmlib.AdbHelper
import com.voxfinite.logvue.processor.MainProcessor
import com.voxfinite.logvue.storage.Db
import com.voxfinite.logvue.ui.AppTheme
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.ui.components.BodyPanel
import com.voxfinite.logvue.ui.components.SideNavigation
import com.voxfinite.logvue.ui.components.dialogs.CrashDialog
import com.voxfinite.logvue.ui.components.dialogs.IntroDialog
import com.voxfinite.logvue.utils.*
import com.voxfinite.logvue.utils.plugins.PluginsHelper

@Composable
@Preview
fun App() {
    val processor = remember { MainProcessor() }
    val isLightTheme by Helpers.isThemeLightMode.collectAsState()
    LaunchedEffect(Unit) {
        PluginsHelper.load()
    }
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
