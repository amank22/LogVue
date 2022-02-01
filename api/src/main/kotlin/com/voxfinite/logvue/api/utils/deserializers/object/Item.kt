package com.voxfinite.logvue.api.utils.deserializers.`object`

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import org.slf4j.LoggerFactory
import java.io.StreamTokenizer
import java.io.StringReader
import java.util.*
import java.util.regex.Pattern

/**
 * Model for parsing toString() output.
 *
 * Caveat: if values/strings contain '[' or ']' one might get unexpected
 * results.
 *
 * @author dschreiber
 */

internal val objectPattern = Pattern.compile(
    "(^[A-Z]\\S*)[ ]?(\\((.*)\\)$)|(\\[(.*)]$)",
    Pattern.DOTALL
)
internal val mapPattern = Pattern.compile(
    "\\{.*=.*}",
    Pattern.DOTALL
)

sealed class Item(val stringRepresentation: String?) {
    class ValueItem(stringRepresentation: String?) : Item(stringRepresentation)

    class ObjectItem(stringRepresentation: String) : Item(stringRepresentation) {
        var type: String? = null
        private val attributes: MutableMap<String, Item> = HashMap()

        init {
            val typeMatcher = objectPattern.matcher(stringRepresentation)
            if (typeMatcher.matches()) {
                type = typeMatcher.group(1)
                val onFirstLevelCommaRespectEqualSign =
                    splitOnFirstLevelCommaRespectEqualSign(typeMatcher.group(2))
                for (attributeValue: String in onFirstLevelCommaRespectEqualSign) {
                    val split: Iterator<String> = Splitter.on("=").trimResults()
                        .limit(2).split(attributeValue).iterator()
                    val attributeName = split.next()
                    val attributeValueString = split.next()
                    attributes[attributeName] = parseString(attributeValueString)
                }
            } else {
                throw IllegalArgumentException(
                    "cannot create object from string: " +
                            stringRepresentation
                )
            }
        }

        fun getAttributes(): Map<String, Item> {
            return attributes
        }

        override fun toString(): String {
            return (super.toString() +
                    "\n Type=" +
                    type +
                    "\n  " +
                    Joiner.on("\n  ").withKeyValueSeparator(" = ")
                        .join(attributes))
        }
    }

    class MapItem(stringRepresentation: String) : Item(stringRepresentation) {
        private val attributes: MutableMap<String, Item> = hashMapOf()

        init {
            val typeMatcher = mapPattern.matcher(stringRepresentation)
            if (!typeMatcher.matches()) {
                throw IllegalArgumentException("Cannot create map from string: $stringRepresentation")
            }
            val onFirstLevelCommaRespectEqualSign =
                splitOnFirstLevelCommaRespectEqualSignInMap(stringRepresentation.removePrefix("{").removeSuffix("}"))
            for (attributeValue: String in onFirstLevelCommaRespectEqualSign) {
                val split: Iterator<String> = Splitter.on("=").trimResults()
                    .limit(2).split(attributeValue).iterator()
                val attributeName = split.next()
                val attributeValueString = split.next()
                attributes[attributeName] = parseString(attributeValueString)
            }
        }

        fun getAttributes(): Map<String, Item> {
            return attributes
        }

        override fun toString(): String {
            return (super.toString() +
                    Joiner.on("\n  ").withKeyValueSeparator(" = ")
                        .join(attributes))
        }
    }

    class ListItem(stringRepresentation: String) : Item(stringRepresentation) {
        private val values: MutableList<Item> = ArrayList()

        init {
            // remove "[" and "]":
            val valueString = stringRepresentation.substring(
                1,
                stringRepresentation.length - 1
            )
            LOGGER.debug("no brackets - list: $valueString")
            for (value: String in splitOnFirstLevelComma(valueString)) {
                values.add(parseString(value))
            }
        }

        override fun toString(): String {
            return super.toString() + "\n  " + Joiner.on("\n  ").join(values)
        }
    }

    override fun toString(): String {
        return "Item [stringRepresentation=$stringRepresentation]"
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Item::class.java)

