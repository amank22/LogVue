package models

import com.android.ddmlib.logcat.LogCatMessage
import java.io.Serializable

data class LogCatMessage2(val header: LogCatHeader2, val message: String) : Serializable {

    constructor(log: LogCatMessage) : this(LogCatHeader2(log.header), log.message)

    companion object {
        private const val serialVersionUID = 1L
    }

    override fun toString(): String {
        return "$header: $message"
    }
}
