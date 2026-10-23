package fr.crossplaymc.geysermcnewextra.spigot.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpigotConfigTest {
    private static GeyserMCNewExtraSpigotConfig load(Path file) throws Exception {
        CommentedConfigurationNode node = YamlConfigurationLoader.builder().path(file).nodeStyle(NodeStyle.BLOCK).build().load();
        return node.get(GeyserMCNewExtraSpigotConfig.class);
    }

    @Test
    void oldConfigGetsEveryModuleOff(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("spigot.yml");
        Files.writeString(file, "enable-java-only-block-placement: true\nversion: 1\n");

        GeyserMCNewExtraSpigotConfig config = load(file);

        assertTrue(config.isEnableJavaOnlyBlockPlacement(), "existing options are kept");
        assertFalse(config.getArmorStands().isEnabled());
        assertFalse(config.getMaps().isEnabled());
        assertFalse(config.getCollisions().isEnabled());
        // Options that matter for PvP stay off even once the module is turned on
        assertFalse(config.getMaps().isLocatorMaps());
        assertFalse(config.getMaps().isReducedDebugInfo());
    }

    @Test
    void moduleOptionsAndWorldsAreRead(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("spigot.yml");
        Files.writeString(file, """
                armor-stands:
                  enabled: true
                  worlds: [smp, smp_nether]
                  pose-cycling: false
                collisions:
                  enabled: true
                  pointed-dripstone: false
                """);

        GeyserMCNewExtraSpigotConfig config = load(file);

        assertTrue(config.getArmorStands().isEnabled());
        assertEquals(List.of("smp", "smp_nether"), config.getArmorStands().getWorlds());
        assertFalse(config.getArmorStands().isPoseCycling());
        assertTrue(config.getArmorStands().isArms());
        assertTrue(config.getCollisions().isBamboo());
        assertFalse(config.getCollisions().isPointedDripstone());
    }

    @Test
    void savedConfigContainsTheModules(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("spigot.yml");
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(file).nodeStyle(NodeStyle.BLOCK).build();
        CommentedConfigurationNode node = loader.createNode();
        node.set(GeyserMCNewExtraSpigotConfig.class, new GeyserMCNewExtraSpigotConfig());
        loader.save(node);

        String saved = Files.readString(file);
        assertTrue(saved.contains("armor-stands:"), saved);
        assertTrue(saved.contains("locator-maps: false"), saved);
        assertTrue(saved.contains("worlds: []"), saved);
    }

    @Test
    void emptyWorldListMeansEveryWorld() {
        assertTrue(WorldScope.contains(List.of(), "world"));
        assertTrue(WorldScope.contains(List.of("SMP"), "smp"));
        assertFalse(WorldScope.contains(List.of("smp"), "world"));
    }
}
