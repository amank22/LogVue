package com.voxfinite.logvue.models

sealed class ParameterFormats(val key: String, val text: String, val subText: String)
object FormatJsonPretty :
    ParameterFormats(
        "jsonpretty", "Json with pretty print",
        "Long string with multiple lines with json format"
    )

object FormatJsonCompact :
    ParameterFormats(
        "json", "Compact Json",
        "Small json string without any formatting in a single line"
    )

object FormatYaml : ParameterFormats(
    "yaml", "Yaml",
    "Human friendly formatted string with indentation as formatting and multi-lines"
)

val DefaultFormats = listOf(FormatJsonPretty, FormatJsonCompact, FormatYaml)
