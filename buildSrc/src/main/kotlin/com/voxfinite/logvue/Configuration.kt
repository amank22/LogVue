package com.voxfinite.logvue

object Configuration {

    object Api {
        const val majorVersion = 1
        const val minorVersion = 0
        const val patchVersion = 0
        const val versionName = "$majorVersion.$minorVersion.$patchVersion"
        const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
        const val isSnapshot = false
        const val artifactGroup = "io.github.amank22.logvue"
        const val artifactId = "api"
    }
}
