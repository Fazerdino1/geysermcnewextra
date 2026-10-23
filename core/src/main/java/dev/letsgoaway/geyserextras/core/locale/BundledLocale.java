package dev.letsgoaway.geyserextras.core.locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Translations shipped inside the jar (resources/lang/cpmc/), for strings added by the fork
 * that the downloaded GeyserExtras language files do not have.
 */
public final class BundledLocale {
    private static final String DEFAULT_LOCALE = "en_US";
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    private BundledLocale() {
    }

    /**
     * @return the translation for the exact locale, else for another locale of the same language,
     * else in English, else the key itself
     */
    public static String translate(String key, String locale) {
        String value = load(locale).get(key);
        if (value == null && locale != null && locale.length() >= 2) {
            value = load(sameLanguage(locale)).get(key);
        }
        if (value == null) {
            value = load(DEFAULT_LOCALE).get(key);
        }
        return value != null ? value : key;
    }

    static java.util.Set<String> keys(String locale) {
        return load(locale).keySet();
    }

    private static String sameLanguage(String locale) {
        String language = locale.substring(0, 2).toLowerCase();
        return switch (language) {
            case "fr" -> "fr_FR";
            default -> DEFAULT_LOCALE;
        };
    }

    private static Map<String, String> load(String locale) {
        if (locale == null) {
            return Map.of();
        }
        return CACHE.computeIfAbsent(locale, BundledLocale::read);
    }

    private static Map<String, String> read(String locale) {
        Map<String, String> strings = new HashMap<>();
        try (InputStream stream = BundledLocale.class.getClassLoader().getResourceAsStream("lang/cpmc/" + locale + ".json")) {
            if (stream == null) {
                return strings;
            }
            JsonObject json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            for (String key : json.keySet()) {
                strings.put(key, json.get(key).getAsString());
            }
        } catch (IOException | RuntimeException e) {
            // A broken bundled file only means untranslated strings, never a failure
            return strings;
        }
        return strings;
    }
}
