package com.voxfinite.logvue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.voxfinite.logvue.adb.ddmlib.Devices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.voxfinite.logvue.models.DeviceDetails2
import com.voxfinite.logvue.ui.CustomTheme

@Composable
fun DeviceList(devices: List<DeviceDetails2>, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
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
            if (it == currentDeviceSelected) {
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
