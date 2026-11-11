package fr.crossplaymc.geysermcnewextra.core;

import fr.crossplaymc.geysermcnewextra.InitializeLogger;
import fr.crossplaymc.geysermcnewextra.Server;
import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.cache.Cache;
import fr.crossplaymc.geysermcnewextra.core.cache.PackCacheUtils;
import fr.crossplaymc.geysermcnewextra.core.cache.UpstreamResources;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.config.ConfigLoader;
import fr.crossplaymc.geysermcnewextra.core.config.DataFolderMigration;
import fr.crossplaymc.geysermcnewextra.core.config.GeyserMCNewExtraConfig;
import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import fr.crossplaymc.geysermcnewextra.core.parity.bedrock.EmoteUtils;
import fr.crossplaymc.geysermcnewextra.core.parity.java.blockdisplay.BlockDisplayEntity;
import fr.crossplaymc.geysermcnewextra.core.preferences.JavaPreferencesData;
import fr.crossplaymc.geysermcnewextra.core.preferences.PreferencesData;
import fr.crossplaymc.geysermcnewextra.core.preferences.PreferencesIO;
import fr.crossplaymc.geysermcnewextra.core.protocol.CapeLoader;
import fr.crossplaymc.geysermcnewextra.core.utils.IdUtils;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import fr.crossplaymc.geysermcnewextra.core.version.PluginVersion;
import lombok.Getter;
import lombok.Setter;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.bedrock.*;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineEntitiesEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreReloadEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserShutdownEvent;
import org.geysermc.geyser.session.GeyserSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GeyserMCNewExtra implements EventRegistrar {
    public static GeyserMCNewExtra GE;
    public static Server SERVER;
    public GeyserApi geyserApi;
    // Created up front: packet injectors can run before the constructor finishes (issues #29, #76)
    public final ConcurrentHashMap<String, ExtrasPlayer> connections = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<UUID, JavaPreferencesData> javaConnections = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private GeyserMCNewExtraConfig config;
    @Getter
    private boolean blockDisplayRegistered = false;

    public GeyserMCNewExtra(Server server) {
        GE = this;
        SERVER = server;
        ServerType.platformType = GeyserImpl.getInstance().platformType();

        IsAvailable.preload();

        // Bundled texts until the config (and so the server language) is loaded
        Lang.loadBundled(GeyserMCNewExtraConfig.DEFAULT_LOCALE);

        InitializeLogger.start();

        if (!IsAvailable.cloudburst()) {
            SERVER.warn(Lang.console("gmne.console.modded_unsupported"));
            SERVER.warn(Lang.console("gmne.console.use_standalone"));
            InitializeLogger.endNoDone();
            return;
        }



        geyserApi = GeyserApi.api();

        migrateLegacyDataFolder();

        SERVER.log(Lang.console("gmne.console.loading_config"));
        ConfigLoader.load();
        SERVER.onConfigLoad();
        loadLanguages();

        if (ServerType.isExtension()) {
            GeyserHandler.register();

            SERVER.log(Lang.console("gmne.console.initializing_cache"));
            // Independent steps: an offline cache (pack, credits) must not leave the emotes unloaded
            runSafely("cache", Cache::initialize);
            SERVER.log(Lang.console("gmne.console.loading_emotes"));
            runSafely("emotes", EmoteUtils::initialize);
        }

        PreferencesData.init();

        SERVER.log(Lang.console("gmne.console.registering_events"));
        geyserApi.eventBus().register(this, this);
        geyserApi.eventBus().subscribe(this, GeyserPostInitializeEvent.class, this::onGeyserInitialize);
        // Custom entity types must be registered here: Geyser freezes its entity registry right after this event
        geyserApi.eventBus().subscribe(this, GeyserDefineEntitiesEvent.class, this::onDefineEntities);

        // ExtrasPlayer handlers
        geyserApi.eventBus().subscribe(this, SessionJoinEvent.class, this::onSessionJoin);
        geyserApi.eventBus().subscribe(this, SessionDisconnectEvent.class, this::onSessionRemove);

        // Emote bindings
        geyserApi.eventBus().subscribe(this, ClientEmoteEvent.class, this::onEmoteEvent);

        // Auto reconnect
        geyserApi.eventBus().subscribe(this, GeyserPreReloadEvent.class, this::onGeyserReload);
        geyserApi.eventBus().subscribe(this, GeyserShutdownEvent.class, this::onGeyserShutdown);

        // Packs
        geyserApi.eventBus().subscribe(this, SessionLoadResourcePacksEvent.class, this::onLoadPacks);

        if (ServerType.isExtension()) {
            InitializeLogger.end();
        }

        PluginVersion.checkForUpdatesAndPrintToLog();
    }

    /**
     * Dont use this on proxys, only on servers
     * Tick individually based on tickrate for each player
     * on proxys
     */
    public void serverTick() {
        for (ExtrasPlayer player : connections.values()) {
            if (player.isLoggedIn()) {
                player.safeTick();
            }
        }
    }

    public void onGeyserInitialize(GeyserPostInitializeEvent init) {
        if (!ServerType.isExtension()) {
            GeyserHandler.register();
            SERVER.log(Lang.console("gmne.console.initializing_cache"));
            // Independent steps: an offline cache (pack, credits) must not leave the emotes unloaded
            runSafely("cache", Cache::initialize);
            SERVER.log(Lang.console("gmne.console.loading_emotes"));
            runSafely("emotes", EmoteUtils::initialize);
            InitializeLogger.end();
        }
    }

    /**
     * Extracts/completes {@code lang/*.yml} and loads them with the configured server language.
     */
    public void loadLanguages() {
        Path folder = SERVER.getPluginFolder().resolve("lang");
        java.util.Map<String, Integer> changes = new java.util.LinkedHashMap<>();
        try {
            Lang.load(folder, getConfig().getDefaultLocale(), changes::put);
        } catch (IOException e) {
            Lang.loadBundled(getConfig().getDefaultLocale());
            SERVER.warn(Lang.console("gmne.console.lang_folder_failed", folder, e));
            return;
        }
        changes.forEach((file, added) -> SERVER.log(added < 0
                ? Lang.console("gmne.console.lang_created", file)
                : Lang.console("gmne.console.lang_completed", file, added)));
    }

    private static void migrateLegacyDataFolder() {
        Path current = SERVER.getPluginFolder();
        // Spigot and Bungee name the folder after the plugin, Velocity and Geyser after the id
        String legacyName = (ServerType.type == ServerType.SPIGOT || ServerType.type == ServerType.BUNGEECORD)
                ? UpstreamResources.LEGACY_PLUGIN_FOLDER : UpstreamResources.LEGACY_ID;
        Path legacy = current.toAbsolutePath().getParent().resolve(legacyName);
        try {
            if (DataFolderMigration.copyLegacyFolder(legacy, current)) {
                SERVER.log(Lang.console("gmne.console.data_folder_copied", legacy, current));
            }
        } catch (IOException e) {
            SERVER.warn(Lang.console("gmne.console.data_folder_copy_failed", legacy, e));
        }
    }

    public void onDefineEntities(GeyserDefineEntitiesEvent ignored) {
        if (!getConfig().isEnableBlockDisplayWorkaround() || blockDisplayRegistered) {
            return;
        }
        try {
            BlockDisplayEntity.buildEntityDef();
            blockDisplayRegistered = true;
        } catch (Exception e) {
            SERVER.warn(Lang.console("gmne.console.block_display_failed", e));
        }
    }

    public void onSessionJoin(SessionJoinEvent ev) {
        SERVER.debug("SessionJoinEvent xuid=" + ev.connection().xuid() + " known=" + connections.containsKey(ev.connection().xuid()));
        ExtrasPlayer player = connections.get(ev.connection().xuid());
        if (player != null) {
            player.startGame();
        }

    }

    public void onSessionRemove(SessionDisconnectEvent ev) {
        GeyserConnection connection = ev.connection();
        // Match on the session itself, not the name: when the same player reconnects quickly,
        // the old session can close after the new one was created and must not remove it.
        // Never reconnect here: a disconnect can be a kick or a ban, it must be respected.
        for (ExtrasPlayer player : connections.values()) {
            GeyserSession session = player.getSession();
            if (session == connection) {
                // this occurs before bedrock authenticates properly
                if (session.getAuthData() == null && session.getClientData() == null) {
                    // we clear the players packs here as its possible that the game crashes when trying to load a pack
                    player.getPreferences().getSelectedPacks().clear();
                    player.getPreferences().save();
                }
                player.onDisconnect();
                connections.remove(player.getBedrockXUID(), player);
                return;
            }
        }
        // Not registered (the session closed before its packs were loaded) or already replaced
        SERVER.debug("SessionDisconnectEvent for an unknown session xuid=" + connection.xuid());
    }

    public void onEmoteEvent(ClientEmoteEvent ev) {
        ExtrasPlayer player = connections.get(ev.connection().xuid());
        if (player != null) {
            player.onEmoteEvent(ev);
        }
    }

    public void onGeyserReload(GeyserPreReloadEvent ignored) {
        runSafely("server stop", this::onServerStop);
    }

    public void onGeyserShutdown(GeyserShutdownEvent ignored) {
        runSafely("server stop", this::onServerStop);
    }

    public void onLoadPacks(SessionLoadResourcePacksEvent ev) {
        ExtrasPlayer player = SERVER.createPlayer(ev.connection());
        ExtrasPlayer previous = connections.put(ev.connection().xuid(), player);
        if (previous != null) {
            // Stop the tick task and boss bar of the session being replaced
            runSafely("replacing " + ev.connection().xuid(), previous::onDisconnect);
        }
        SERVER.debug("Created player for xuid=" + ev.connection().xuid() + " javaUuid=" + ev.connection().javaUuid());
        PackCacheUtils.onPackLoadEvent(player, ev);
    }

    public JavaPreferencesData getJavaPreferencesData(UUID javaUUID) {
        return javaConnections.get(javaUUID);
    }

    /**
     * Same as {@link #getJavaPreferencesData(UUID)}, but loads the preferences when they are
     * missing (e.g. the player ran a command before the join event, or after a cancelled kick).
     */
    public JavaPreferencesData getOrLoadJavaPreferencesData(UUID javaUUID) {
        return javaConnections.computeIfAbsent(javaUUID, JavaPreferencesData::load);
    }

    /**
     * {@code /ge reload}: reloads the config, the platform config, the languages and the
     * command aliases. Players stay connected; options read when a player joins apply to the
     * next connections.
     *
     * @return true if the config was read without error
     */
    public boolean reload() {
        boolean loaded = ConfigLoader.load();
        SERVER.onConfigLoad();
        loadLanguages();
        CommandExecutor.loadCommands();
        return loaded;
    }

    /**
     * Called when the server, the proxy or Geyser stops or reloads: saves every player's
     * preferences and waits for them to reach the disk, then reconnects the Bedrock players
     * if {@code auto-reconnect} is on. This is the only place players are reconnected:
     * a kick or a disconnect is always respected.
     */
    public void onServerStop() {
        // Can run from a shutdown after a failed start, when the config was never loaded
        boolean reconnect = getConfig() != null && getConfig().isAutoReconnect();
        for (ExtrasPlayer player : connections.values()) {
            runSafely("saving " + player.getBedrockXUID(), () -> player.getPreferences().save());
        }
        // The writer thread is a daemon: pending saves would be lost without this
        PreferencesIO.flush(5, TimeUnit.SECONDS);
        if (reconnect) {
            for (ExtrasPlayer player : connections.values()) {
                runSafely("reconnecting " + player.getBedrockXUID(), player::reconnect);
            }
        }
    }

    /**
     * Runs a shutdown or cleanup step so that its failure is logged instead of propagated:
     * disabling the plugin must never throw, even after a failed start.
     */
    public static void runSafely(String step, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException | LinkageError e) {
            if (SERVER != null) {
                SERVER.warn(Lang.console("gmne.console.step_failed", step, e));
            } else {
                System.err.println("[GeyserMCNewExtra] " + step + " failed: " + e);
            }
        }
    }

    // These are called from the seperate plugin classes and these aren't ever called on standalone / extension
    public void onJavaPlayerJoin(UUID javaUUID) {
        // this still saves sometimes if your a bedrock player anyway (doesnt really matter atm but pretty stupid that it happens)
        if (!IdUtils.isBedrockPlayer(javaUUID)) {
            javaConnections.put(javaUUID, JavaPreferencesData.load(javaUUID));
        }
    }

    public void onJavaPlayerLeave(UUID javaUUID) {
        JavaPreferencesData data = javaConnections.remove(javaUUID);
        if (data != null) {
            data.save();
        }
    }
}