package inputs.adb.ddmlib

import models.LogCatMessage2


fun interface LogCatListener2 {
    fun log(msgList: ArrayList<LogCatMessage2>)
}