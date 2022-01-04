package inputs.adb

import inputs.adb.ddmlib.AdbHelper
import kotlinx.coroutines.flow.Flow
import models.LogCatMessage2
import utils.Either

class AndroidLogStreamer {

    fun stream(packageName: String): Flow<Either<LogCatErrors, ArrayList<LogCatMessage2>>> {
        return AdbHelper.monitorLogs(packageName)
    }

    fun stop() {
        AdbHelper.closeLogs()
    }
}
