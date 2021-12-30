package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import inputs.adb.ddmlib.AdbHelper
import inputs.adb.ddmlib.Devices
import models.DeviceDetails2
import models.SessionInfo
import ui.CustomTheme

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NewSessionBox(onDismissRequest: () -> Unit, onButtonClick: (sessionInfo: SessionInfo) -> Unit) {
    StyledCustomVerticalDialog(onDismissRequest = onDismissRequest) {
        Column(Modifier.fillMaxHeight().padding(16.dp)) {
            Text("Create Session", style = CustomTheme.typography.headings.h2)
            var description by remember { mutableStateOf(TextFieldValue()) }
            var dIsError by remember { mutableStateOf(false) }
            var appName by remember { mutableStateOf(TextFieldValue()) }
            val device by Devices.currentDeviceFlow.collectAsState()
            var clients by remember { mutableStateOf(deviceClients(device)) }
            var submitError by remember { mutableStateOf("") }
            val dSub = "Keep it short & something you can remember (Max ${SessionInfo.DESC_MAX_LENGTH} characters)"
            Spacer(Modifier.height(24.dp))
            CustomTextBox(description, "Session description", dSub, dIsError) {
                description = it
                dIsError = it.text.length > SessionInfo.DESC_MAX_LENGTH
            }
            Spacer(Modifier.height(16.dp))
            CustomTextBox(appName, "App package name", "Package name should be exact to avoid any inconsistencies") {
                appName = it
                clients = clients.filterPackages(appName.text)
            }
            if (clients.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Device packages:", style = CustomTheme.typography.headings.h6)
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically, contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(clients, { item -> item }) {
                        Chip(
                            it,
                            Modifier.clip(CustomTheme.shapes.small)
                                .clickable { appName = TextFieldValue(it, TextRange(it.length)) })
                    }
                }
            }
            val iDevice = device?.device
            if (iDevice != null) {
                LaunchedEffect(device?.serial) {
                    AdbHelper.getPackages(iDevice) {
                        clients = it.filterPackages(appName.text)
                    }
                }
            }

            // error view
            if (submitError.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    submitError,
                    Modifier.fillMaxWidth()
                        .background(
                            CustomTheme.colors.alertColors.danger,
                            CustomTheme.shapes.small
                        ).padding(horizontal = 8.dp, vertical = 2.dp),
                    style = CustomTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(16.dp))
            Button({
                if (description.text.length > SessionInfo.DESC_MAX_LENGTH) {
                    submitError = "Description should be less than ${SessionInfo.DESC_MAX_LENGTH} characters"
                    return@Button
                }
                if (description.text.isBlank()) {
                    submitError = "Description should not be empty"
                    return@Button
                }
                if (appName.text.isBlank()) {
                    submitError = "Package name should not be empty"
                    return@Button
                }
                onButtonClick(SessionInfo(description.text, appName.text))
            }) {
                Icon(painterResource("icons/ico-plus.svg"), "Add session")
                Text("Create session")
            }
        }
    }
}

private fun List<String>.filterPackages(appName: String) = this.filter { packageName ->
    (appName.isBlank() || (appName.isNotBlank() && packageName.contains(appName)))
}

private fun deviceClients(device: DeviceDetails2?) = device
    ?.device
    ?.clients
    ?.filterNotNull()
    ?.map { it.clientData.packageName }
    ?.filter { !it.isNullOrBlank() } ?: emptyList()

@Composable
private fun CustomTextBox(
    text: TextFieldValue,
    placeholder: String,
    subtext: String,
    isError: Boolean = false,
    onValueChange: (TextFieldValue) -> Unit
) {
    OutlinedTextField(text, onValueChange, Modifier.fillMaxWidth(), placeholder = {
        Text(placeholder)
    }, singleLine = true, isError = isError, shape = CustomTheme.shapes.medium)
    Text(subtext, Modifier.padding(start = 8.dp, top = 4.dp), style = CustomTheme.typography.headings.caption)
}