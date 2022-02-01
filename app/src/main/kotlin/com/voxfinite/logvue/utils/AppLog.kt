package com.voxfinite.logvue.utils

import io.sentry.Sentry
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

object AppLog {

    private val formatter = SimpleFormatter()

    fun d(tag: String, msg: String) {
        val s = formatter.format(LogRecord(Level.INFO, "$tag : $msg"))
        Logger.getGlobal().log(Level.INFO, s)
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
    if (!Sentry.isEnabled()) {
        Logger.getGlobal().log(Level.WARNING, message)
        return
    }
    Sentry.captureException(this)
}
