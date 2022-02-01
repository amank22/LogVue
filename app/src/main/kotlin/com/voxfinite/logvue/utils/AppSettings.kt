package com.voxfinite.logvue.utils

import com.voxfinite.logvue.storage.Db

object AppSettings {

    const val AUTO_SCROLL = "autoScrollLogs"

    fun getFlag(key: String) = Db.configs[key] == "true"
    fun getFlagStrict(key: String) = Db.configs[key]?.toBooleanStrictOrNull()
    fun getFlagOr(key: String, default: Boolean) = getFlagStrict(key) ?: default
    fun setFlag(key: String, value: Boolean) {
        Db.configs[key] = value.toString()
    }
}
