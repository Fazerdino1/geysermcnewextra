package fr.crossplaymc.geysermcnewextra.core.cache;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
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
        SERVER.log(Lang.console("gmne.console.pack_checking"));
        try {
            Files.createDirectories(PACKS_FOLDER);
            Version3 latestVersion = checkExtrasPack();
            if (latestVersion != null) {
                downloadExtrasPack(latestVersion);
            }
        } catch (IOException | RuntimeException e) {
            // Offline or GitHub down: keep the cached pack if there is one, never stop the start
            SERVER.warn(Lang.console("gmne.console.pack_download_failed", e));
        }
        SERVER.log(Lang.console("gmne.console.loading_resources"));
        RP_EXTRAS_PACK = null;
        if (Files.exists(EXTRAS_PACK_FILE)) {
            try {
                RP_EXTRAS_PACK = ResourcePack.create(PackCodec.path(EXTRAS_PACK_FILE));
            } catch (RuntimeException e) {
                SERVER.warn(Lang.console("gmne.console.pack_unreadable", e));
            }
        }

        // Geyser's packs folder belongs to the admin: only warn about packs that are now redundant,
        // never delete them (based on h4-hub/GeyserMCNewExtra@a352538, MIT)
        Path geyserMCPacks = GE.geyserApi.packDirectory();
        if (geyserMCPacks.resolve(UpstreamResources.LEGACY_PACK_FILE_NAME).toFile().exists()) {
            SERVER.warn(Lang.console("gmne.console.legacy_pack_in_geyser", UpstreamResources.LEGACY_PACK_FILE_NAME));
        }
        if (geyserMCPacks.resolve("GeyserOptionalPack.mcpack").toFile().exists()) {
            SERVER.warn(Lang.console("gmne.console.optional_pack_in_geyser"));
        }
        // delete old GeyserOptionalPack from cache folder if it exists
        File oldOptionalPack = PACKS_FOLDER.resolve("GeyserOptionalPack.mcpack").toFile();
        if (oldOptionalPack.exists()) {
            if (oldOptionalPack.delete()) {
                SERVER.log(Lang.console("gmne.console.legacy_optional_pack_removed"));
            }
        }
    }

    /**
     * @return the version to download, or null when the cached pack is up to date
     */
    private static Version3 checkExtrasPack() {
        boolean missing = !Files.exists(EXTRAS_PACK_FILE);
        if (!missing && !GE.getConfig().isCheckForUpdates()) {
            return null;
        }
        Version3 latestVersion = getPackVersion(UpstreamResources.PACK_MANIFEST_URL);
        if (missing) {
            return latestVersion;
        }
        Version3 cachedVersion = Version3.fromArray(CACHE_DATES.lastExtrasPackVersion);
        return latestVersion.isNewer(cachedVersion) ? latestVersion : null;
    }

    /**
     * Downloads next to the pack then moves it: an interrupted download never replaces a
     * working pack, and the version is only remembered once the new pack is in place.
     */
    private static void downloadExtrasPack(Version3 version) throws IOException {
        SERVER.log(Lang.console("gmne.console.pack_downloading"));
        Path temp = PACKS_FOLDER.resolve("extras-pack.mcpack.tmp");
        try (InputStream in = HTTP.request(UpstreamResources.PACK_URL)) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
        try {
            Files.move(temp, EXTRAS_PACK_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, EXTRAS_PACK_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
        CACHE_DATES.lastExtrasPackVersion = version.asArray();
        Cache.saveCacheDates();
        SERVER.log(Lang.console("gmne.console.pack_downloaded"));
    }

    private static Version3 getPackVersion(String url) {
        try (InputStream in = HTTP.request(url)) {
            JsonNode manifestJson = JSON_MAPPER.readTree(in.readAllBytes());
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
        if (RP_EXTRAS_PACK == null) {
            // The pack could not be downloaded: the optional packs are still sent
            registerSelectedPacks(player, ev);
            return;
        }
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
                    SERVER.log(Lang.console("gmne.console.pack_via_cdn"));
                } else {
                    SERVER.log(Lang.console("gmne.console.pack_already_registered"));
                }
            } else {
                // register local copy of the extras resource pack
                int priority = GE.getConfig().getResourcePackPriority();
                // Geyser requires priority to be between -100 and 100
                if (priority < -100 || priority > 100) {
                    SERVER.warn(Lang.console("gmne.console.pack_priority_invalid", priority));
                    priority = 100;
                }
                ev.register(RP_EXTRAS_PACK, PriorityOption.priority(priority));
            }
        } catch (Exception ignored) {
        }
        registerSelectedPacks(player, ev);
    }

    private static void registerSelectedPacks(ExtrasPlayer player, SessionLoadResourcePacksEvent ev) {
        List<UUID> packsToLoad = player.getPreferences().getSelectedPacks();
        for (UUID pack : packsToLoad) {
            ResourcePack rp = PackLoader.PACKS.get(pack);
            if (rp == null) {
                // Pack removed from the packs folder since the player selected it
                continue;
            }
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
