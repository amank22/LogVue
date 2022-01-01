package processor

import com.googlecode.cqengine.attribute.SimpleNullableAttribute
import com.googlecode.cqengine.query.option.QueryOptions
import models.LogItem


class ParameterizedAttribute<T>(private val mapKey: String, private val clazz: Class<T>) :
    SimpleNullableAttribute<LogItem, T>(LogItem::class.java, clazz, mapKey) {

    override fun getValue(logItem: LogItem, queryOptions: QueryOptions?): T? {
        val result = logItem.properties[mapKey]
        if (result == null || attributeType.isAssignableFrom(clazz)) {
            return clazz.cast(result)
        }
        throw ClassCastException("Cannot cast " + result.javaClass.name + " to " + attributeType.name + " for map key: " + mapKey);
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