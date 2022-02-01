package com.voxfinite.logvue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.voxfinite.logvue.adb.ddmlib.Devices
import kotlinx.coroutines.launch
import com.voxfinite.logvue.processor.MainProcessor
import com.voxfinite.logvue.ui.CustomTheme
import com.voxfinite.logvue.ui.components.common.AppLogo

@Composable
fun SideNavigation(
    processor: MainProcessor,
    sessionId: String?,
    modifier: Modifier = Modifier,
    onSessionChange: (sessionId: String?) -> Unit
) {
    Column(modifier) {
        AppLogo(Modifier.fillMaxWidth(0.8f).padding(24.dp))
        SideNavHeader("Sessions")

        SessionsBox(sessionId, processor, onSessionChange)

        Divider(Modifier.height(1.dp).fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f)))

        val devices by Devices.devicesFlow.collectAsState()
        val deviceHeader = if (devices.isEmpty()) "No Devices" else "Devices"
        SideNavHeader(deviceHeader)

        DeviceList(devices, Modifier.fillMaxHeight().padding(vertical = 16.dp))
    }
}

@Composable
private fun SessionsBox(
    sessionId: String?,
    processor: MainProcessor,
    onSessionChange: (sessionId: String?) -> Unit
) {
    var createSessionBoxShown by remember { mutableStateOf(false) }
    var sessions by remember { mutableStateOf<List<String>>(arrayListOf()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(sessionId) {
        sessions = processor.getSessions()
    }
    CreateSessionButton {
        createSessionBoxShown = true
    }
    if (createSessionBoxShown) {
        NewSessionBox({ createSessionBoxShown = false }) {
            processor.createNewSession(it)
            createSessionBoxShown = false
            onSessionChange(processor.getCurrentSessionId())
        }
    }

    if (sessions.isEmpty()) {
        EmptySession()
    } else {
        SessionsList(sessions, processor, Modifier.fillMaxHeight(0.5f).padding(vertical = 16.dp), onSessionChange) {
            scope.launch {
                sessions = processor.getSessions()
            }
        }
    }
}

@Composable
private fun SideNavHeader(header: String) {
    Text(
        header, Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        style = CustomTheme.typography.headings.h3
    )
}