        /**
         * counts occurence of `count` in `string`
         *
         * @param string
         * @param count
         * @return
         */
        private fun contains(string: String, count: Char): Int {
            var counter = 0
            for (element in string) {
                if (element == count) {
                    counter++
                }
            }
            return counter
        }

        /**
         * only the first comma before an equal sign ('=') is used for split. (So
         * that strings that contain a comma are not split.)
         *
         * @param string
         * @return
         */
        fun splitOnFirstLevelCommaRespectEqualSign(
            string: String
        ): List<String> {
            val allSplits = splitOnFirstLevelComma(string)
            val result: MutableList<String> = ArrayList(allSplits.size)
            for (current: String in allSplits) {
                if (current.contains("=")) {
                    result.add(current)
                } else {
                    if (result.isEmpty()) {
//                        throw IllegalStateException(
//                            ("first comma must not occur before first equal sign! (" +
//                                    string + ")")
//                        )
                        result.add(current)
                    } else {
                        result[result.size - 1] = (result[result.size - 1] +
                                ", " + current)
                    }
                }
            }
            return result
        }

        /**
         * ignores commas nested in square brackets ("[", "]")
         *
         * @param string
         */
        fun splitOnFirstLevelComma(string: String?): List<String> {
            if (string.isNullOrBlank()) return emptyList()
            val scanner = Scanner(string)
            scanner.useDelimiter(", ")
            val result: MutableList<String> = ArrayList()
            var openBrackets = 0
            while (scanner.hasNext()) {
                val next = scanner.next()
                val open = contains(next, '[')
                val close = contains(next, ']')
                LOGGER.debug(
                    ("openBrackets: " + openBrackets + ", open: " + open +
                            ", close: " + close + ", next: " + next)
                )
                if (openBrackets > 0) {
                    result[result.size - 1] = (result[result.size - 1] +
                            ", " + next)
                } else {
                    result.add(next)
                }
                openBrackets = openBrackets + open - close
            }
            scanner.close()
            return result
        }

        /**
         * only the first comma before an equal sign ('=') is used for split. (So
         * that strings that contain a comma are not split.)
         *
         * @param string
         * @return
         */
        fun splitOnFirstLevelCommaRespectEqualSignInMap(
            string: String
        ): List<String> {
            val allSplits = splitOnFirstLevelCommaInMap(string)
            val result: MutableList<String> = ArrayList(allSplits.size)
            for (current: String in allSplits) {
                if (current.contains("=")) {
                    result.add(current)
                } else {
                    if (result.isEmpty()) {
                        throw IllegalStateException(
                            ("first comma must not occur before first equal sign! (" +
                                    string + ")")
                        )
                    }
                    result[result.size - 1] = (result[result.size - 1] +
                            ", " + current)
                }
            }
            return result
        }

        /**
         * ignores commas nested in square brackets ("{", "}")
         *
         * @param string
         */
        fun splitOnFirstLevelCommaInMap(string: String?): List<String> {
            if (string.isNullOrBlank()) return emptyList()
            val scanner = Scanner(string)
            scanner.useDelimiter(", ")
            val result = arrayListOf<String>()

            var openBrackets = 0
            while (scanner.hasNext()) {
                val next = scanner.next()
                val open = contains(next, '{')
                val close = contains(next, '}')
                println(
                    ("openBrackets: " + openBrackets + ", open: " + open +
                            ", close: " + close + ", next: " + next)
                )
                if (openBrackets > 0) {
                    result[result.size - 1] = (result[result.size - 1] +
                            ", " + next)
                } else {
                    result.add(next)
                }
                openBrackets = openBrackets + open - close
            }
            scanner.close()
            return result
        }

        fun parseString(stringRaw: String?): Item {
            if (stringRaw.isNullOrBlank()) {
                return ValueItem(stringRaw)
            }
            val string = stringRaw.trim { it <= ' ' }
            return if (string.startsWith("[")) {
                ListItem(string)
            } else if (objectPattern.matcher(string).matches() && string.contains("=")) {
                ObjectItem(string)
            } else if (mapPattern.matcher(string).matches() && string.contains("=")) {
                MapItem(string)
            } else {
                ValueItem(string)
            }
        }
    }
}
