package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inputs.adb.ddmlib.Devices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.DeviceDetails2
import processor.MainProcessor
import ui.CustomTheme

@Composable
fun SideNavigation(
    processor: MainProcessor, sessionId: String, modifier: Modifier = Modifier,
    onSessionChange: (sessionId: String) -> Unit
) {
    var sessions by remember { mutableStateOf<List<String>>(arrayListOf()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(sessionId) {
        sessions = processor.getSessions()
    }
    Column(modifier) {
        Row(
            Modifier.padding(32.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(painterResource("icons/logo.png"), "goFlog", Modifier.size(40.dp))
            Text("GoFlog", fontSize = 16.sp)
        }
        Text(
            "Sessions", Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = CustomTheme.typography.headings.h3
        )

        Button(
            {
                processor.createNewSession()
                onSessionChange(processor.getCurrentSessionId())
            }, Modifier.fillMaxWidth(0.8f), elevation = ButtonDefaults.elevation(0.dp),
            shape = RoundedCornerShape(0, 50, 50, 0)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource("icons/ico-plus.svg"), "plus")
                Text("Start New Session", color = contentColorFor(MaterialTheme.colors.primary))
            }
        }

        SessionsList(sessions, processor, Modifier.fillMaxHeight(0.5f).padding(vertical = 16.dp), onSessionChange) {
            scope.launch {
                sessions = processor.getSessions()
            }
        }

        Divider(Modifier.height(1.dp).fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f)))

        Text(
            "Devices", Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = CustomTheme.typography.headings.h3
        )

        DeviceList(Modifier.fillMaxHeight().padding(vertical = 16.dp))
    }
}

@Composable
private fun DeviceList(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val devices by Devices.devicesFlow.collectAsState()
    val currentDeviceSelected by Devices.currentDeviceFlow.collectAsState()
    if (devices.isEmpty()) {
        scope.launch {
            Devices.setCurrentDevice(null)
        }
    } else if (devices.size == 1) {
        scope.launch {
            Devices.setCurrentDevice(devices.first())
        }
    }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(devices, { item: DeviceDetails2 -> item.serial }) {
            val shape = RoundedCornerShape(0, 50, 50, 0)
            var modifier1 = Modifier.clip(shape).clickable {
                Devices.setCurrentDevice(it)
            }.fillMaxWidth(0.8f)
            if (it.serial == currentDeviceSelected) {
                modifier1 = modifier1.background(
                    CustomTheme.colors.accent.copy(0.4f),
                    shape
                )
            }
            modifier1 = modifier1.padding(start = 24.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
            val stateColor = if (it.isOnline()) {
                CustomTheme.colors.alertColors.success
            } else {
                CustomTheme.colors.alertColors.danger
            }
            Column(modifier1, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(it.name)
                Text(it.stateText(), style = CustomTheme.typography.headings.caption, color = stateColor)
            }
        }
    }
}

@Composable
private fun SessionsList(
    sessions: List<String>,
    processor: MainProcessor,
    modifier: Modifier = Modifier,
    onSessionChange: (sessionId: String) -> Unit,
    onSessionDelete: () -> Unit
) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sessions, key = { item: String -> item }) {
            val currentSession = processor.getCurrentSessionId()
            val shape = RoundedCornerShape(0, 50, 50, 0)
            val isThisCurrentSession = currentSession == it
            var modifier1 = Modifier.clip(shape).clickable {
                processor.startOldSession(it)
                onSessionChange(processor.getCurrentSessionId())
            }.fillMaxWidth(0.8f)
            if (isThisCurrentSession) {
                modifier1 = modifier1.background(
                    CustomTheme.colors.accent.copy(0.4f),
                    shape
                )
            }
            modifier1 = modifier1.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(it, modifier1)
                IconButton({
                    processor.deleteSession(it)
                    onSessionDelete()
                }) {
                    Icon(painterResource("icons/ico_close.xml"), "delete session")
                }
            }
        }
    }
}