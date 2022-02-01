package com.voxfinite.logvue.api.utils.deserializers

import com.voxfinite.logvue.api.utils.deserializers.`object`.ObjectDeserializer
import com.voxfinite.logvue.api.utils.deserializers.json.JsonDeserializer

object EventDeserializers {

    fun fromObject(message: String?) = ObjectDeserializer.map(message)
    fun fromJson(message: String?) = JsonDeserializer.map(message)

}