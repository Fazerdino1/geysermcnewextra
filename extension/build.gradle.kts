dependencies {
    implementation(project(":core"))

    compileOnly("org.geysermc.geyser:core:${property("geyser_version")}")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("GeyserMCNewExtra-Extension.jar")
        relocate("org.spongepowered", "fr.crossplaymc.geysermcnewextra.relocate.org.spongepowered")
        relocate("io.leangen.geantyref", "fr.crossplaymc.geysermcnewextra.relocate.io.leangen.geantyref")
        relocate("com.fasterxml", "fr.crossplaymc.geysermcnewextra.relocate.com.fasterxml")
    }
}
