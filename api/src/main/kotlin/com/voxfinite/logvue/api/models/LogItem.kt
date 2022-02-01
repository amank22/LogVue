package com.voxfinite.logvue.api.models

import com.voxfinite.logvue.api.utils.HashMapEntity
import com.voxfinite.logvue.api.utils.hashMapEntityOf
import java.io.Serializable
import java.util.*

/**
 * Base class for holding analytics data
 * Every plugin can extend [ItemSource] and add here along with event name and other details.
 * Do not add anything to [internalContent]
 */
data class LogItem @JvmOverloads constructor(
    val source: ItemSource,
    val eventName: String,
    val properties: HashMapEntity<String, Any> = hashMapEntityOf(hashMapOf()),
    val localTime: Long = System.currentTimeMillis(),
    val internalContent: InternalContent? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    private val id: String = buildKey()

    @Transient
    var isSelected: Boolean = false

    private fun buildKey() = "${iKey()}_$localTime"
    fun key() = id

    private fun iKey(): String {
        val k = "${source.type}_${eventName}_${properties.hashCode()}"
        return k + UUID.randomUUID().toString()
    }
}
