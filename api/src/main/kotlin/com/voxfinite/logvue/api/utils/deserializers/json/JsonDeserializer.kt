package com.voxfinite.logvue.api.utils.deserializers.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import com.voxfinite.logvue.api.utils.HashMapEntity
import com.voxfinite.logvue.api.utils.hashMapEntityOf

object JsonDeserializer {

    internal val gson: Gson by lazy {
        val gsonBuilder = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        gsonBuilder.setPrettyPrinting()
        gsonBuilder.create()
    }

    fun createJsonString(src: Any): String {
        return gson.toJson(src)
    }

    fun map(message: String?): HashMapEntity<String, Any> {
        val type = object : TypeToken<HashMap<String, Any>>() {}.type
        val map = try {
            gson.fromJson<HashMap<String, Any>>(message, type)
        } catch (e: JsonParseException) {
            hashMapOf()
        } catch (e: JsonSyntaxException) {
            hashMapOf()
        }
        return hashMapEntityOf(map)
    }

}
