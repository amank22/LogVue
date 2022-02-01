package com.voxfinite.logvue.api.models

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

private val EPOCH_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.INSTANT_SECONDS)
    .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
    .toFormatter(Locale.ROOT)

data class LogCatHeader2(
    val logLevel: LogLevel2,
    val pid: Int,
    val tid: Int,
    val appName: String,
    val tag: String,
    val timestamp: Instant,
) {

    override fun toString(): String {
        val epoch = EPOCH_TIME_FORMATTER.format(timestamp)
        val priority = logLevel.priorityLetter
        return "$epoch: $priority/$tag($pid:$tid) $appName"
    }
}
