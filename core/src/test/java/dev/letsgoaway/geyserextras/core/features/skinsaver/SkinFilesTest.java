package dev.letsgoaway.geyserextras.core.features.skinsaver;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinFilesTest {
    @Test
    void keepsNormalBedrockIds() {
        assertEquals("c18e65aa-7b21-4637-9b63-8ad63622ef01.Custom", SkinFiles.safeFolderName("c18e65aa-7b21-4637-9b63-8ad63622ef01.Custom"));
    }

    @Test
    void hashesIdsThatAreNotPlainNames() {
        for (String id : new String[]{"../../plugins/Essentials", "..", ".", "a/b", "a\\b", "", "x".repeat(65), "C:evil", null}) {
            String name = SkinFiles.safeFolderName(id);
            assertTrue(name.matches("id-[0-9a-f]{32}"), id + " -> " + name);
        }
        assertNotEquals(SkinFiles.safeFolderName("../a"), SkinFiles.safeFolderName("../b"));
    }

    @Test
    void resolveInsideRejectsEscapes() {
        Path base = Path.of("plugins/GeyserExtras/skins");
        assertNull(SkinFiles.resolveInside(base, "../../config.yml"));
        assertNull(SkinFiles.resolveInside(base, ".."));
        assertNull(SkinFiles.resolveInside(base, "."));
        assertTrue(SkinFiles.resolveInside(base, "abc").endsWith(Path.of("skins", "abc")));
    }

    @Test
    void validatesImageSizeAgainstData() {
        assertTrue(SkinFiles.isValidImage(64, 64, 64 * 64 * 4));
        assertFalse(SkinFiles.isValidImage(64, 64, 64 * 64 * 4 - 1));
        assertFalse(SkinFiles.isValidImage(0, 64, 0));
        assertFalse(SkinFiles.isValidImage(-1, 64, 1 << 20));
        assertFalse(SkinFiles.isValidImage(100_000, 100_000, Integer.MAX_VALUE));
    }
}
