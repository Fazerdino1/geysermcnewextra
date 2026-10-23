package fr.crossplaymc.geysermcnewextra.core.cache;

/**
 * Resources still downloaded from the original project's repositories (MIT), and the names it used
 * on disk. Kept in one place so they can be replaced by our own hosting/bundled copies.
 */
public final class UpstreamResources {
    private static final String ORG = "GeyserExtras";

    /** Language files and credits. */
    public static final String DATA_REPO = "https://raw.githubusercontent.com/" + ORG + "/data/main/";
    public static final String DATA_BRANCH_API = "https://api.github.com/repos/" + ORG + "/data/branches/main";
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
