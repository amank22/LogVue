package models

enum class FilterOperation(val opString : String, val opDescriptor : String) {
    OpEqual("=", "Equals");
    // TODO: Add capability for more like >,<, >= , <=, !=

    companion object {

        fun getOpList() = values().map { it.opString }

        fun getOp(sign : String): FilterOperation {
            val s = sign.trim()
            values().forEach {
                if (it.opString == s) return it
            }
            return OpEqual
        }

        fun filter(propertyValue : Any, filter: Filter) : Boolean {
            return when(filter.operation) {
                OpEqual -> {
                    filter.value == propertyValue
                }
                else -> false
            }
        }
    }
}



