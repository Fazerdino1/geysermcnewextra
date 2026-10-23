dependencies {
    implementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
    shadow("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    shadow("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
    shadow("com.fasterxml.jackson.core:jackson-core:2.17.0")
    shadow("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    compileOnly("com.github.retrooper:packetevents-api:2.10.0")
    compileOnly("org.geysermc.geyser:core:${property("geyser_version")}")
    compileOnly("org.geysermc.floodgate:core:2.2.5-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}


tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("GeyserExtras-CPMC.jar")
        relocate("org.spongepowered", "dev.letsgoaway.relocate.org.spongepowered")
        relocate("io.leangen.geantyref", "dev.letsgoaway.io.leangen.geantyref")
        relocate("com.fasterxml", "dev.letsgoaway.relocate.com.fasterxml")

    }
}


// PluginVersion.GE_VERSION has to be a compile-time constant (used by the Velocity @Plugin annotation),
// so it cannot be generated from the project version. Fail the build if the two drift apart.
val checkPluginVersion by tasks.registering {
    val source = file("src/main/java/dev/letsgoaway/geyserextras/core/version/PluginVersion.java")
    val expected = project.version.toString()
    inputs.file(source)
    doLast {
        val actual = Regex("GE_VERSION = \"([^\"]+)\"").find(source.readText())?.groupValues?.get(1)
        if (actual != expected) {
            throw GradleException("PluginVersion.GE_VERSION is '$actual' but gradle.properties version is '$expected'")
        }
    }
}

tasks.named("check") {
    dependsOn(checkPluginVersion)
}
