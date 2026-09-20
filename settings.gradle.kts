pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Flux"

include(":common")
include(":platform-paper")
project(":platform-paper").projectDir = file("platform/paper")
include(":demo-paper")
project(":demo-paper").projectDir = file("demo/paper")
