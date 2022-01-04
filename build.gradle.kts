import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}

group = "com.gi"
version = "1.0"

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
    // https://mvnrepository.com/artifact/io.netty/netty-resolver-dns-native-macos
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.72.Final") // not sure if needed now
    implementation("com.android.tools.ddms:ddmlib:30.2.0-alpha06")
    implementation("com.google.code.gson:gson:2.8.9")
    // https://mvnrepository.com/artifact/com.googlecode.cqengine/cqengine
    implementation("com.googlecode.cqengine:cqengine:3.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
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
            packageName = "logvue"
            packageVersion = "1.0.0"
        }
    }
}
