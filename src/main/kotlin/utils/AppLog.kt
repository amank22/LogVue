package utils

import io.sentry.Sentry
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

object AppLog {

    private val formatter = SimpleFormatter()

    fun d(tag: String, msg: String) {
        val s = formatter.format(LogRecord(Level.FINER, "$tag : $msg"))
        println(s)
    }

    fun d(msg: String) {
        d("Debug", msg)
    }
}

fun Throwable?.reportException() {
    if (this == null) {
        Sentry.captureException(UnsupportedOperationException("Throwable should not be null"))
        return
    }
    Sentry.captureException(this)
}
