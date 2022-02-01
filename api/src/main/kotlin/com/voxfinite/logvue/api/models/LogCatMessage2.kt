package com.voxfinite.logvue.api.models

import java.io.Serializable

data class LogCatMessage2(val header: LogCatHeader2, val message: String) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    override fun toString(): String {
        return "$header: $message"
    }
}
