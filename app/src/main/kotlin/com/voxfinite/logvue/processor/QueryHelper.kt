package com.voxfinite.logvue.processor

import com.googlecode.cqengine.ConcurrentIndexedCollection
import com.googlecode.cqengine.ObjectLockingIndexedCollection
import com.googlecode.cqengine.attribute.support.FunctionalSimpleAttribute
import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.index.navigable.NavigableIndex
import com.googlecode.cqengine.index.radix.RadixTreeIndex
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex
import com.googlecode.cqengine.index.radixreversed.ReversedRadixTreeIndex
import com.googlecode.cqengine.query.parser.sql.SQLParser
import com.voxfinite.logvue.api.models.LogItem
import com.voxfinite.logvue.storage.SessionConfig
import com.voxfinite.logvue.utils.AppLog
import com.voxfinite.logvue.utils.ConfigConstants
import com.voxfinite.logvue.utils.EventCompanion
import com.voxfinite.logvue.utils.reportException
import kotlin.reflect.KProperty1
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

const val QUERY_PREFIX = "Select * from logs where"

inline fun <reified O, reified A> attribute(name: String, accessor: KProperty1<O, A>): FunctionalSimpleAttribute<O, A> {
    return FunctionalSimpleAttribute(O::class.java, A::class.java, name) { accessor.get(it) }
}

fun queryCollection(): ConcurrentIndexedCollection<LogItem> {
    return ObjectLockingIndexedCollection<LogItem>().apply {
        addIndex(HashIndex.onAttribute(EventCompanion.EVENT_NAME))
        addIndex(RadixTreeIndex.onAttribute(EventCompanion.EVENT_NAME))
        addIndex(InvertedRadixTreeIndex.onAttribute(EventCompanion.EVENT_NAME))
        addIndex(ReversedRadixTreeIndex.onAttribute(EventCompanion.EVENT_NAME))
        addIndex(HashIndex.onAttribute(EventCompanion.ATTR_TIME))
        addIndex(NavigableIndex.onAttribute(EventCompanion.ATTR_TIME))
    }
}

fun sqlParser(): SQLParser<LogItem> {
    return SQLParser.forPojo(LogItem::class.java).apply {
        registerAttribute(EventCompanion.EVENT_NAME)
        registerAttribute(EventCompanion.ATTR_TIME)
    }
}

@OptIn(ExperimentalTime::class)
fun filterLogs(
    indexedCollection: ConcurrentIndexedCollection<LogItem>,
    list: List<LogItem>,
    parser: SQLParser<LogItem>,
    filterQuery: String?
): List<LogItem> {
    indexedCollection.addAll(list)
    registerPropertiesInParser(list, parser, indexedCollection)
    val filterResult = measureTimedValue {
        parser.retrieve(indexedCollection, filterQuery)
    }
    if (filterResult.duration.inWholeSeconds > 2) {
        AppLog.d(
            "filtering",
            "Time taken: ${filterResult.duration} , " + "Retrieval Cost: ${filterResult.value.retrievalCost}"
        )
    }
    return filterResult.value.toList()
}

fun registerPropertiesInParser(
    list: List<LogItem>,
    parser: SQLParser<LogItem>,
    indexedCollection: ConcurrentIndexedCollection<LogItem>
) {
    val addIndex = SessionConfig.boolDefaultOn(ConfigConstants.QUERY_INDEX)
    val propertySet = hashSetOf<String>()
    list.forEach {
        registerMapPropertiesInParser(it.properties, propertySet, parser, indexedCollection, addIndex)
    }
}

private fun registerMapPropertiesInParser(
    properties: Map<String, Any>,
    propertySet: HashSet<String>,
    parser: SQLParser<LogItem>,
    indexedCollection: ConcurrentIndexedCollection<LogItem>,
    addIndex: Boolean,
    parentKey: String = ""
) {
    properties.forEach { (k, v) ->
        if (!propertySet.contains(k)) {
            if (v is Map<*, *>) {
                @Suppress("UNCHECKED_CAST") registerMapPropertiesInParser(
                    v as Map<String, Any>, propertySet, parser, indexedCollection, addIndex, "$parentKey$k."
                )
            } else {
//                println("Attribute : ${att.attributeName} with first value = $v and v class = ${v.javaClass.name}")
                val key = "$parentKey$k"
                registerAndAddIndex(v, key, parser, indexedCollection, addIndex)
                propertySet.add(k)
            }
        } else {
//            println("Duplicate Attribute : $k = $v")
        }
    }
}

fun registerAndAddIndex(
    value: Any,
    key: String,
    parser: SQLParser<LogItem>,
    indexedCollection: ConcurrentIndexedCollection<LogItem>,
    addIndex: Boolean
) {
    with(indexedCollection) {
        if (!addIndex) {
            addGenericAttribute(key, value, parser, false)
            return
        }
        if (key.isBlank()) {
            addGenericAttribute(key, value, parser)
            return
        }
        when (value) {
            is String -> {
                if (value.isBlank()) {
                    addGenericAttribute(key, value, parser)
                    return
                }
                val att: ParameterizedAttribute<String> = ParameterizedAttribute(key, value.javaClass)
                try {
                    addIndex(HashIndex.onAttribute(att))
                    addIndex(NavigableIndex.onAttribute(att))
                    addIndex(RadixTreeIndex.onAttribute(att))
                    addIndex(InvertedRadixTreeIndex.onAttribute(att))
                    addIndex(ReversedRadixTreeIndex.onAttribute(att))
                    parser.registerAttribute(att)
                } catch (e: Exception) {
                    e.reportException()
                    addGenericAttribute(key, value, parser)
                }
            }
            is Comparable<*> -> {
                try {
                    val att: ParameterizedAttribute<Comparable<*>> = ParameterizedAttribute(key, value.javaClass)
                    addIndex(HashIndex.onAttribute(att))
//                    addIndex(NavigableIndex.onAttribute(att)) // TODO: break it to specific types
                    parser.registerAttribute(att)
                } catch (e: Exception) {
                    e.reportException()
                    addGenericAttribute(key, value, parser)
                }
            }
            else -> {
                addGenericAttribute(key, value, parser)
            }
        }
    }
}

private fun ConcurrentIndexedCollection<LogItem>.addGenericAttribute(
    key: String,
    value: Any,
    parser: SQLParser<LogItem>,
    addIndex: Boolean = true
) {
    val att: ParameterizedAttribute<Any> = ParameterizedAttribute(key, value.javaClass)
    if (addIndex) {
        try {
            addIndex(HashIndex.onAttribute(att))
        } catch (e: IllegalStateException) {
            // Maybe the index is already added
            AppLog.d(e.message.orEmpty())
        }
    }
    parser.registerAttribute(att)
}
