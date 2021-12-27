package inputs.adb

sealed class LogCatErrors : Exception()
object LogErrorNotEnabledForFA : LogCatErrors()
object LogErrorDeviceNotConnected : LogCatErrors()
object LogErrorPackageIssue : LogCatErrors()
object LogErrorADBIssue : LogCatErrors()
class LogErrorUnknown(val exception: Exception = Exception()) : LogCatErrors() {
    constructor(exception: String) : this(Exception(exception))
}