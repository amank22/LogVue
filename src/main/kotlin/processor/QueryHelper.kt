package processor

import com.googlecode.cqengine.ConcurrentIndexedCollection
import com.googlecode.cqengine.attribute.support.FunctionalSimpleAttribute
import com.googlecode.cqengine.index.hash.HashIndex
import com.googlecode.cqengine.index.radix.RadixTreeIndex
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex
import com.googlecode.cqengine.index.radixreversed.ReversedRadixTreeIndex
import com.googlecode.cqengine.query.parser.sql.SQLParser
import models.LogItem
import utils.Log
import kotlin.reflect.KProperty1
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

const val QUERY_PREFIX = "Select * from logs where"

inline fun <reified O, reified A> attribute(name: String, accessor: KProperty1<O, A>): FunctionalSimpleAttribute<O, A> {
    return FunctionalSimpleAttribute(O::class.java, A::class.java, name) { accessor.get(it) }
}

fun queryCollection(): ConcurrentIndexedCollection<LogItem> {
    return ConcurrentIndexedCollection<LogItem>().apply {
        addIndex(HashIndex.onAttribute(LogItem.EVENT_NAME))
        addIndex(RadixTreeIndex.onAttribute(LogItem.EVENT_NAME))
        addIndex(InvertedRadixTreeIndex.onAttribute(LogItem.EVENT_NAME))
        addIndex(ReversedRadixTreeIndex.onAttribute(LogItem.EVENT_NAME))
    }
}

fun sqlParser(): SQLParser<LogItem> {
    return SQLParser.forPojo(LogItem::class.java).apply {
        registerAttribute(LogItem.EVENT_NAME)
        registerAttribute(LogItem.PROPERTY)
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
    registerPropertiesInParser(list, parser)
    val filterResult = measureTimedValue {
        parser.retrieve(indexedCollection, filterQuery)
    }
    Log.d("filtering", "Time taken: ${filterResult.duration} , Retrieval Cost: ${filterResult.value.retrievalCost}")
    return filterResult.value.toList().sortedBy { it.localTime }
}

private fun registerPropertiesInParser(
    list: List<LogItem>,
    parser: SQLParser<LogItem>
) {
    val propertySet = hashSetOf<String>()
    list.forEach {
        registerMapPropertiesInParser(it.properties, propertySet, parser)
    }
}

private fun registerMapPropertiesInParser(
    properties: Map<String, Any>,
    propertySet: HashSet<String>,
    parser: SQLParser<LogItem>,
    parentKey: String = ""
) {
    properties.forEach { (k, v) ->
        if (!propertySet.contains(k)) {
            val att: ParameterizedAttribute<Any> = ParameterizedAttribute("$parentKey$k", v.javaClass)
            if (v is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                registerMapPropertiesInParser(
                    v as Map<String, Any>, propertySet,
                    parser, "$parentKey$k."
                )
            } else {
                println("Attribute : ${att.attributeName} with first value = $v and v class = ${v.javaClass.name}")
                parser.registerAttribute(att)
                propertySet.add(k)
            }
        } else {
//            println("Duplicate Attribute : $k = $v")
        }
    }
}
