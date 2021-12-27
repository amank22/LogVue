package inputs.adb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Serializable


data class LoggerCommand(val packageName: String) : AdbCommand, Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    private var process: ReceiveChannel<Result<String>>? = null
    private val logCommand =
        "adb shell setprop log.tag.FA VERBOSE && adb shell setprop log.tag.FA-SVC VERBOSE &&" +
                " pid=\$(adb shell pidof -s $packageName) && adb logcat -s FA FA-SVC --pid=\$pid -v long time"

    suspend fun stream(scope: CoroutineScope): ReceiveChannel<Result<String>> {
        val stream = SimpleAdbProcess().stream(scope, createCommands())
        process = stream
        return stream
    }

    private fun createCommands(): Array<String> {
        val currentDevice = AdbUtils.currentDeviceFlow.value
        val deviceParam = if (currentDevice.isNullOrBlank()) {
            ""
        } else {
            "-s $currentDevice"
        }
        return arrayOf(
            "adb $deviceParam shell setprop log.tag.FA VERBOSE && adb $deviceParam shell setprop log.tag.FA-SVC VERBOSE &&" +
                    " pid=\$(adb $deviceParam shell pidof -s $packageName) && adb $deviceParam logcat -s FA FA-SVC --pid=\$pid -v long time"
        )
    }

    fun close() {
        process?.cancel()
    }

}