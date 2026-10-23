package fr.crossplaymc.geysermcnewextra.core.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.crossplaymc.geysermcnewextra.ServerType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class PluginVersion {
    // Shown to admins and players
    public static final String DISPLAY_NAME = "GeyserMCNewExtra";

    // Must match `version` in gradle.properties (checked by the :core:checkPluginVersion task)
    public static final String GE_VERSION = "2.1.0-alpha.1";

    private static final boolean UPSTREAM_UPDATE_CHECK = false;
    // Set when the GeyserMCNewExtra repository publishes releases (the checker stays off until then)
    private static final String UPDATE_API = "";
    private static final String RELEASES_PAGE = "";

    public static String latestVersion = "";
    public static String latestVersionModrinthID = "";


    public static boolean checkForUpdate() {
        if (!GE.getConfig().isCheckForUpdates()) {
            return false;
        }

        try {
            URL url = new URL(UPDATE_API);
            URLConnection request = url.openConnection();
            request.setConnectTimeout(5000);
            request.setRequestProperty("User-Agent", "GeyserMCNewExtra/" + GE_VERSION);
            request.connect();
            JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));

            for (JsonElement version : root.getAsJsonArray()) {
                List<String> loadersArray = new ArrayList<>();
                JsonArray loaders = version.getAsJsonObject().get("loaders").getAsJsonArray();
                loaders.forEach(loader -> {
                    loadersArray.add(loader.getAsString());
                });
                if (loadersArray.contains(ServerType.type.name().toLowerCase()) || (ServerType.isExtension() && loadersArray.contains("geyser"))) {
                    latestVersion = version.getAsJsonObject().get("version_number").getAsString();
                    latestVersionModrinthID = version.getAsJsonObject().get("id").getAsString();
                    return !latestVersion.equals(GE_VERSION);
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static void checkForUpdatesAndPrintToLog() {
        if (!GE.getConfig().isCheckForUpdates()) {
            return;
        }

        // The Modrinth project above is the archived upstream: comparing our fork's version with it
        // would always report a bogus "new update". Skip until the fork's own releases exist.
        if (!UPSTREAM_UPDATE_CHECK) {
            SERVER.log("Update check skipped: " + DISPLAY_NAME + " has no release channel yet.");
            return;
        }

        new Thread(() -> {
            if (checkForUpdate()) {
                SERVER.warn("There is a new update to GeyserMCNewExtra!");
                SERVER.warn("You are on version " + GE_VERSION + " but the latest version is " + latestVersion + ".");
                SERVER.warn("Download & Changelog: " + RELEASES_PAGE);
            } else {
                SERVER.warn("GeyserMCNewExtra is on the latest version.");
            }
        }).start();
    }
}
