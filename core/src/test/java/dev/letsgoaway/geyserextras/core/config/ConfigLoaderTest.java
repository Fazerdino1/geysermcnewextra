package dev.letsgoaway.geyserextras.core.config;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {
    private static CommentedConfigurationNode load(String yaml) throws Exception {
        return YamlConfigurationLoader.builder()
                .source(() -> new BufferedReader(new StringReader(yaml)))
                .build()
                .load();
    }

    @Test
    void migratesAVersion4ConfigKeepingUserValues() throws Exception {
        CommentedConfigurationNode node = load("""
                enable-java-combat-sounds: false
                enable-bedrock-capes-on-java-workaround: false
                enable-custom-cooldown: true
                version: 4
                """);

        assertEquals(4, ConfigLoader.migrate(node));
        GeyserExtrasConfig config = node.get(GeyserExtrasConfig.class);

        assertEquals(ConfigLoader.LATEST_VERSION, config.getVersion());
        assertFalse(config.isEnableJavaCombatSounds());
        assertFalse(config.isEnableBedrockCapesOnJavaWorkaround());
        assertEquals(500, config.getBedrockCommandDelayMs());
        assertTrue(node.node("enable-custom-cooldown").virtual(), "removed option must be dropped");
    }

    @Test
    void latestConfigIsLeftAlone() throws Exception {
        CommentedConfigurationNode node = load("bedrock-command-delay-ms: 0\nversion: " + ConfigLoader.LATEST_VERSION + "\n");
        assertEquals(ConfigLoader.LATEST_VERSION, ConfigLoader.migrate(node));
        assertEquals(0, node.get(GeyserExtrasConfig.class).getBedrockCommandDelayMs());
    }
}
