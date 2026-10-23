dependencies {
    implementation(project(":core"))
    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("org.geysermc.geyser:core:${property("geyser_version")}")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1")
    compileOnly("com.github.retrooper:packetevents-bungeecord:2.10.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("GeyserMCNewExtra-BungeeCord.jar")
        relocate("org.spongepowered", "fr.crossplaymc.geysermcnewextra.relocate.org.spongepowered")
        relocate("io.leangen.geantyref", "fr.crossplaymc.geysermcnewextra.relocate.io.leangen.geantyref")
        relocate("com.fasterxml", "fr.crossplaymc.geysermcnewextra.relocate.com.fasterxml")
        relocate("net.kyori", "org.geysermc.geyser.platform.bungeecord.shaded.net.kyori")
    }
}
