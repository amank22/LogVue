package utils

class ValueException(val value: Any?) : Exception("Value is $value")
