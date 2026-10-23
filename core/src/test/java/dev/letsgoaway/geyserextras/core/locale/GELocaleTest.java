package dev.letsgoaway.geyserextras.core.locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GELocaleTest {
    @Test
    void translatesWithoutDownloadedLanguages() {
        // Upstream #57: with no downloaded language files, translate() threw and the menu never opened
        assertEquals("ge.welcome_toast.line1", GELocale.translate("ge.welcome_toast.line1", "fr_FR"));
        assertEquals("aucune", GELocale.translate("cpmc.emote_link.none", "fr_FR"));
    }
}
