package fr.crossplaymc.geysermcnewextra.core.config;

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
        GeyserMCNewExtraConfig config = node.get(GeyserMCNewExtraConfig.class);

        assertEquals(ConfigLoader.LATEST_VERSION, config.getVersion());
        assertFalse(config.isEnableJavaCombatSounds());
        assertFalse(config.isEnableBedrockCapesOnJavaWorkaround());
        assertEquals(500, config.getBedrockCommandDelayMs());
        assertTrue(node.node("enable-custom-cooldown").virtual(), "removed option must be dropped");
    }

    @Test
    void renamesOptionsNamedAfterTheOldProject() throws Exception {
        CommentedConfigurationNode node = load("""
                enable-geyser-extras-menu: false
                geyser-extras-pack-priority: 42
                version: 7
                """);
        ConfigLoader.migrate(node);
        GeyserMCNewExtraConfig config = node.get(GeyserMCNewExtraConfig.class);
        assertFalse(config.isEnableMenu());
        assertEquals(42, config.getResourcePackPriority());
        assertTrue(node.node("enable-geyser-extras-menu").virtual());
    }

    @Test
    void switchesTheOldDefaultLanguageToFrench() throws Exception {
        CommentedConfigurationNode node = load("default-locale: en_US\nversion: 8\n");
        ConfigLoader.migrate(node);
        assertEquals("fr_FR", node.get(GeyserMCNewExtraConfig.class).getDefaultLocale());

        CommentedConfigurationNode chosen = load("default-locale: es_ES\nversion: 8\n");
        ConfigLoader.migrate(chosen);
        assertEquals("es_ES", chosen.get(GeyserMCNewExtraConfig.class).getDefaultLocale(), "a language chosen by the admin stays");
    }

    @Test
    void latestConfigIsLeftAlone() throws Exception {
        CommentedConfigurationNode node = load("bedrock-command-delay-ms: 0\nversion: " + ConfigLoader.LATEST_VERSION + "\n");
        assertEquals(ConfigLoader.LATEST_VERSION, ConfigLoader.migrate(node));
        assertEquals(0, node.get(GeyserMCNewExtraConfig.class).getBedrockCommandDelayMs());
    }
}
