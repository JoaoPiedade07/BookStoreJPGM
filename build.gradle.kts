plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "2.3.0" // Add the Ktor plugin
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("com.google.firebase:firebase-admin:9.0.0")
    implementation("io.ktor:ktor-server-core:2.3.0") // Core Ktor module
    implementation("io.ktor:ktor-server-netty:2.3.0") // Netty engine for running the server
    implementation("io.ktor:ktor-server-html-builder:2.3.0")
    testImplementation("io.ktor:ktor-server-tests:2.3.0") // Test utilities
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
