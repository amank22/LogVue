package models

import androidx.compose.ui.text.AnnotatedString
import processor.attribute
import utils.HashMapEntity
import utils.Helpers
import utils.hashMapEntityOf
import java.io.Serializable
import java.util.*
import javax.annotation.concurrent.GuardedBy

data class LogItem(
    val source: ItemSource,
    val eventName: String,
    val properties: HashMapEntity<String, Any> = hashMapEntityOf(hashMapOf()),
    val localTime: Long = System.currentTimeMillis(),
    val internalContent: InternalContent? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
        val EVENT_NAME = attribute("eventName", LogItem::eventName)

        fun noContent(msg: String) = LogItem(SourceInternalContent, "No Logs", internalContent = NoLogsContent(msg))
        fun errorContent(error: String) = LogItem(SourceInternalContent, "Error", internalContent = ErrorContent(error))
    }

    private val id: String = buildKey()

    @Transient
    var _propertiesAString: AnnotatedString? = null

    @Transient
    var _predictedEventType: PredictedEventType? = null

    /**
     * This is a predicted event type and there is no guarantee of it's accuracy
     */
    fun predictedEventType(): PredictedEventType {
        if (_predictedEventType == null) {
            synchronized(lock) {
                if (_predictedEventType == null) {
                    _predictedEventType = Helpers.predictEventType(this)
                }
            }
        }
        return _predictedEventType!!
    }

    @Transient
    private val lock = true

    @GuardedBy("lock")
    fun propertiesAString(): AnnotatedString {
        if (_propertiesAString == null) {
            synchronized(lock) {
                if (_propertiesAString == null) {
                    _propertiesAString = Helpers.createAnnotatedString(properties)
                }
            }
        }
        return _propertiesAString!!
    }

    @Transient
    var isSelected: Boolean = false

    fun buildKey() = "${iKey()}_$localTime"
    fun key() = id

    private fun iKey(): String {
        val k = "${source.type}_${eventName}_${properties.hashCode()}"
        return k + UUID.randomUUID().toString()
    }
}
