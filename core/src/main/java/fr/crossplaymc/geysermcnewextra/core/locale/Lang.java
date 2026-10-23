package fr.crossplaymc.geysermcnewextra.core.locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Every text the plugin shows, to players and in the console, comes from the language files in
 * {@code lang/<locale>.yml} of the data folder. Defaults ship inside the jar; on startup the files are
 * extracted, older files get the keys they miss appended, and admins can edit any text or add a
 * language by adding a file.
 * <p>
 * Lookup for a player: their client locale, then another locale of the same language, then the
 * server's default locale, then English, then the key itself.
 */
public final class Lang {
    public static final String FALLBACK_LOCALE = "en_US";
    private static final String RESOURCE_FOLDER = "lang/";
    private static final int MAX_SUBSTITUTION_DEPTH = 8;

    private static final Map<String, Map<String, String>> LANGUAGES = new ConcurrentHashMap<>();
    private static volatile String defaultLocale = FALLBACK_LOCALE;

    private Lang() {
    }

    /**
     * Extracts/updates the language files in {@code folder} and loads every {@code *.yml} in it.
     *
     * @param changes receives (file name, number of texts added), with -1 for a file that was created
     */
    public static void load(Path folder, String serverDefaultLocale, BiConsumer<String, Integer> changes) throws IOException {
        Files.createDirectories(folder);
        for (String locale : bundledLocales()) {
            Map<String, String> bundled = bundled(locale);
            Path file = folder.resolve(locale + ".yml");
            if (!Files.exists(file)) {
                try (InputStream in = resource(locale + ".yml")) {
                    Files.copy(in, file);
                }
                changes.accept(file.getFileName().toString(), -1);
                continue;
            }
            int added = appendMissing(file, bundled, readFile(file));
            if (added > 0) {
                changes.accept(file.getFileName().toString(), added);
            }
        }

        Map<String, Map<String, String>> loaded = new LinkedHashMap<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.yml")) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                loaded.put(name.substring(0, name.length() - ".yml".length()), readFile(file));
            }
        }
        LANGUAGES.clear();
        LANGUAGES.putAll(loaded);
        defaultLocale = serverDefaultLocale == null || serverDefaultLocale.isBlank() ? FALLBACK_LOCALE : serverDefaultLocale;
    }

    /**
     * Loads only the defaults bundled in the jar (no data folder), e.g. before the plugin is set up.
     */
    public static void loadBundled(String serverDefaultLocale) {
        for (String locale : bundledLocales()) {
            LANGUAGES.putIfAbsent(locale, bundled(locale));
        }
        defaultLocale = serverDefaultLocale == null || serverDefaultLocale.isBlank() ? FALLBACK_LOCALE : serverDefaultLocale;
    }

    public static String defaultLocale() {
        return defaultLocale;
    }

    /**
     * A text in the server's default locale, for the console.
     */
    public static String console(String key, Object... args) {
        return get(key, defaultLocale, args);
    }

    /**
     * @param locale the player's client locale (e.g. {@code fr_FR}), or null for the server default
     */
    public static String get(String key, @Nullable String locale, Object... args) {
        String text = lookup(key, locale);
        return format(substitute(text, locale, 0), args);
    }

    /**
     * @return whether a text exists for this key in any loaded language
     */
    public static boolean has(String key) {
        return LANGUAGES.values().stream().anyMatch(texts -> texts.containsKey(key));
    }

    private static String lookup(String key, @Nullable String locale) {
        for (String candidate : candidates(locale)) {
            Map<String, String> texts = LANGUAGES.get(candidate);
            if (texts != null && texts.get(key) != null) {
                return texts.get(key);
            }
        }
        return key;
    }

    static List<String> candidates(@Nullable String locale) {
        List<String> candidates = new ArrayList<>();
        if (locale != null && !locale.isBlank()) {
            String normalized = normalize(locale);
            candidates.add(normalized);
            String language = normalized.split("_")[0] + "_";
            LANGUAGES.keySet().stream().filter(l -> l.startsWith(language)).sorted().forEach(candidates::add);
        }
        candidates.add(defaultLocale);
        candidates.add(FALLBACK_LOCALE);
        return candidates.stream().distinct().toList();
    }

    // Bedrock sends locales like "fr_FR"; tolerate "fr-fr" and other casings
    private static String normalize(String locale) {
        String[] parts = locale.replace('-', '_').split("_");
        return parts.length >= 2 ? parts[0].toLowerCase(Locale.ROOT) + "_" + parts[1].toUpperCase(Locale.ROOT) : parts[0].toLowerCase(Locale.ROOT);
    }

    // "/{key}/" inserts another text, e.g. the plugin name
    private static String substitute(String text, @Nullable String locale, int depth) {
        if (depth >= MAX_SUBSTITUTION_DEPTH || !text.contains("/{")) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (true) {
            int start = text.indexOf("/{", index);
            int end = start < 0 ? -1 : text.indexOf("}/", start);
            if (start < 0 || end < 0) {
                result.append(text, index, text.length());
                return result.toString();
            }
            result.append(text, index, start);
            String inner = text.substring(start + 2, end);
            result.append(substitute(lookup(inner, locale), locale, depth + 1));
            index = end + 2;
        }
    }

    static String format(String text, Object... args) {
        String result = text;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }

    static int appendMissing(Path file, Map<String, String> defaults, Map<String, String> current) throws IOException {
        Map<String, String> missing = new TreeMap<>();
        defaults.forEach((key, value) -> {
            if (!current.containsKey(key)) {
                missing.put(key, value);
            }
        });
        if (missing.isEmpty()) {
            return 0;
        }
        StringBuilder text = new StringBuilder();
        String existing = Files.readString(file, StandardCharsets.UTF_8);
        if (!existing.isEmpty() && !existing.endsWith("\n")) {
            text.append('\n');
        }
        text.append("\n# Added automatically: texts that did not exist when this file was created\n");
        missing.forEach((key, value) -> text.append(key).append(": ").append(quote(value)).append('\n'));
        Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        return missing.size();
    }

    static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    static Map<String, String> readFile(Path file) throws IOException {
        return read(YamlConfigurationLoader.builder().path(file).build().load());
    }

    static Map<String, String> readText(String yaml) throws IOException {
        return read(YamlConfigurationLoader.builder().source(() -> new BufferedReader(new StringReader(yaml))).build().load());
    }

    private static Map<String, String> read(ConfigurationNode root) {
        Map<String, String> texts = new LinkedHashMap<>();
        root.childrenMap().forEach((key, node) -> {
            String value = node.getString();
            if (value != null) {
                texts.put(String.valueOf(key), value);
            }
        });
        return texts;
    }

    static List<String> bundledLocales() {
        try (InputStream in = resource("languages.json")) {
            JsonArray languages = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
            List<String> locales = new ArrayList<>();
            for (JsonElement language : languages) {
                locales.add(language.getAsJsonArray().get(0).getAsString());
            }
            return locales;
        } catch (IOException | RuntimeException e) {
            return List.of(FALLBACK_LOCALE);
        }
    }

    static Map<String, String> bundled(String locale) {
        try (InputStream in = resource(locale + ".yml")) {
            return readText(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            return Collections.emptyMap();
        }
    }

    private static InputStream resource(String name) throws IOException {
        InputStream in = Lang.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + name);
        if (in == null) {
            throw new IOException("Missing bundled language resource " + RESOURCE_FOLDER + name);
        }
        return in;
    }
}
