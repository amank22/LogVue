package processor

import models.FilterOperation
import models.LogItem
import storage.Db

class ParamFilter {

    private val paramsForFilter
        get() = Db.getSessionFilters()

    fun filter(value: LogItem): Boolean {
        val properties = value.properties
        val paramsForFilter1 = paramsForFilter
        if (paramsForFilter1.isEmpty()) {
            return true
        }
        paramsForFilter1.forEach { filter ->
            val propertyValue = properties[filter.key] ?: return@forEach
            if (FilterOperation.filter(propertyValue, filter)) {
                return true
            }
        }
        return false
    }
}