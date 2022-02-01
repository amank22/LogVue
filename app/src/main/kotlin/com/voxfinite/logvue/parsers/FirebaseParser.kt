package com.voxfinite.logvue.parsers

import com.voxfinite.logvue.api.LogEventParser
import com.voxfinite.logvue.api.models.LogCatMessage2
import com.voxfinite.logvue.api.models.LogItem
import com.voxfinite.logvue.api.models.LogLevel2
import com.voxfinite.logvue.utils.Helpers

class FirebaseParser : LogEventParser {

    override fun filters(): List<String> {
        return arrayListOf("FA", "FA-SVC")
    }

    override fun validate(logCatMessage2: LogCatMessage2): Boolean {
        return Helpers.validateFALogString(logCatMessage2.message) &&
                logCatMessage2.header.logLevel != LogLevel2.ERROR
    }

    override fun parse(logCatMessage2: LogCatMessage2): LogItem = Helpers.parseFALogs(logCatMessage2)
}
