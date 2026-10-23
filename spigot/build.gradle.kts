
dependencies {
    implementation(project(":core"))
    compileOnly("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("org.geysermc.geyser:core:${property("geyser_version")}")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.10.0")

}


tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("GeyserMCNewExtra-Spigot.jar")
        relocate("org.spongepowered", "fr.crossplaymc.geysermcnewextra.relocate.org.spongepowered")
        relocate("io.leangen.geantyref", "fr.crossplaymc.geysermcnewextra.relocate.io.leangen.geantyref")
        relocate("com.fasterxml", "fr.crossplaymc.geysermcnewextra.relocate.com.fasterxml")
        relocate("net.kyori", "org.geysermc.geyser.platform.spigot.shaded.net.kyori") {
            exclude("net.kyori.adventure.text.logger.slf4j.ComponentLogger")
        }
        relocate("it.unimi.dsi.fastutil", "org.geysermc.geyser.platform.spigot.shaded.it.unimi.dsi.fastutil")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Provided by the server at runtime; tests need them on their own classpath
    testImplementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
