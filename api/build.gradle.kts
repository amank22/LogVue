val pf4jVersion: String by project


plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")

    compileOnly("org.pf4j:pf4j:${pf4jVersion}")
    // types parser for object to map conversion
    implementation("com.github.drapostolos:type-parser:0.7.0")
    implementation("com.google.code.gson:gson:2.8.9")
    // https://mvnrepository.com/artifact/com.google.guava/guava
    api("com.google.guava:guava:31.0.1-jre")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}