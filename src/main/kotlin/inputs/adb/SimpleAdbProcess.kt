package inputs.adb

import com.github.pgreze.process.Redirect
import com.github.pgreze.process.process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import utils.Log
import java.io.Serializable

class SimpleAdbProcess : Serializable {

    companion object {
        private const val serialVersionUID = 1L
        private val WIN_RUNTIME = arrayOf("cmd.exe", "/C")
        private val OS_LINUX_RUNTIME = arrayOf("/bin/bash", "-l", "-c")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun stream(scope: CoroutineScope, commands: Array<String>): ReceiveChannel<Result<String>> {
        val isLinux = !(System.getProperty("os.name").contains("Windows"))
        val baseCommand = if (isLinux) OS_LINUX_RUNTIME else WIN_RUNTIME
        val allCommands = (baseCommand + commands)
        return scope.produce {
            if (AdbUtils.currentDeviceFlow.value.isNullOrBlank()) {
                send(Result.failure(LogErrorDeviceNotConnected))
                return@produce
            }
            var errorConsumed = false
            val result = process(*allCommands, stdout = Redirect.Consume { flow: Flow<String> ->
                flow.collect {
                    yield()
                    send(Result.success(it))
                }
            },
            stderr = Redirect.Consume { flow: Flow<String> ->
                errorConsumed = true
                val error = flow.map {
                    yield()
                    it
                }.firstOrNull()
                Log.d("ConsoleError", "Some error = $error")
                if (error.isNullOrBlank()) {
                    send(Result.failure<String>(LogErrorPackageIssue))
                } else {
                    send(Result.failure<String>(LogErrorUnknown(error)))
                }
            }) {
                send(Result.success(it))
            }.resultCode
            println("Result Code of simple process : $result")
            if (!errorConsumed && result != 0) {
                send(Result.failure(LogErrorUnknown("There are some errors in getting logs")))
            }
            flushLogs(baseCommand)
            awaitClose {
                scope.launch {
                    flushLogs(baseCommand)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun flushLogs(baseCommand: Array<String>) {
        try {
            Log.d("Logcat Clear", "Clearing logs")
            process(*baseCommand, "adb logcat -c")
        } catch (e: Exception) {
            Log.d("Logcat Clear", "Error: ${e.message}")
        }
    }

}