package com.voxfinite.logvue.adb.ddmlib

import com.android.ddmlib.*
import com.android.ddmlib.logcat.LogCatMessageParser
import com.voxfinite.logvue.api.models.LogCatHeader2
import com.voxfinite.logvue.api.models.LogCatMessage2
import com.voxfinite.logvue.api.models.LogLevel2
import com.voxfinite.logvue.utils.reportException
import com.voxfinite.logvue.utils.to2
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.GuardedBy

class LogCatRunner(
    val mDevice: IDevice,
    pid: Long,
    filters: List<String>
) {

    companion object {
        private const val DEVICE_POLL_INTERVAL_MSEC = 1000

        private val sDeviceDisconnectedMsg: LogCatMessage2 = newLogCatMessage("Device disconnected: 1")

        private val sConnectionTimeoutMsg: LogCatMessage2 = newLogCatMessage("LogCat Connection timed out")

        private val sConnectionErrorMsg: LogCatMessage2 = newLogCatMessage("LogCat Connection error")

        private fun newLogCatMessage(message: String): LogCatMessage2 {
            return LogCatMessage2(
                LogCatHeader2(
                    LogLevel2.ERROR, -1, -1, "", "",
                    Instant.EPOCH
                ), message
            )
        }
    }

    private val mParser = LogCatMessageParser()

    private val mCancelled = AtomicBoolean(false)
    private val mReceiver = LogCatOutputReceiver()

    // TODO: Check if filters is empty then show all logs
    private val logcatCommand = "logcat -s ${filters.joinToString(" ")} --pid=$pid -v long"

    @GuardedBy("this")
    private val mListeners = hashSetOf<LogCatListener2>()

    fun run() {
        // wait while device comes online
        while (!mDevice.isOnline) {
            try {
                Thread.sleep(DEVICE_POLL_INTERVAL_MSEC.toLong())
            } catch (e: InterruptedException) {
                return
            }
        }
        try {
            mDevice.executeShellCommand(logcatCommand, mReceiver, 0, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            notifyListeners(arrayListOf(sConnectionTimeoutMsg))
        } catch (ignored: AdbCommandRejectedException) {
            ignored.reportException()
            // will not be thrown as long as the shell supports logcat
        } catch (ignored: ShellCommandUnresponsiveException) {
            // this will not be thrown since the last argument is 0
            ignored.reportException()
        } catch (e: IOException) {
            notifyListeners(arrayListOf(sConnectionErrorMsg))
        }
        notifyListeners(arrayListOf(sDeviceDisconnectedMsg))
    }

    fun stop() {
        mCancelled.set(true)
    }

    private inner class LogCatOutputReceiver : MultiLineReceiver() {
        init {
            setTrimLine(false)
        }

        /** Implements [IShellOutputReceiver.isCancelled].  */
        override fun isCancelled(): Boolean {
            return mCancelled.get()
        }

        override fun processNewLines(lines: Array<String>) {
            if (!mCancelled.get()) {
                processLogLines(lines)
            }
        }

        private fun processLogLines(lines: Array<String>) {
            val newMessages: List<LogCatMessage2> = mParser.processLogLines(lines, mDevice).map { it.to2() }
            if (newMessages.isNotEmpty()) {
                notifyListeners(arrayListOf<LogCatMessage2>().also { logCatMessage2s ->
                    logCatMessage2s.addAll(
                        newMessages
                    )
                })
            }
        }
    }

    @Synchronized
    fun addLogCatListener(l: LogCatListener2) {
        mListeners.add(l)
    }

    @Synchronized
    fun removeLogCatListener(l: LogCatListener2) {
        mListeners.remove(l)
    }

    @Synchronized
    private fun notifyListeners(messages: ArrayList<LogCatMessage2>) {
        for (l in mListeners) {
            l.log(messages)
        }
    }
}
