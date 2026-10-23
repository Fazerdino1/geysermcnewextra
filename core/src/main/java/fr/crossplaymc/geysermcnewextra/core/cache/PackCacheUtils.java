package fr.crossplaymc.geysermcnewextra.core.cache;

import com.fasterxml.jackson.databind.JsonNode;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.packs.PackLoader;
import fr.crossplaymc.geysermcnewextra.core.version.Version3;
import org.geysermc.api.Geyser;
import org.geysermc.geyser.api.event.bedrock.SessionLoadResourcePacksEvent;
import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.pack.option.PriorityOption;
import org.geysermc.geyser.api.pack.option.SubpackOption;

import org.geysermc.geyser.api.pack.UrlPackCodec;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;
import static fr.crossplaymc.geysermcnewextra.core.cache.Cache.CACHE_DATES;
import static fr.crossplaymc.geysermcnewextra.core.cache.Cache.JSON_MAPPER;

public class PackCacheUtils {
    public static Path PACKS_FOLDER;
    public static Path EXTRAS_PACK_FILE;
    public static ResourcePack RP_EXTRAS_PACK;

    public static void initialize() {
        PACKS_FOLDER = Cache.CACHE_FOLDER.resolve("packs/");
        EXTRAS_PACK_FILE = PACKS_FOLDER.resolve("extras-pack.mcpack");
        SERVER.log("Checking for pack updates...");
        try {
            Files.createDirectories(PACKS_FOLDER);
            boolean extrasPackNeedsUpdate = checkExtrasPack();
            if (extrasPackNeedsUpdate) {
                Cache.saveCacheDates();

                SERVER.log("Downloading extras resource pack...");
                InputStream in = HTTP.request(UpstreamResources.PACK_URL);
                Files.copy(in, EXTRAS_PACK_FILE, StandardCopyOption.REPLACE_EXISTING);
                SERVER.log("extras resource pack downloaded!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while downloading resources!", e);
        }
        SERVER.log("Loading resources...");
        RP_EXTRAS_PACK = ResourcePack.create(PackCodec.path(EXTRAS_PACK_FILE));

        // Geyser's packs folder belongs to the admin: only warn about packs that are now redundant,
        // never delete them (based on h4-hub/GeyserMCNewExtra@a352538, MIT)
        Path geyserMCPacks = GE.geyserApi.packDirectory();
        if (geyserMCPacks.resolve(UpstreamResources.LEGACY_PACK_FILE_NAME).toFile().exists()) {
            SERVER.warn(UpstreamResources.LEGACY_PACK_FILE_NAME + " is in Geyser's packs folder; GeyserMCNewExtra already sends it, you can remove that copy.");
        }
        if (geyserMCPacks.resolve("GeyserOptionalPack.mcpack").toFile().exists()) {
            SERVER.warn("GeyserOptionalPack.mcpack is in Geyser's packs folder; it is deprecated (Geyser now ships the GeyserIntegratedPack) and may conflict with it.");
        }
        // delete old GeyserOptionalPack from cache folder if it exists
        File oldOptionalPack = PACKS_FOLDER.resolve("GeyserOptionalPack.mcpack").toFile();
        if (oldOptionalPack.exists()) {
            if (oldOptionalPack.delete()) {
                SERVER.log("Removed deprecated GeyserOptionalPack.mcpack from cache");
            }
        }
    }

    private static boolean checkExtrasPack() {
        // Download the pack if it exists
        if (!EXTRAS_PACK_FILE.toFile().exists()) {
            return true;
        }

        // Otherwise check for updates
        if (!GE.getConfig().isCheckForUpdates()) {
            return false;
        }
        Version3 oldVersion = Version3.fromArray(CACHE_DATES.lastExtrasPackVersion);
        Version3 latestExtrasPackVersion = getPackVersion(UpstreamResources.PACK_MANIFEST_URL);
        CACHE_DATES.lastExtrasPackVersion = latestExtrasPackVersion.asArray();
        return latestExtrasPackVersion.isNewer(oldVersion);
    }

    private static Version3 getPackVersion(String url) {
        try {
            byte[] data = new URL(url).openStream().readAllBytes();
            JsonNode manifestJson = JSON_MAPPER.readTree(data);
            ArrayList<Integer> versionArray = new ArrayList<>();
            for (JsonNode node : manifestJson.get("header").get("version")) {
                versionArray.add(node.asInt());
            }
            return Version3.fromList(versionArray);
        } catch (Exception e) {
            return Version3.fromString("0.0.0");
        }
    }

    public static void onPackLoadEvent(ExtrasPlayer player, SessionLoadResourcePacksEvent ev) {
        try {
            UUID extrasPackUuid = RP_EXTRAS_PACK.manifest().header().uuid();

            // check if extras resource pack is already registered (e.g. via CDN/UrlPackCodec)
            ResourcePack existingPack = null;
            for (ResourcePack pack : ev.resourcePacks()) {
                if (pack.manifest().header().uuid().equals(extrasPackUuid)) {
                    existingPack = pack;
                    break;
                }
            }

            if (existingPack != null) {
                // pack already registered - check if it's via CDN
                if (existingPack.codec() instanceof UrlPackCodec) {
                    SERVER.log("extras resource pack detected via CDN (UrlPackCodec), skipping local registration.");
                } else {
                    SERVER.log("extras resource pack already registered, skipping.");
                }
            } else {
                // register local copy of the extras resource pack
                int priority = GE.getConfig().getResourcePackPriority();
                // Geyser requires priority to be between -100 and 100
                if (priority < -100 || priority > 100) {
                    SERVER.warn("extras resource pack priority " + priority + " is out of valid range (-100 to 100). Using default value of 100.");
                    priority = 100;
                }
                ev.register(RP_EXTRAS_PACK, PriorityOption.priority(priority));
            }
        } catch (Exception ignored) {
        }
        List<UUID> packsToLoad = player.getPreferences().getSelectedPacks();
        for (UUID pack : packsToLoad) {
            ResourcePack rp = PackLoader.PACKS.get(pack);
            try {
                if (ev.resourcePacks().contains(rp)) {
                    continue;
                }
                String subpack = "";
                if (player.getPreferences().getSelectedSubpacks().containsKey(pack)) {
                    subpack = player.getPreferences().getSelectedSubpacks().get(pack);
                }

                ev.register(rp, PriorityOption.priority(99 - packsToLoad.indexOf(pack)), SubpackOption.named(subpack));
            } catch (Exception ignored) {
            }
        }
    }
}
