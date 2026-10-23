package fr.crossplaymc.geysermcnewextra.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataFolderMigrationTest {
    @TempDir
    Path plugins;

    @Test
    void copiesTheOldFolderAndKeepsIt() throws Exception {
        Path legacy = plugins.resolve("old");
        Files.createDirectories(legacy.resolve("preferences"));
        Files.writeString(legacy.resolve("config.yml"), "version: 7\n");
        Files.writeString(legacy.resolve("preferences/123.json"), "{}");
        Path current = plugins.resolve("new");

        assertTrue(DataFolderMigration.copyLegacyFolder(legacy, current));
        assertEquals("version: 7\n", Files.readString(current.resolve("config.yml")));
        assertEquals("{}", Files.readString(current.resolve("preferences/123.json")));
        assertTrue(Files.exists(legacy.resolve("config.yml")), "the old folder stays as a backup");
    }

    @Test
    void neverOverwritesAnExistingSetup() throws Exception {
        Path legacy = plugins.resolve("old");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("config.yml"), "old");
        Path current = plugins.resolve("new");
        Files.createDirectories(current);
        Files.writeString(current.resolve("config.yml"), "new");

        assertFalse(DataFolderMigration.copyLegacyFolder(legacy, current));
        assertEquals("new", Files.readString(current.resolve("config.yml")));
    }

    @Test
    void copiesIntoAnEmptyFolderAndIgnoresAMissingLegacyFolder() throws Exception {
        Path current = plugins.resolve("new");
        Files.createDirectories(current);
        assertFalse(DataFolderMigration.copyLegacyFolder(plugins.resolve("missing"), current));

        Path legacy = plugins.resolve("old");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("config.yml"), "x");
        assertTrue(DataFolderMigration.copyLegacyFolder(legacy, current));
    }
}
