package fr.crossplaymc.geysermcnewextra.core.cache;

/**
 * The resource pack still downloaded from the original project's repository (MIT), and the names it
 * used on disk. Its language files and credits are bundled in the jar instead.
 */
public final class UpstreamResources {
    private static final String ORG = "GeyserExtras";

    /** The resource pack with the combat sounds and the ping/form glyphs. */
    public static final String PACK_URL = "https://raw.githubusercontent.com/" + ORG + "/" + ORG + "Pack/main/" + ORG + "Pack.mcpack";
    public static final String PACK_MANIFEST_URL = "https://raw.githubusercontent.com/" + ORG + "/" + ORG + "Pack/main/src/pack/manifest.json";

    /** File name of that pack when an admin copied it into Geyser's packs folder. */
    public static final String LEGACY_PACK_FILE_NAME = ORG + "Pack.mcpack";
    /** Data folder name of the original plugin (Spigot/Bungee) and extension/Velocity id, for migration. */
    public static final String LEGACY_PLUGIN_FOLDER = ORG;
    public static final String LEGACY_ID = ORG.toLowerCase();

    private UpstreamResources() {
    }
}
