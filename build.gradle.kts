plugins {
    kotlin("jvm") version "2.4.0"
    application
}

group = "calculator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "calculator.MainKt"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}