package com.voxfinite.logvue.adb

import com.voxfinite.logvue.adb.ddmlib.AdbHelper
import kotlinx.coroutines.flow.Flow
import com.voxfinite.logvue.api.models.LogCatMessage2
import com.voxfinite.logvue.utils.Either

class AndroidLogStreamer {

    fun stream(packageName: String, filters: List<String>): Flow<Either<LogCatErrors, ArrayList<LogCatMessage2>>> {
        return AdbHelper.monitorLogs(packageName, filters)
    }

    fun stop() {
        AdbHelper.closeLogs()
    }
}
