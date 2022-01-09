package models

sealed interface InternalContent

data class NoLogsContent(val msg: String) : InternalContent
data class ErrorContent(val error: String, val throwable: Throwable?) : InternalContent
