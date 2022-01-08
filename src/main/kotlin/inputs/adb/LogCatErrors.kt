package inputs.adb

import java.io.Serializable

sealed class LogCatErrors : Exception(), Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

object LogErrorNotEnabledForFA : LogCatErrors()
object LogErrorDeviceNotConnected : LogCatErrors()
object LogErrorNoSession : LogCatErrors()
object LogErrorPackageIssue : LogCatErrors()
object LogErrorADBIssue : LogCatErrors()
class LogErrorUnknown(val exception: Exception = Exception()) : LogCatErrors() {
    constructor(exception: String) : this(Exception(exception))
}

fun logcatErrorString(it: LogCatErrors) = when (it) {
    is LogErrorADBIssue -> {
        "There is some issue with device. Check if your device is connected and your app is running"
    }
    is LogErrorDeviceNotConnected -> {
        "Please connect your device or start an emulator"
    }
    is LogErrorNoSession -> {
        "Create a new session to start logging data"
    }
    is LogErrorNotEnabledForFA -> {
        "Unable to enable logs for firebase"
    }
    is LogErrorPackageIssue -> {
        "The app might not be installed or app processed is not running on the device. Please check."
    }
    is LogErrorUnknown -> {
        "This is some unknown error in collecting logs. \n ${it.exception.localizedMessage}"
    }
}
