package fr.crossplaymc.geysermcnewextra.core.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangTest {
    @TempDir
    Path folder;

    private Map<String, Integer> load(String defaultLocale) throws Exception {
        Map<String, Integer> changes = new HashMap<>();
        Lang.load(folder, defaultLocale, changes::put);
        return changes;
    }

    @Test
    void extractsEveryBundledLanguage() throws Exception {
        Map<String, Integer> changes = load("fr_FR");
        assertEquals(-1, changes.get("fr_FR.yml"));
        assertEquals(-1, changes.get("en_US.yml"));
        assertTrue(Files.exists(folder.resolve("ja_JP.yml")));
        assertTrue(load("fr_FR").isEmpty(), "a second start changes nothing");
    }

    @Test
    void appendsMissingKeysAndKeepsAdminEdits() throws Exception {
        load("fr_FR");
        Path fr = folder.resolve("fr_FR.yml");
        String edited = Files.readString(fr)
                .replace("gmne.reconnect: \"Reconnecter\"", "gmne.reconnect: \"Reconnexion CrossPlayMC\"")
                .lines().filter(line -> !line.startsWith("gmne.menu:")).reduce("", (a, b) -> a + b + "\n");
        Files.writeString(fr, edited);

        assertEquals(1, load("fr_FR").get("fr_FR.yml"));
        assertTrue(Files.readString(fr).contains("gmne.menu:"));
        assertEquals("Reconnexion CrossPlayMC", Lang.get("gmne.reconnect", "fr_FR"));
    }

    @Test
    void followsThePlayerLocaleThenTheServerDefault() throws Exception {
        load("fr_FR");
        assertEquals("aucune", Lang.get("gmne.emote_link.none", "fr_FR"));
        assertEquals("aucune", Lang.get("gmne.emote_link.none", "fr_BE"), "same language, other region");
        assertEquals("none", Lang.get("gmne.emote_link.none", "en_US"));
        // de_DE has no file and ja_JP has no emote texts: both fall back to the server default (French)
        assertEquals("aucune", Lang.get("gmne.emote_link.none", "de_DE"));
        assertEquals("aucune", Lang.get("gmne.emote_link.none", "ja_JP"));
        assertEquals("aucune", Lang.console("gmne.emote_link.none"));
        assertEquals("gmne.unknown", Lang.get("gmne.unknown", "fr_FR"));
    }

    @Test
    void addedLanguageFilesAreUsed() throws Exception {
        load("fr_FR");
        Files.writeString(folder.resolve("de_DE.yml"), "gmne.emote_link.none: \"keine\"\n");
        load("fr_FR");
        assertEquals("keine", Lang.get("gmne.emote_link.none", "de_DE"));
        assertEquals("GeyserMCNewExtra", Lang.get("gmne.name", "de_DE"), "missing texts fall back");
    }

    @Test
    void substitutesAndFormats() throws Exception {
        load("en_US");
        assertEquals("GeyserMCNewExtra Menu", Lang.get("gmne.menu", "en_US"));
        assertEquals("Emote 1 is now linked to the emote Wave.", Lang.get("gmne.emote_link.done", "en_US", "Emote 1", "Wave"));
        assertEquals("l'emote", Lang.format("l'{0}", "emote"), "apostrophes are kept (no MessageFormat)");
    }

    @Test
    void quotesValuesForYaml() throws Exception {
        String value = "Line \"1\"\nC:\\path";
        assertEquals(value, Lang.readText("k: " + Lang.quote(value) + "\n").get("k"));
    }

    @Test
    void bundledFrenchAndEnglishHaveTheSameKeys() {
        assertEquals(Lang.bundled("en_US").keySet(), Lang.bundled("fr_FR").keySet());
        assertFalse(Lang.bundledLocales().isEmpty());
        assertTrue(Lang.candidates("fr_FR").containsAll(List.of("fr_FR", Lang.FALLBACK_LOCALE)));
    }
}
