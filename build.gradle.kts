import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val pluginsDir by extra { file("$buildDir/plugins") }

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
    }
}

plugins {
    kotlin("jvm") version "1.6.10"
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
        kotlinOptions.languageVersion = "1.6"
        kotlinOptions.apiVersion = "1.6"
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}
