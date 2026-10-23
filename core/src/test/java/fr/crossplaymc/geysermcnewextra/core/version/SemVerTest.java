package fr.crossplaymc.geysermcnewextra.core.version;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemVerTest {
    private static SemVer v(String text) {
        return SemVer.parse(text);
    }

    @Test
    void followsSemVerPrecedence() {
        // Example ordering from semver.org §11
        List<String> ordered = List.of("1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-alpha.beta", "1.0.0-beta",
                "1.0.0-beta.2", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0", "1.0.1", "1.1.0", "2.0.0", "10.0.0");
        for (int i = 0; i + 1 < ordered.size(); i++) {
            assertTrue(v(ordered.get(i)).compareTo(v(ordered.get(i + 1))) < 0, ordered.get(i) + " < " + ordered.get(i + 1));
        }
    }

    @Test
    void lowerMinorWithHigherPatchIsOlder() {
        // The old Version3.isNewer said 1.5.0 is newer than 2.0.0
        assertTrue(v("1.5.0").compareTo(v("2.0.0")) < 0);
    }

    @Test
    void parsesTagsAndRejectsGarbage() {
        assertEquals("2.1.0-alpha.1", v("v2.1.0-alpha.1").toString());
        assertEquals(0, v("1.0.0+build.5").compareTo(v("1.0.0")));
        assertNull(v("2.0.0-BETA_11"));
        assertNull(v("latest"));
        assertNull(v(null));
    }
}
