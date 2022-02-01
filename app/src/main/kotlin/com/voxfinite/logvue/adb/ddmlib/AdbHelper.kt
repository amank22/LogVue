package com.voxfinite.logvue.adb.ddmlib

import com.android.ddmlib.AdbInitOptions
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.android.ddmlib.Log
import com.voxfinite.logvue.adb.LogErrorDeviceNotConnected
import com.voxfinite.logvue.adb.LogErrorPackageIssue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.voxfinite.logvue.api.models.LogCatMessage2
import com.voxfinite.logvue.utils.Either
import com.voxfinite.logvue.utils.reportException
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object AdbHelper {

    private var bridge: AndroidDebugBridge? = null
    private const val PACKAGES_COMMAND = "pm list packages -3 -e"
    // list of lines with format : package:com.ea.games.r3_row

    private const val ADB_TIMEOUT = 10L

    fun init() {
        val options = AdbInitOptions.builder().setClientSupportEnabled(true).build()
        AndroidDebugBridge.init(options)
        AndroidDebugBridge.addDeviceChangeListener(Devices())
        val adbPath = adbPath()
        bridge = if (!adbPath.isNullOrBlank()) {
            AndroidDebugBridge.createBridge(adbPath, false, ADB_TIMEOUT, TimeUnit.SECONDS)
        } else {
            AndroidDebugBridge.createBridge(ADB_TIMEOUT, TimeUnit.SECONDS)
        }
        AndroidDebugBridge.addDebugBridgeChangeListener {
            bridge = it
        }
        AndroidDebugBridge.addClientChangeListener { client, changeMask ->
            Log.d("ClientChange", "$client : $changeMask")
        }
    }

    fun close() {
        try {
            AndroidDebugBridge.terminate()
        } catch (e: Exception) {
            e.reportException()
        }
    }

    private var stopLogs = false

    @OptIn(ExperimentalCoroutinesApi::class)
    fun monitorLogs(
        packageName: String,
        filters: Array<String> = arrayOf("FA", "FA-SVC", "PDTLogging")
    ) = callbackFlow {
        stopLogs = false
        val currentSelectedDevice = Devices.currentDevice?.device
        if (currentSelectedDevice == null || !currentSelectedDevice.isOnline) {
            send(Either.Left(LogErrorDeviceNotConnected))
            awaitClose()
            return@callbackFlow
        }
        currentSelectedDevice.emptyShellCommand("setprop log.tag.FA VERBOSE")
        currentSelectedDevice.emptyShellCommand("setprop log.tag.FA-SVC VERBOSE")
        val client = currentSelectedDevice.getClient(packageName)
        var clientPid = -1
        if (client == null) {
            currentSelectedDevice.executeShellCommand("pidof -s $packageName",
                SingleValueReceiver {
                    clientPid = it.toIntOrNull() ?: -1
                })
        } else {
            clientPid = client.clientData.pid
        }
        if (clientPid < 0) {
            send(Either.Left(LogErrorPackageIssue))
            close()
            awaitClose()
            return@callbackFlow
        }
        val logTask = LogCatRunner(currentSelectedDevice, clientPid.toLong(), filters)
        val listener: (msgList: ArrayList<LogCatMessage2>) -> Unit = {
            if (stopLogs) {
                close()
            } else if (isActive) {
                trySend(Either.Right(it))
            }
        }
        logTask.addLogCatListener(listener)
        thread {
            logTask.run()
        }
        awaitClose {
            logTask.removeLogCatListener(listener)
            logTask.stop()
        }
    }.buffer(capacity = Channel.UNLIMITED).cancellable()

    fun closeLogs() {
        stopLogs = true
    }

    suspend fun getPackages(device: IDevice, onValue: (packages: List<String>) -> Unit) = withContext(Dispatchers.IO) {
        device.executeShellCommand(
            PACKAGES_COMMAND, PackagesReceiver(onValue),
            ADB_TIMEOUT, TimeUnit.SECONDS
        )
    }

    private fun IDevice.emptyShellCommand(command: String) {
        executeShellCommand(
            command,
            EmptyReceiver, ADB_TIMEOUT, TimeUnit.SECONDS
        )
    }

    fun adbPath(): String? {
        val androidEnvHome: File? = try {
            System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        } catch (e: SecurityException) {
            e.reportException()
            null
        }?.let { File(it) }

        val os = System.getProperty("os.name").lowercase(Locale.ENGLISH)
        val adbBinaryName = when {
            os.contains("win") -> {
                "adb.exe"
            }
            else -> "adb"
        }

        val adb = androidEnvHome?.let { File(it, "platform-tools" + File.separator + adbBinaryName) }
            ?: return null
        if (!adb.isFile) return null
        return adb.absolutePath
    }
}
