package inputs.adb

class CancelException : Exception {
    constructor() : super("Logging is Cancelled")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super("Logging is Cancelled", cause)
}
