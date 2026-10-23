package fr.crossplaymc.geysermcnewextra.core.preferences;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PreferencesIOTest {
    @TempDir
    Path dir;

    @Test
    void replacesTheFileAndLeavesNoTemporaryFiles() throws Exception {
        Path file = dir.resolve("123.json");
        PreferencesIO.write(file, "{\"a\":1}");
        PreferencesIO.write(file, "{\"a\":2}");
        assertEquals("{\"a\":2}", Files.readString(file));
        try (var files = Files.list(dir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void nullContentDeletesTheFile() throws Exception {
        Path file = dir.resolve("123.json");
        PreferencesIO.write(file, "{}");
        PreferencesIO.write(file, null);
        assertFalse(Files.exists(file));
        PreferencesIO.write(file, null); // deleting a missing file is fine
    }

    @Test
    void queuedWritesApplyInOrder() throws Exception {
        Path file = dir.resolve("456.json");
        for (int i = 0; i < 50; i++) {
            PreferencesIO.submit(file, "{\"v\":" + i + "}", e -> {
                throw new AssertionError(e);
            });
        }
        PreferencesIO.flush(10, TimeUnit.SECONDS);
        assertEquals("{\"v\":49}", Files.readString(file));
    }
}
