package com.voxfinite.logvue.api.utils.deserializers.`object`

import com.voxfinite.logvue.api.utils.deserializers.`object`.Item.ObjectItem

/**
 * Creates object/instance from toString()-Model.
 */
class ItemObjectMapper {

    fun parse(item: Item): Any? {
        return try {
            when (item) {
                is ObjectItem -> {
                    parseObject(item)
                }
                is Item.MapItem -> {
                    parseMap(item)
                }
                else -> {
                    ObjectDeserializer.tryParseToType(item.stringRepresentation)
                }
            }
        } catch (e: Exception) {
//            Exception("Item mapping failed for item=$item", e).reportException()
            item.stringRepresentation
        }
    }

    private fun parseObject(item: ObjectItem): HashMap<String, Any?> {
        val map = hashMapOf<String, Any?>()
        item.getAttributes().forEach { entry ->
            val stringRepresentation = entry.value.stringRepresentation ?: ""
            val key = entry.key.removePrefix("{").removeSuffix("}")
            if (mapPattern.matcher(stringRepresentation).matches() && stringRepresentation.contains("=")) {
                // this is a map
                map[key] = parse(Item.MapItem(stringRepresentation))
            } else if (objectPattern.matcher(stringRepresentation).matches() && stringRepresentation.contains("=")) {
                // this is a map
                map[key] = parse(ObjectItem(stringRepresentation.removePrefix("{").removeSuffix("}")))
            } else {
                map[key] = parse(Item.ValueItem(stringRepresentation.removePrefix("{").removeSuffix("}")))
            }
        }
        return map
    }

    private fun parseMap(item: Item.MapItem): HashMap<String, Any?> {
        val map = hashMapOf<String, Any?>()
        item.getAttributes().forEach { entry ->
            val stringRepresentation = entry.value.stringRepresentation ?: ""
            val key = entry.key.removePrefix("{").removeSuffix("}")
            if (mapPattern.matcher(stringRepresentation).matches() && stringRepresentation.contains("=")) {
                // this is a map
                map[key] = parse(Item.MapItem(stringRepresentation))
            } else if (objectPattern.matcher(stringRepresentation).matches() && stringRepresentation.contains("=")) {
                // this is a map
                map[key] = parse(ObjectItem(stringRepresentation.removePrefix("{").removeSuffix("}")))
            } else {
                map[key] = parse(Item.ValueItem(stringRepresentation.removePrefix("{").removeSuffix("}")))
            }
        }
        return map
    }

//    companion object {
//        private val LOGGER: Logger = LoggerFactory
//            .getLogger(ItemObjectMapper::class.java)
//    }
}
