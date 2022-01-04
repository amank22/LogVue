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
