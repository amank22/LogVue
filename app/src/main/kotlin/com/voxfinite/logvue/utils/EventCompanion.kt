package com.voxfinite.logvue.utils

import com.voxfinite.logvue.api.models.LogItem
import com.voxfinite.logvue.api.models.ErrorContent
import com.voxfinite.logvue.api.models.NoLogsContent
import com.voxfinite.logvue.api.models.SourceInternalContent
import com.voxfinite.logvue.processor.attribute

object EventCompanion {

    val EVENT_NAME = attribute("eventName", LogItem::eventName)
    val ATTR_TIME = attribute("localTime", LogItem::localTime)

    fun noContent(msg: String) = LogItem(SourceInternalContent, "No Logs", internalContent = NoLogsContent(msg))
    fun errorContent(error: ErrorContent) = LogItem(SourceInternalContent, "Error", internalContent = error)

}
