package fr.crossplaymc.geysermcnewextra.core.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

/**
 * Picks the release to recommend from the GitHub releases API response.
 */
public final class UpdateChecker {
    public static final String RELEASES_API = "https://api.github.com/repos/Fazerdino1/geysermcnewextra/releases?per_page=30";
    public static final String RELEASES_PAGE = "https://github.com/Fazerdino1/geysermcnewextra/releases";

    public record Release(SemVer version, String url) {
    }

    private UpdateChecker() {
    }

    /**
     * @return the highest published release newer than {@code current}. Pre-releases are only
     * offered to servers already running a pre-release; drafts and unparsable tags are ignored.
     */
    public static Optional<Release> findNewer(SemVer current, JsonArray releases) {
        Release best = null;
        for (JsonElement element : releases) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject release = element.getAsJsonObject();
            if (bool(release, "draft") || (bool(release, "prerelease") && !current.isPreRelease())) {
                continue;
            }
            SemVer version = SemVer.parse(string(release, "tag_name"));
            if (version == null || version.compareTo(current) <= 0) {
                continue;
            }
            if (best == null || version.compareTo(best.version()) > 0) {
                String url = string(release, "html_url");
                best = new Release(version, url != null ? url : RELEASES_PAGE);
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean bool(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() && object.get(key).getAsBoolean();
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
    }
}
