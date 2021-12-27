package inputs.adb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import java.io.Serializable


data class LoggerCommandAdam(val packageName: String) : AdbCommand, Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    private var process: ReceiveChannel<String>? = null
    private val logCommand =
        "adb shell setprop log.tag.FA VERBOSE && adb shell setprop log.tag.FA-SVC VERBOSE &&" +
                " pid=\$(adb shell pidof -s $packageName) && adb logcat -s FA FA-SVC --pid=\$pid -v long time"

    suspend fun init(scope: CoroutineScope): Result<ReceiveChannel<String>> {
        val monitorLogs = AdbUtils.monitorLogs(scope, packageName)
        process = monitorLogs.getOrNull()
        return monitorLogs
    }

    suspend fun stream(onNewLog : (msg : String?) -> Unit) {
//        process?.let {
//            for (e in it) {
//                onNewLog(e)
//            }
//        }
        process?.consumeAsFlow()?.collect {
//            println("logs: $it")
            onNewLog(it)
        }
    }

    fun close() {
        process?.cancel()
    }

}