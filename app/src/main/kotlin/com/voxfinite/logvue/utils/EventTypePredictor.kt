package com.voxfinite.logvue.utils

import com.voxfinite.logvue.api.models.LogItem
import com.voxfinite.logvue.models.PredictedEventType

object EventTypePredictor {

    private val predictionMap = hashMapOf<String, PredictedEventType>()

    fun predict(logItem: LogItem) : PredictedEventType {
        return predictionMap.getOrPut(logItem.key()) {
            Helpers.predictEventType(logItem)
        }
    }

    fun clear() = predictionMap.clear()

}

fun LogItem.predictedEventType() = EventTypePredictor.predict(this)