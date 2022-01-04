package models

import com.android.ddmlib.IDevice

class DeviceDetails2(
    val device: IDevice
) {

    val serial: String = device.serialNumber
    val name: String = device.getProperty("ro.product.device") ?: device.name

    fun isOnline(): Boolean {
        return device.isOnline
    }

    fun stateText(): String {
        return if (isOnline()) "Connected" else "Offline"
    }

    fun sortKey(): String {
        return (if (isOnline()) 0 else 1).toString() + serial
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceDetails2) return false
        if (serial != other.serial) return false
        return true
    }

    override fun hashCode(): Int {
        return serial.hashCode()
    }

}
