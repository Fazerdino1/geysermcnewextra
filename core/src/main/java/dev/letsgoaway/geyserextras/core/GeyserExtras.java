package dev.letsgoaway.geyserextras.core;

import dev.letsgoaway.geyserextras.InitializeLogger;
import dev.letsgoaway.geyserextras.Server;
import dev.letsgoaway.geyserextras.ServerType;
import dev.letsgoaway.geyserextras.core.cache.Cache;
import dev.letsgoaway.geyserextras.core.cache.PackCacheUtils;
import dev.letsgoaway.geyserextras.core.config.ConfigLoader;
import dev.letsgoaway.geyserextras.core.config.GeyserExtrasConfig;
import dev.letsgoaway.geyserextras.core.injectors.GeyserHandler;
import dev.letsgoaway.geyserextras.core.parity.bedrock.EmoteUtils;
import dev.letsgoaway.geyserextras.core.parity.java.blockdisplay.BlockDisplayEntity;
import dev.letsgoaway.geyserextras.core.preferences.JavaPreferencesData;
import dev.letsgoaway.geyserextras.core.preferences.PreferencesData;
import dev.letsgoaway.geyserextras.core.protocol.CapeLoader;
import dev.letsgoaway.geyserextras.core.utils.IdUtils;
import dev.letsgoaway.geyserextras.core.utils.IsAvailable;
import dev.letsgoaway.geyserextras.core.version.PluginVersion;
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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GeyserExtras implements EventRegistrar {
    public static GeyserExtras GE;
    public static Server SERVER;
    public GeyserApi geyserApi;
    // Created up front: packet injectors can run before the constructor finishes (issues #29, #76)
    public final ConcurrentHashMap<String, ExtrasPlayer> connections = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<UUID, JavaPreferencesData> javaConnections = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private GeyserExtrasConfig config;
    @Getter
    private boolean blockDisplayRegistered = false;

    public GeyserExtras(Server server) {
        GE = this;
        SERVER = server;
        ServerType.platformType = GeyserImpl.getInstance().platformType();

        IsAvailable.preload();

        InitializeLogger.start();

        if (!IsAvailable.cloudburst()) {
            SERVER.warn("!ERROR! GeyserExtras currently does not support running as an extension on modded platforms. !ERROR!");
            SERVER.warn("Please use Geyser-Standalone!");
            InitializeLogger.endNoDone();
            return;
        }



        geyserApi = GeyserApi.api();

        SERVER.log("Loading config...");
        ConfigLoader.load();
        SERVER.onConfigLoad();

        if (ServerType.isExtension()) {
            GeyserHandler.register();

            SERVER.log("Initializing cache...");
            Cache.initialize();
            SERVER.log("Loading Emote Data...");
            EmoteUtils.initialize();
        }

        PreferencesData.init();

        SERVER.log("Registering events...");
        geyserApi.eventBus().register(this, this);
        geyserApi.eventBus().subscribe(this, GeyserPostInitializeEvent.class, this::onGeyserInitialize);
        // Custom entity types must be registered here: Geyser freezes its entity registry right after this event
        geyserApi.eventBus().subscribe(this, GeyserDefineEntitiesEvent.class, this::onDefineEntities);

        // ExtrasPlayer handlers
        geyserApi.eventBus().subscribe(this, SessionLoginEvent.class, this::onSessionLogin);
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
                player.tick();
            }
        }
    }

    public void onGeyserInitialize(GeyserPostInitializeEvent init) {
        if (!ServerType.isExtension()) {
            GeyserHandler.register();
            SERVER.log("Initializing cache...");
            Cache.initialize();
            SERVER.log("Loading Emote Data...");
            EmoteUtils.initialize();
            InitializeLogger.end();
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
            SERVER.warn("Block display workaround disabled: could not register the block display entity (" + e + ")");
        }
    }

    public void onSessionLogin(SessionLoginEvent ev) {

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
        for (ExtrasPlayer player : connections.values()) {
            GeyserSession session = player.getSession();
            if (session.bedrockUsername().equals(connection.bedrockUsername())) {
                // this occurs before bedrock authenticates properly
                if (session.getAuthData() == null && session.getClientData() == null) {
                    // we clear the players packs here as its possible that the game crashes when trying to load a pack
                    player.getPreferences().getSelectedPacks().clear();
                    player.getPreferences().save();
                }
                if (GE.getConfig().isAutoReconnect()) {
                    player.reconnect();
                }
                connections.get(player.getBedrockXUID()).onDisconnect();
                connections.remove(player.getBedrockXUID());
                return;
            }
        }
        if (connections.containsKey(ev.connection().xuid())) {
            connections.get(ev.connection().xuid()).onDisconnect();
        }
        if (connections.remove(ev.connection().xuid()) == null) {
            SERVER.warn("Could not remove user.");
        }

    }

    public void onEmoteEvent(ClientEmoteEvent ev) {
        ExtrasPlayer player = connections.get(ev.connection().xuid());
        if (player != null) {
            player.onEmoteEvent(ev);
        }
    }

    public void onGeyserReload(GeyserPreReloadEvent ignored) {
        runSafely("auto-reconnect", this::autoReconnectAll);
    }

    public void onGeyserShutdown(GeyserShutdownEvent ignored) {
        runSafely("auto-reconnect", this::autoReconnectAll);
    }

    public void onLoadPacks(SessionLoadResourcePacksEvent ev) {
        connections.remove(ev.connection().xuid());
        connections.put(ev.connection().xuid(), SERVER.createPlayer(ev.connection()));
        SERVER.debug("Created player for xuid=" + ev.connection().xuid() + " javaUuid=" + ev.connection().javaUuid());
        PackCacheUtils.onPackLoadEvent(connections.get(ev.connection().xuid()), ev);
    }

    public JavaPreferencesData getJavaPreferencesData(UUID javaUUID) {
        return javaConnections.get(javaUUID);
    }

    public void autoReconnectAll() {
        // Can run from a shutdown after a failed start, when the config was never loaded
        if (getConfig() == null || !getConfig().isAutoReconnect()) {
            return;
        }
        for (ExtrasPlayer player : connections.values()) {
            runSafely("reconnecting " + player.getBedrockXUID(), () -> {
                player.getPreferences().save();
                player.reconnect();
            });
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
                SERVER.warn("GeyserExtras: " + step + " failed: " + e);
            } else {
                System.err.println("[GeyserExtras] " + step + " failed: " + e);
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
        if (javaConnections.containsKey(javaUUID)) {
            javaConnections.get(javaUUID).save();
            javaConnections.remove(javaUUID);
        }
    }
}