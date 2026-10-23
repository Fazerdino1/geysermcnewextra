package fr.crossplaymc.geysermcnewextra.core.preferences;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;
import static fr.crossplaymc.geysermcnewextra.core.cache.Cache.JSON_MAPPER;

public class JavaPreferencesData {
    @JsonIgnore
    public static Path JAVA_PREFRENCES_PATH;
    @JsonIgnore
    public static JavaPreferencesData DEFAULT;
    @JsonIgnore
    public UUID javaUUID;
    @JsonIgnore
    public File saveFile;
    public boolean muteEmoteChat = false;


    public JavaPreferencesData() {
        this.javaUUID = null;
    }

    public JavaPreferencesData(UUID javaUUID) {
        this.javaUUID = javaUUID;
        this.saveFile = JAVA_PREFRENCES_PATH.resolve(javaUUID.toString() + ".json").toFile();
        if (saveFile.exists()) {
            try {
                FileInputStream data = new FileInputStream(saveFile);

                this.copyFrom(JSON_MAPPER.convertValue(JSON_MAPPER.readTree(data.readAllBytes()), JavaPreferencesData.class));
            } catch (Exception e) {
                SERVER.warn(Lang.console("gmne.console.prefs_load_failed", javaUUID, e.getLocalizedMessage()));

                this.copyFrom(DEFAULT);
            }
        } else {
            this.copyFrom(DEFAULT);
        }
    }

    public static void init() {
        JAVA_PREFRENCES_PATH = PreferencesData.PREFERENCES_PATH.resolve("java/");
        try {
            Files.createDirectories(JAVA_PREFRENCES_PATH);
        } catch (Exception ignored) {
        }

        Path defaultPath = JAVA_PREFRENCES_PATH.resolve("default.json");
        DEFAULT = new JavaPreferencesData();
        try {
            if (defaultPath.toFile().exists()) {
                FileInputStream data = new FileInputStream(defaultPath.toFile());
                // Copy from because defaults might be different
                DEFAULT.copyFrom(JSON_MAPPER.convertValue(JSON_MAPPER.readTree(data.readAllBytes()), JavaPreferencesData.class));
            }
            JSON_MAPPER.writeValue(defaultPath.toFile(), DEFAULT);
        } catch (Exception e) {
            SERVER.warn(Lang.console("gmne.console.prefs_default_load_failed", e.getLocalizedMessage(), "java/default.json"));
        }
    }

    public static JavaPreferencesData load(UUID javaUUID) {
        return new JavaPreferencesData(javaUUID);
    }

    public void copyFrom(JavaPreferencesData data) {
        this.muteEmoteChat = data.muteEmoteChat;
    }

    public void save() {
        try {
            String json = JSON_MAPPER.writeValueAsString(this);
            String content = json.equals(JSON_MAPPER.writeValueAsString(DEFAULT)) ? null : json;
            PreferencesIO.submit(saveFile.toPath(), content, e ->
                    SERVER.warn(Lang.console("gmne.console.prefs_save_failed", javaUUID, e)));
        } catch (IOException e) {
            SERVER.warn(Lang.console("gmne.console.prefs_save_failed", javaUUID, e));
        }
    }
}
