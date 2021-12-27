package models

import com.malinskiy.adam.request.device.DeviceState

data class DeviceDetails(
    val serial: String,
    val name: String,
    val state: DeviceState,
) {
    fun isOnline() : Boolean {
        return (state == DeviceState.DEVICE)
    }

    fun stateText() : String {
        return if (isOnline()) "Connected" else "Offline"
    }
}
