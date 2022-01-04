package inputs.adb.ddmlib

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import kotlinx.coroutines.flow.MutableStateFlow
import models.DeviceDetails2

class Devices : AndroidDebugBridge.IDeviceChangeListener {

    companion object {
        private val _devicesFlow: MutableStateFlow<List<DeviceDetails2>> = MutableStateFlow(emptyList())
        val devicesFlow: MutableStateFlow<List<DeviceDetails2>> = _devicesFlow
        private val devices: HashSet<DeviceDetails2> = hashSetOf()
        private val _currentDeviceFlow: MutableStateFlow<DeviceDetails2?> = MutableStateFlow(null)

        val currentDeviceFlow = _currentDeviceFlow
        val currentDevice
            get() = currentDeviceFlow.value

        fun setCurrentDevice(serial: DeviceDetails2?) {
            _currentDeviceFlow.value = serial
        }
    }

    override fun deviceConnected(device: IDevice) {
        val details2 = DeviceDetails2(device)
        devices.add(details2)
        _devicesFlow.value = currentDevices()
    }

    override fun deviceDisconnected(device: IDevice) {
        val details2 = DeviceDetails2(device)
        devices.remove(details2)
        _devicesFlow.value = currentDevices()
    }

    override fun deviceChanged(device: IDevice, changeMask: Int) {
        var details2 = DeviceDetails2(device)
        devices.remove(details2)
        details2 = DeviceDetails2(device)
        devices.add(details2)
        _devicesFlow.value = currentDevices()
    }

    private fun currentDevices() = devices.toList().sortedBy { it.sortKey() }
}
