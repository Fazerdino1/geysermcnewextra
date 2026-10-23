package fr.crossplaymc.geysermcnewextra.core.cache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.crossplaymc.geysermcnewextra.core.preferences.CooldownTypeAdapter;
import org.geysermc.geyser.util.CooldownUtils;
import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.packs.PackLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class Cache {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CooldownUtils.CooldownType.class, new CooldownTypeAdapter())
            .create();

    public static Path CACHE_FOLDER;
    public static CacheDates CACHE_DATES;

    public static Path DATES_PATH;

    // Credits of the original project, bundled (they used to be downloaded at every start)
    public static String CREDITS_TEXT = "";

    public static void initialize() {
        CACHE_FOLDER = SERVER.getPluginFolder().resolve("cache/");
        DATES_PATH = CACHE_FOLDER.resolve("dates.json");

        try {
            Files.createDirectories(CACHE_FOLDER);
            if (Files.exists(DATES_PATH)) {
                CACHE_DATES = JSON_MAPPER.readValue(DATES_PATH.toFile(), CacheDates.class);
            } else {
                CACHE_DATES = new CacheDates();
                saveCacheDates();
            }
        } catch (IOException | RuntimeException e) {
            SERVER.warn(Lang.console("gmne.console.cache_unreadable", e));
            CACHE_DATES = new CacheDates();
        }

        loadCredits();

        SERVER.log(Lang.console("gmne.console.loading_resources"));
        PackCacheUtils.initialize();
        PackLoader.initialize();
    }

    public static void saveCacheDates() {
        try {
            JSON_MAPPER.writeValue(DATES_PATH.toFile(), CACHE_DATES);
        } catch (IOException e) {
            SERVER.warn(Lang.console("gmne.console.cache_unsaved", e));
        }
    }

    private static void loadCredits() {
        try (InputStream in = Cache.class.getClassLoader().getResourceAsStream("credits/original.txt")) {
            CREDITS_TEXT = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("&", "§");
        } catch (IOException e) {
            CREDITS_TEXT = "";
        }
    }
}
