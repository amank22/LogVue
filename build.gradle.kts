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
    // https://mvnrepository.com/artifact/org.apache.flink/flink-streaming-java
    implementation("org.apache.flink:flink-streaming-java_2.12:1.14.0")
    // https://mvnrepository.com/artifact/org.apache.flink/flink-clients
    implementation("org.apache.flink:flink-clients_2.12:1.14.0")
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.16.0")
    implementation("com.github.drapostolos:type-parser:0.7.0")
    // embedded database
    implementation("org.mapdb:mapdb:3.0.8")
    implementation("org.snakeyaml:snakeyaml-engine:2.3")
    implementation ("com.malinskiy.adam:adam:0.4.3")
    implementation("com.github.pgreze:kotlin-process:1.3.1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Goflog"
            packageVersion = "1.0.0"
        }
    }
}