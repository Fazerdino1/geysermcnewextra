package fr.crossplaymc.geysermcnewextra.core.parity.java.menus.packs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackLoaderTest {
    @Test
    void acceptsMcpackAndZip() {
        assertTrue(PackLoader.isPackFile("Faithful.mcpack"));
        assertTrue(PackLoader.isPackFile("Faithful.zip"));
        assertTrue(PackLoader.isPackFile("UPPER.ZIP"));
    }

    @Test
    void ignoresOtherFiles() {
        assertFalse(PackLoader.isPackFile("readme.txt"));
        assertFalse(PackLoader.isPackFile("pack.zip.bak"));
        assertFalse(PackLoader.isPackFile("mcpack"));
    }
}
