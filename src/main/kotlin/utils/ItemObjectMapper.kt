package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.Item.ObjectItem

/**
 * Creates object/instance from toString()-Model.
 *
 * @author dschreiber
 */
class ItemObjectMapper {

    fun parse(item: Item): Any? {
        return try {
            when (item) {
                is ObjectItem -> {
                    parseObject(item)
                }
                else -> {
                    Helpers.tryParseToType(item.stringRepresentation)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Unexpected exception!", e)
            println("Unexpected Exception! (item=$item e = ${e.message}")
            item.stringRepresentation
        }
    }

    private fun parseObject(item: ObjectItem): HashMap<String, Any?> {
        val map = hashMapOf<String, Any?>()
        item.getAttributes().forEach { entry ->
//            println("parsing for field: $entry")
            val stringRepresentation = entry.value.stringRepresentation ?: ""
            val key = entry.key.removePrefix("{").removeSuffix("}")

            if (stringRepresentation.startsWith("{") && stringRepresentation.endsWith("}")) {
                // this is an object
                map[key] = parse(ObjectItem("Bundle[$stringRepresentation]"))
            } else {
                map[key] = parse(Item.ValueItem(stringRepresentation.removePrefix("{").removeSuffix("}")))
            }
        }
        return map
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory
            .getLogger(ItemObjectMapper::class.java)
    }
}
