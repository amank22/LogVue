package processor

import com.googlecode.cqengine.attribute.SimpleNullableAttribute
import com.googlecode.cqengine.query.option.QueryOptions
import javassist.NotFoundException
import models.LogItem

class ParameterizedAttribute<T>(private val mapKey: String, private val clazz: Class<T>) :
    SimpleNullableAttribute<LogItem, T>(LogItem::class.java, clazz, mapKey) {

    override fun getValue(logItem: LogItem, queryOptions: QueryOptions?): T? {
        val result = getNestedValue(logItem)
        if (result == null || attributeType.isAssignableFrom(clazz)) {
            try {
                return clazz.cast(result)
            } catch (cl: ClassCastException) {
                // ignore
            }
        }
        throw ClassCastException("Cannot cast " + result?.javaClass?.name + " to " + attributeType.name + " for map key: " + mapKey)
    }

    private fun getNestedValue(logItem: LogItem): Any? {
        val map = logItem.properties
        if (map.isEmpty()) {
            throw NotFoundException("$mapKey not found in properties as it is empty")
        }
        val nestedKeys = mapKey.split(".")
        if (nestedKeys.isEmpty()) {
            throw IllegalArgumentException("Key should not be empty")
        }
        val nSize = nestedKeys.size
        if (nSize == 1) {
            return map[mapKey]
        }
        var innerMap: Map<String, Any> = map
        var value: Any? = null
        nestedKeys.forEachIndexed { index, it ->
            value = innerMap[it]
            if (value == null) {
                return null // todo: not sure about this logic
            }
            if (value !is Map<*, *> && index != (nSize - 1)) {
                val ex = IllegalArgumentException(
                    "Nested structure should be in a map/object. " +
                            "Nested key = $nestedKeys with current key = $it and value = $value.\n" +
                            "Log Item is $logItem"
                )
                ex.printStackTrace()
                return null
            }
            if (index != (nSize - 1)) {
                @Suppress("UNCHECKED_CAST")
                innerMap = value as Map<String, Any>
            }
        }
        return value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + mapKey.hashCode()
        return result
    }

    override fun canEqual(other: Any?): Boolean {
        return other is ParameterizedAttribute<*>
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && mapKey == (other as? ParameterizedAttribute<*>)?.mapKey
    }
}
