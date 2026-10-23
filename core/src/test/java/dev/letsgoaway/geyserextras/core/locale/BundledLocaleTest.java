package dev.letsgoaway.geyserextras.core.locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BundledLocaleTest {
    @Test
    void usesTheExactLocale() {
        assertEquals("aucune", BundledLocale.translate("cpmc.emote_link.none", "fr_FR"));
        assertEquals("none", BundledLocale.translate("cpmc.emote_link.none", "en_US"));
    }

    @Test
    void fallsBackToTheSameLanguageThenEnglish() {
        assertEquals("aucune", BundledLocale.translate("cpmc.emote_link.none", "fr_CA"));
        assertEquals("none", BundledLocale.translate("cpmc.emote_link.none", "de_DE"));
        assertEquals("none", BundledLocale.translate("cpmc.emote_link.none", null));
    }

    @Test
    void unknownKeyReturnsTheKey() {
        assertEquals("cpmc.does_not_exist", BundledLocale.translate("cpmc.does_not_exist", "fr_FR"));
    }

    @Test
    void frenchHasExactlyTheEnglishKeys() {
        assertEquals(BundledLocale.keys("en_US"), BundledLocale.keys("fr_FR"));
    }
}
