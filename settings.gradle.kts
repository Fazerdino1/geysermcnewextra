rootProject.name = "GeyserExtras-CPMC"
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

plugins {
    // Lets Gradle download the JDK required by the Java toolchain when none is installed
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include("core", "spigot", "bungee", "velocity", "extension")