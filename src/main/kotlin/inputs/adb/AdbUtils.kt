package inputs.adb

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.exception.RequestRejectedException
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.logcat.LogcatFilterSpec
import com.malinskiy.adam.request.logcat.LogcatReadMode
import com.malinskiy.adam.request.logcat.LogcatVerbosityLevel
import com.malinskiy.adam.request.pkg.Package
import com.malinskiy.adam.request.pkg.PmListRequest
import com.malinskiy.adam.request.prop.GetSinglePropRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import models.DeviceDetails

object AdbUtils {

    private val adb by lazy { getClient() }

    private val _currentDeviceFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    private val currentDevice: String?
        get() = _currentDeviceFlow.value

    val currentDeviceFlow = _currentDeviceFlow

    private const val CMD_ENABLE_FA = "setprop log.tag.FA VERBOSE"
    private const val CMD_ENABLE_FA_SVC = "setprop log.tag.FA-SVC VERBOSE"
    private const val CMD_GET_PID = "pidof -s %s"

    suspend fun startServer() {
        //Start the adb server
        StartAdbInteractor().execute()
    }

    private fun getClient(): AndroidDebugBridgeClient {
        //Create adb client
        return AndroidDebugBridgeClientFactory().build()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun monitorDevices(scope: CoroutineScope): Flow<List<DeviceDetails>> {
        val deviceEventsChannel: ReceiveChannel<List<Device>> = adb.execute(
            request = AsyncDeviceMonitorRequest(),
            scope = scope
        )
        return deviceEventsChannel.receiveAsFlow().distinctUntilChanged()
            .mapLatest {
                val list = arrayListOf<DeviceDetails>()
                it.forEach { device ->
                    val name = getDeviceProperty(device.serial, "ro.product.device")
                    val dd = DeviceDetails(serial = device.serial, name = name, state = device.state)
                    list.add(dd)
                }
                list
            }
    }

    fun setCurrentDevice(serial: String?) {
        _currentDeviceFlow.value = serial
    }

    suspend fun getDeviceProperty(deviceSerial: String, property: String): String {
        return adb.execute(
            request = GetSinglePropRequest(name = property),
            serial = deviceSerial
        ).trim()
    }

    suspend fun getAllPackages(includePath: Boolean = false): List<Package> {
        return adb.execute(
            request = PmListRequest(includePath = includePath),
            serial = currentDevice
        )
    }

    /**
     * Monitor logs of specific package id or null if no package is selected
     */
    suspend fun monitorLogs(
        scope: CoroutineScope, packageId: String?,
        filterList: List<String> = listOf("FA", "FA-SVC")
    ): Result<ReceiveChannel<String>> {
        try {
            val logsEnabled = shellCommand(CMD_ENABLE_FA) successWith shellCommand(CMD_ENABLE_FA_SVC)
            if (!logsEnabled) return Result.failure(LogErrorNotEnabledForFA)
            val request = if (packageId.isNullOrBlank()) {
                StreamingLogcatRequest()
            } else {
                // "adb shell setprop log.tag.FA VERBOSE && adb shell setprop log.tag.FA-SVC VERBOSE &&" +
                // " pid=\$(adb shell pidof -s $packageName) && adb logcat -s FA FA-SVC --pid=\$pid -v long time"
                val filters = filterList.map { LogcatFilterSpec(it, LogcatVerbosityLevel.D) }
                val pid = shellCommand(String.format(CMD_GET_PID, packageId)).stdout.trim().toLongOrNull()
                    ?: return Result.failure(LogErrorPackageIssue)
                StreamingLogcatRequest(
                    pid = pid, modes = listOf(LogcatReadMode.long),
                    filters = filters
                )
            }
            val receiveChannel = adb.execute(
                request = request,
                scope = scope,
                serial = currentDevice
            )
            return Result.success(receiveChannel)
        } catch (e: RequestRejectedException) {
            return Result.failure(LogErrorADBIssue)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun shellCommand(cmd: String): ShellCommandResult {
        return adb.execute(
            request = ShellCommandRequest(cmd),
            serial = currentDevice
        )
    }

    private infix fun ShellCommandResult.successWith(second: ShellCommandResult): Boolean {
        return (this.exitCode == 0 && second.exitCode == 0)
    }

}