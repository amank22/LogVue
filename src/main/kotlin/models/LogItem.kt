package models

import androidx.compose.ui.text.AnnotatedString
import java.io.Serializable

data class LogItem(
    val source: ItemSource,
    val eventName: String,
    val properties: HashMap<String, Any> = hashMapOf(),
    val localTime : Long = System.currentTimeMillis(),
    val internalContent : InternalContent? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L

        val NoContent = LogItem(SourceInternalContent, "No Logs", internalContent = NoLogsContent)

    }
    
    var propertiesAString : AnnotatedString? = null
    var isSelected : Boolean = false

    fun key() = "${source.type}_${eventName}_$localTime"
}
