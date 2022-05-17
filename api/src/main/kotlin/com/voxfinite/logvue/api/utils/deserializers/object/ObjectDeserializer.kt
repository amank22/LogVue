package com.voxfinite.logvue.api.utils.deserializers.`object`

import com.github.drapostolos.typeparser.GenericType
import com.github.drapostolos.typeparser.TypeParser
import com.voxfinite.logvue.api.utils.HashMapEntity
import com.voxfinite.logvue.api.utils.hashMapEntityOf
import java.util.HashMap

object ObjectDeserializer {

    private val parser = TypeParser.newBuilder().build()

    fun map(message: String?): HashMapEntity<String, Any> {
        val properties = hashMapOf<String, Any>()
        if (!message.isNullOrBlank()) {
            val mMsg = message.trim()
            if (mMsg.startsWith("{") && mMsg.endsWith("}")) {
                val item = Item.MapItem(mMsg)
                properties.putAll(item.getAttributes())
            } else {
                val item = Item.ObjectItem(mMsg)
                properties.putAll(item.getAttributes())
            }
        }
        return hashMapEntityOf(properties)
    }

    internal fun tryParseToType(str: String?): Any {
        return try {
            tryParseInternal(str)
        } catch (ve: ValueException) {
            ve.value ?: "null"
        }
    }

    @Throws(ValueException::class)
    private fun tryParseInternal(str: String?): Any {
        if (str == null) return "null"
        if (str.isBlank()) return str
        var parsed: Boolean
        parsed = tryParseType(str, Boolean::class.java)
        if (!parsed) {
            parsed = tryParseType(str, Long::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Int::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Double::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Float::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Double::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Boolean>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Boolean>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Boolean>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Int>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Int>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Int>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Long>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Long>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Long>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Float>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Float>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Float>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Double>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Double>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Double>>() {})
        }
        return str
    }

    @Throws(ValueException::class)
    private fun <T> tryParseType(str: String, clazz: Class<T>): Boolean {
        return tryOp {
            val value = parser.parse(str, clazz)
            throw ValueException(value) // anti-pattern ki ****
        }
    }

    @Throws(ValueException::class)
    private fun <T> tryParseType(str: String, genericType: GenericType<T>): Boolean {
        return tryOp {
            val value = parser.parse(str, genericType)
            throw ValueException(value)
        }
    }

    private fun tryOp(op: () -> Unit): Boolean {
        return try {
            op()
            true
        } catch (e: ValueException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

}
