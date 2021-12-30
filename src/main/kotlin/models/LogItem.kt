package models

import androidx.compose.ui.text.AnnotatedString
import utils.Helpers
import java.io.Serializable
import javax.annotation.concurrent.GuardedBy

data class LogItem(
    val source: ItemSource,
    val eventName: String,
    val properties: HashMap<String, Any> = hashMapOf(),
    val localTime: Long = System.currentTimeMillis(),
    val internalContent: InternalContent? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L

        val NoContent = LogItem(SourceInternalContent, "No Logs", internalContent = NoLogsContent)

    }

    @Transient
    var _propertiesAString: AnnotatedString? = null

    @Transient
    private val lock = true

    val propertiesAString: AnnotatedString
        @GuardedBy("lock")
        get() {
            if (_propertiesAString == null) {
                synchronized(lock) {
                    if (_propertiesAString == null) {
                        _propertiesAString = Helpers.createAnnotatedString(properties)
                    }
                }
            }
            return _propertiesAString!!
        }

    var isSelected: Boolean = false

    fun key() = "${source.type}_${eventName}_${localTime}_${properties.hashCode()}"
}
