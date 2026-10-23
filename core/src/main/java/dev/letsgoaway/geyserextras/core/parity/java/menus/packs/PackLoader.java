package dev.letsgoaway.geyserextras.core.parity.java.menus.packs;

import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static dev.letsgoaway.geyserextras.core.GeyserExtras.GE;
import static dev.letsgoaway.geyserextras.core.GeyserExtras.SERVER;

public class PackLoader {
    public static Path PACKS_PATH;

    public static LinkedHashMap<UUID, ResourcePack> PACKS = new LinkedHashMap<>();

    public static void initialize() {
        PACKS_PATH = SERVER.getPluginFolder().resolve("optionalpacks/");
        try {
            Files.createDirectories(PACKS_PATH);
        } catch (IOException ignored) {
        }
        File packFolder = PACKS_PATH.toFile();
        File[] packs = packFolder.listFiles();

        if (packFolder.exists() && packs != null) {
            Arrays.sort(packs);
            for (File pack : packs) {
                if (!isPackFile(pack.getName())) {
                    continue;
                }
                // One broken pack must not prevent the others (or GeyserExtras) from loading
                try {
                    ResourcePack mcpack = ResourcePack.create(PackCodec.path(pack.toPath()));
                    SERVER.log(mcpack.manifest().header().name() + " loaded!");
                    PACKS.put(mcpack.manifest().header().uuid(), mcpack);
                } catch (Exception e) {
                    SERVER.warn("Skipping optional pack " + pack.getName() + ": " + e);
                }
            }
        }
    }

    /**
     * Optional packs can be .mcpack or .zip files, like in Geyser's own packs folder (upstream #60).
     */
    public static boolean isPackFile(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".mcpack") || lower.endsWith(".zip");
    }

    // https://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }
}
