package fr.crossplaymc.geysermcnewextra.core.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCheckerTest {
    private static JsonArray releases(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private static final JsonArray RELEASES = releases("""
            [
              {"tag_name": "v2.2.0-beta.1", "prerelease": true, "draft": false, "html_url": "u-beta"},
              {"tag_name": "v2.1.0", "prerelease": false, "draft": false, "html_url": "u-210"},
              {"tag_name": "v3.0.0", "prerelease": false, "draft": true, "html_url": "u-draft"},
              {"tag_name": "nightly", "prerelease": false, "draft": false, "html_url": "u-nightly"},
              {"tag_name": "v2.1.0-alpha.1", "prerelease": true, "draft": false, "html_url": "u-alpha"}
            ]
            """);

    @Test
    void stableServersOnlyGetStableReleases() {
        assertTrue(UpdateChecker.findNewer(SemVer.parse("2.1.0"), RELEASES).isEmpty());
        assertEquals("u-210", UpdateChecker.findNewer(SemVer.parse("2.0.0"), RELEASES).orElseThrow().url());
    }

    @Test
    void preReleaseServersGetTheHighestRelease() {
        UpdateChecker.Release release = UpdateChecker.findNewer(SemVer.parse("2.1.0-alpha.1"), RELEASES).orElseThrow();
        assertEquals("2.2.0-beta.1", release.version().toString());
    }

    @Test
    void ignoresDraftsAndUnknownTags() {
        assertTrue(UpdateChecker.findNewer(SemVer.parse("2.2.0-beta.1"), RELEASES).isEmpty());
    }
}
