package com.voxfinite.logvue.api

import com.voxfinite.logvue.api.models.LogCatMessage2
import com.voxfinite.logvue.api.models.LogItem
import org.pf4j.ExtensionPoint

/**
 * Plugin extension point
 * All plugins must extend with interface and annotate with @Extension
 */
interface LogEventParser : ExtensionPoint {

    /**
     * Filters for tags
     */
    fun filters() : List<String>

    /**
     * Validate and return whether to process this message
     */
    fun validate(logCatMessage2: LogCatMessage2) : Boolean

    /**
     * Parse item which is validated before to [LogItem]
     *
     */
    fun parse(logCatMessage2: LogCatMessage2) : LogItem

}
