package models

sealed class ParameterFormats(val key: String, val text: String)
object FormatJsonPretty : ParameterFormats("jsonpretty", "Json with pretty print")
object FormatJsonCompact : ParameterFormats("json", "Compact Json")
object FormatYaml : ParameterFormats("yaml", "Yaml")

val DefaultFormats = listOf(FormatJsonPretty, FormatJsonCompact, FormatYaml)