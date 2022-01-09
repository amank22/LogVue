package models

import processor.attribute
import utils.HashMapEntity
import utils.Helpers
import utils.hashMapEntityOf
import java.io.Serializable
import java.util.*

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
        val ATTR_TIME = attribute("localTime", LogItem::localTime)

        fun noContent(msg: String) = LogItem(SourceInternalContent, "No Logs", internalContent = NoLogsContent(msg))
        fun errorContent(error: String) = LogItem(SourceInternalContent, "Error", internalContent = ErrorContent(error))
    }

    private val id: String = buildKey()

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

    @Transient
    var isSelected: Boolean = false

    private fun buildKey() = "${iKey()}_$localTime"
    fun key() = id

    private fun iKey(): String {
        val k = "${source.type}_${eventName}_${properties.hashCode()}"
        return k + UUID.randomUUID().toString()
    }
}
