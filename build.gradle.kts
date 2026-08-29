import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm") version "2.4.10"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
}

val javaVersion = 21

group = "io.github.natnaelgetachewyirga"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(javaVersion)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        allWarningsAsErrors.set(true)
    }
    explicitApi()
}

application {
    mainClass.set("io.github.natnaelgetachewyirga.anagram.cli.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}

ktlint {
    version.set("1.8.0")
}

kover {
    reports {
        filters {
            excludes {
                classes("io.github.natnaelgetachewyirga.anagram.cli.MainKt")
            }
        }

        verify {
            rule {
                minBound(95, CoverageUnit.LINE)
                minBound(90, CoverageUnit.BRANCH)
            }
        }
    }
}
