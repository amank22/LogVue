package com.voxfinite.logvue.adb.ddmlib

import com.voxfinite.logvue.api.models.LogCatMessage2

fun interface LogCatListener2 {
    fun log(msgList: ArrayList<LogCatMessage2>)
}
