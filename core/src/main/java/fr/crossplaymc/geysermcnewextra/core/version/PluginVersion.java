package fr.crossplaymc.geysermcnewextra.core.version;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class PluginVersion {
    // Shown to admins and players
    public static final String DISPLAY_NAME = "GeyserMCNewExtra";

    // Must match `version` in gradle.properties (checked by the :core:checkPluginVersion task)
    public static final String GE_VERSION = "2.1.0-alpha.1";

    /**
     * Asks GitHub for this project's releases and returns a newer one, if any.
     */
    public static Optional<UpdateChecker.Release> checkForUpdate() throws IOException {
        SemVer current = SemVer.parse(GE_VERSION);
        if (current == null) {
            return Optional.empty();
        }
        URLConnection request = new URL(UpdateChecker.RELEASES_API).openConnection();
        request.setConnectTimeout(5000);
        request.setReadTimeout(5000);
        request.setRequestProperty("User-Agent", DISPLAY_NAME + "/" + GE_VERSION);
        request.setRequestProperty("Accept", "application/vnd.github+json");
        try (InputStreamReader reader = new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            return root.isJsonArray() ? UpdateChecker.findNewer(current, root.getAsJsonArray()) : Optional.empty();
        }
    }

    public static void checkForUpdatesAndPrintToLog() {
        if (!GE.getConfig().isCheckForUpdates()) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                checkForUpdate().ifPresentOrElse(release -> {
                    SERVER.warn(Lang.console("gmne.console.update_available", release.version(), GE_VERSION));
                    SERVER.warn(Lang.console("gmne.console.update_download", release.url()));
                }, () -> SERVER.log(Lang.console("gmne.console.update_up_to_date", GE_VERSION)));
            } catch (IOException | RuntimeException e) {
                // Offline servers or GitHub rate limits must not look like "up to date"
                SERVER.log(Lang.console("gmne.console.update_check_failed", e.getMessage()));
            }
        }, DISPLAY_NAME + " update checker");
        thread.setDaemon(true);
        thread.start();
    }
}
