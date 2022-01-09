import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

group = "com.voxfinite"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    // types parser for object to map conversion
    implementation("com.github.drapostolos:type-parser:0.7.0")
    // embedded database
    implementation("org.mapdb:mapdb:3.0.8")
    implementation("org.snakeyaml:snakeyaml-engine:2.3")
//    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.72.Final") // not sure if needed now
    implementation("com.android.tools.ddms:ddmlib:30.2.0-alpha06")
    implementation("com.google.code.gson:gson:2.8.9")
    // https://mvnrepository.com/artifact/com.googlecode.cqengine/cqengine
    implementation("com.googlecode.cqengine:cqengine:3.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

    implementation("io.sentry:sentry-log4j2:5.5.2")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = project.name
            packageVersion = "${project.version}"
            description = "Local Analytics"
            linux {
                debMaintainer = "kapoor.aman22@gmail.com"
                iconFile.set(project.file("logo_icon.png"))
            }
            macOS {
                bundleID = "${project.group}.${project.name}"
                iconFile.set(project.file("logo_icon.icns"))
            }
            windows {
                upgradeUuid = "8AEBC8BF-9C94-4D02-ACA8-AF543E0CEB98"
                iconFile.set(project.file("logo_icon.ico"))
            }
        }
    }
}

buildConfig {
    className("AppBuildConfig")
    useKotlinOutput { topLevelConstants = true }
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_VERSION", "\"${project.version}\"")
}

/**
 * Sets the Github Action output as package name and path to use in other steps.
 */
gradle.buildFinished {
    val pkgFormat =
        compose.desktop.application.nativeDistributions.targetFormats.firstOrNull { it.isCompatibleWithCurrentOS }
    val nativePkg = buildDir.resolve("compose/binaries").findPkg(pkgFormat?.fileExt)
    val jarPkg = buildDir.resolve("compose/jars").findPkg(".jar")
    nativePkg.ghActionOutput("app_pkg")
    jarPkg.ghActionOutput("uber_jar")
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
