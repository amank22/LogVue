package com.voxfinite.logvue.utils

import org.gradle.api.Project
import java.io.File

fun Project.getMainAppVersion() : String {
    val key = "APP_VERSION"
    return if (hasProperty(key)) {
        val version = property("APP_VERSION").toString()
        println("Version = $version")
        if (version.isBlank()) {
            return "1.0.0"
        }
        if (version.matches(Regex("^[\\d]{1,3}.[\\d]{1,3}.[\\d]{1,4}"))) {
            return version
        }
        if (version.matches(Regex("^v[\\d]{1,3}.[\\d]{1,3}.[\\d]{1,4}"))) {
            return version.removePrefix("v")
        }
        "1.0.0"
    } else {
        "1.0.0"
    }
}

fun File.findPkg(format: String?) = when (format != null) {
    true -> walk().firstOrNull { it.isFile && it.name.endsWith(format, ignoreCase = true) }
    else -> null
}

fun File?.ghActionOutput(prefix: String) = this?.let {
    when (System.getenv("GITHUB_ACTIONS").toBoolean()) {
        true -> println(
            """
        ::set-output name=${prefix}_name::${it.name}
        ::set-output name=${prefix}_path::${it.absolutePath}
      """.trimIndent()
        )
        else -> println("$prefix: $this")
    }
}