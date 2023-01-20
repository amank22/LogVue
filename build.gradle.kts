import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val pluginsDir by extra { file("$buildDir/plugins") }

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.20"))
    }
}

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.7.20"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.languageVersion = "1.7"
        kotlinOptions.apiVersion = "1.7"
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

apply(from = "${rootDir}/scripts/publish-root.gradle")
