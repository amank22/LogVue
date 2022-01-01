package models

import androidx.compose.ui.text.AnnotatedString
import processor.attribute
import utils.Helpers
import utils.hashMapEntityOf
import java.io.Serializable
import javax.annotation.concurrent.GuardedBy

data class LogItem(
    val source: ItemSource,
    val eventName: String,
    val properties: HashMap<String, Any> = hashMapEntityOf(),
    val localTime: Long = System.currentTimeMillis(),
    val internalContent: InternalContent? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
        val EVENT_NAME = attribute("eventName", LogItem::eventName)
        val PROPERTY = attribute("properties", LogItem::properties)

        fun noContent(msg: String) = LogItem(SourceInternalContent, "No Logs", internalContent = NoLogsContent(msg))
        fun errorContent(error: String) = LogItem(SourceInternalContent, "Error", internalContent = ErrorContent(error))

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
