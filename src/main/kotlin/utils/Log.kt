package utils

import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter

object Log {

    private val formatter = SimpleFormatter()

    fun d(tag: String, msg: String) {
        val s = formatter.format(LogRecord(Level.FINER, "$tag : $msg"))
        println(s)
    }

    fun d(msg: String) {
        d("Debug", msg)
    }
}
