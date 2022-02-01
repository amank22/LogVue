package com.voxfinite.logvue.adb.ddmlib

import com.android.ddmlib.MultiLineReceiver

class SingleValueReceiver(val onValue: (value: String) -> Unit) : MultiLineReceiver() {
    var isResultPending = false

    override fun isCancelled(): Boolean = isResultPending

    override fun processNewLines(lines: Array<out String>?) {
        if (!lines.isNullOrEmpty()) {
            onValue(lines.first())
        }
        isResultPending = true
    }
}
