package fr.crossplaymc.geysermcnewextra.core;

import fr.crossplaymc.geysermcnewextra.core.features.skinsaver.SkinSaver;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockMenu;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockModal;
import fr.crossplaymc.geysermcnewextra.core.features.recipemenus.RecipeMenus;
import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.core.parity.bedrock.EmoteUtils;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.serverlinks.ServerLinksData;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.tablist.TabListData;
import fr.crossplaymc.geysermcnewextra.core.preferences.PreferencesData;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.EmoteBindings;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Remappable;
import fr.crossplaymc.geysermcnewextra.core.utils.IdUtils;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;
import org.cloudburstmc.protocol.bedrock.packet.ToastRequestPacket;
import org.geysermc.geyser.api.bedrock.camera.GuiElement;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.geyser.level.JavaDimension;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.BossBar;
import org.geysermc.geyser.text.MinecraftLocale;
import org.geysermc.geyser.util.DimensionUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

//
public class ExtrasPlayer {
    @Getter
    private final TabListData tabListData;
    @Getter
    private final ServerLinksData serverLinksData;
    @Getter
    private final RecipeMenus recipeMenus;
    @Getter
    private final PreferencesData preferences;
    @Getter
    public GeyserSession session;
    @Setter
    @Getter
    public float tickrate = 20.0f;
    public long ticks = 0;
    @Getter
    private UUID javaUUID;
    @Getter
    private String bedrockXUID;
    // Emote bindings waiting for the player to perform the emote to link (filled from the bindings menu)
    private final Queue<Remappable> emoteLinkQueue = new ConcurrentLinkedQueue<>();
    @Getter
    @Setter
    private ScheduledFuture<?> doubleClickShortcutFuture;
    @Getter
    private File userPrefs;
    @Getter
    @Setter
    private ServerboundDiagnosticsPacket diagnostics = null;
    @Setter
    @Getter
    private boolean packsUpdated = false;
    @Getter
    private boolean loggedIn = false;
    private Instant lastEmoteTime = Instant.now();

    // Used for the double click menu action
    @Setter
    @Getter
    private long lastInventoryClickTime = 0;

    // For emote chat on platforms where we cant get the dimensions of other players
    @Getter
    private Map<Integer, JavaDimension> playerDimensionsMap;

    @Getter
    @Setter
    private BossBar fpsBossBar;

    @Setter
    @Getter
    private boolean emoting = false;

    @Getter
    private double averagePing = 0.0f;
    @Getter
    private long pingSample = 0;
    @Getter
    private long pingSampleSize = 0;
    @Getter
    private int lastPing = -1;

    public ExtrasPlayer(GeyserConnection connection) {
        this.session = (GeyserSession) connection;
        this.javaUUID = connection.javaUuid();
        this.bedrockXUID = connection.xuid();
        tabListData = new TabListData(this);
        serverLinksData = new ServerLinksData(this);
        recipeMenus = new RecipeMenus(this);
        preferences = new PreferencesData(this);
        userPrefs = PreferencesData.PREFERENCES_PATH.resolve(bedrockXUID + ".json").toFile();
        preferences.load();
        playerDimensionsMap = new HashMap<>();
    }

    public void startGame() {
        loggedIn = true;
        SERVER.debug("startGame xuid=" + bedrockXUID + " javaUuid=" + session.javaUuid());

        sendSystemToast(lang("gmne.welcome_toast.line1"), lang("gmne.welcome_toast.line2"));

        if (GE.getConfig().isEnableSkinSaving()) {
            SkinSaver.save(this);
        }

        // Java UUID is null until login
        javaUUID = session.javaUuid();

        recipeMenus.attach(session);
    }

    /**
     * @return the name of the world the player is in, or null on platforms that do not know it (proxies, standalone)
     */
    public String getWorldName() {
        return null;
    }

    /**
     * @return whether the anvil or smithing table being opened is a block of the world, as opposed to a menu a
     * plugin opens to ask for text; true on platforms that cannot tell
     */
    public boolean isOpeningBlockMenu() {
        return true;
    }

    private UUID bossbarID;

    private void createFpsBossBar() {
        long entityId = session.getEntityCache().getNextEntityId().incrementAndGet();
        fpsBossBar = new BossBar(session, entityId, Component.text(getBossBarText()), 1.0f, 0, 1, 0);
        bossbarID = UUID.randomUUID();
        session.getEntityCache().addBossBar(bossbarID, fpsBossBar);
    }

    public String getBossBarText() {
        return diagnostics != null ? lang("gmne.fps", Math.round(diagnostics.getAvgFps())) : "";
    }

    public void onDisconnect() {
        disconnected = true;
        if (doubleClickShortcutFuture != null) {
            doubleClickShortcutFuture.cancel(false);
            doubleClickShortcutFuture = null;
        }
        tabListData.clear();
        if (fpsBossBar != null) {
            session.getEntityCache().removeBossBar(bossbarID);
            fpsBossBar = null;
        }
    }

    public void reconnect() {
        session.transfer(session.joinAddress(), session.joinPort());
    }


    public void hungerSprintCancel() {
        // todo: figure out how to recreate this option with geyser codebase
    }

    public void onEmoteEvent(ClientEmoteEvent ev) {
        SERVER.debug("Emote " + ev.emoteId() + " from xuid=" + bedrockXUID);
        UUID uuid = UUID.fromString(ev.emoteId());

        Remappable toLink = emoteLinkQueue.poll();
        SERVER.debug("Emote link pending=" + toLink + " links=" + preferences.emoteLinks + " actions=" + preferences.remappableActionMap);
        if (toLink != null) {
            EmoteBindings.link(preferences.emoteLinks, toLink, uuid);
            preferences.save();
            String emoteName = EmoteUtils.getName(uuid);
            sendMessage(lang("gmne.emote_link.done", toLink.translate(this), emoteName != null ? emoteName : uuid.toString()));
            return;
        }

        // Geyser 2.11+ no longer sends the emote wheel slots, so bindings follow the emote the player linked
        Remappable binding = EmoteBindings.find(preferences.emoteLinks, uuid);

        if (binding == null || preferences.isDefault(binding)) {
            String emoteChat = EmoteUtils.getEmoteChatString(uuid, this);

            if (emoteChat != null && Duration.between(lastEmoteTime, Instant.now()).toMillis() >= 3000) {
                SERVER.sendEmoteChat(this, emoteChat);
                lastEmoteTime = Instant.now();
            }
        } else {
            preferences.getAction(binding).run(this);
        }

    }

    /**
     * The next emote this player performs is linked to {@code binding} instead of being played as usual.
     */
    public void queueEmoteLink(Remappable binding) {
        emoteLinkQueue.add(binding);
        SERVER.debug("Emote link queued for " + binding + " xuid=" + bedrockXUID);
        sendMessage(lang("gmne.emote_link.prompt", binding.translate(this)));
    }

    private volatile boolean disconnected = false;
    private int tickFailures = 0;

    /**
     * Ticks this player, never letting an exception escape to the scheduler (an exception would
     * otherwise stop the Spigot tick loop for every other player, or kill the per-player task).
     *
     * @return false once the player disconnected, so per-player schedulers can stop
     */
    public final boolean safeTick() {
        if (disconnected) {
            return false;
        }
        try {
            tick();
        } catch (RuntimeException e) {
            // Log the first failure only: the same error would otherwise repeat every tick (upstream #67)
            if (tickFailures++ == 0) {
                SERVER.warn(Lang.console("gmne.console.tick_failed", bedrockXUID, e));
                if (GE.getConfig().isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void tick() {
        ticks++;
        if (GE.getConfig().isDisablePaperDoll() && !session.camera().isHudElementHidden(GuiElement.PAPER_DOLL)) {
            session.camera().hideElement(GuiElement.PAPER_DOLL);
        }


        // The dimension is unknown until the Java server sends it (e.g. on the code of conduct screen, upstream #67)
        if (session.getDimensionType() != null && session.getDimensionType().isNetherLike()
                && session.camera().fogEffects().contains(DimensionUtils.BEDROCK_FOG_HELL)) {
            session.camera().removeFog(DimensionUtils.BEDROCK_FOG_HELL);
        }


        if (fpsBossBar == null && preferences.isShowFPS() && diagnostics != null) {
            createFpsBossBar();
        }

        calculateAveragePing();
    }

    private void calculateAveragePing() {
        int ping = session.ping();
        if (ping != lastPing) {
            pingSample += ping;
            pingSampleSize++;
            lastPing = ping;
        }
        averagePing = (double) pingSample / pingSampleSize;
    }

    public void sendToast(String title, String description) {
        ToastRequestPacket toastPacket = new ToastRequestPacket();
        toastPacket.setTitle(title);
        toastPacket.setContent(description);
        session.sendUpstreamPacket(toastPacket);
    }

    public void sendSystemToast(String title, String description) {
        SERVER.debug("Toast '" + title + "' prefToasts=" + preferences.isSendSystemToasts() + " menuEnabled=" + GE.getConfig().isEnableMenu());
        if (preferences.isSendSystemToasts() && GE.getConfig().isEnableMenu()) {
            ToastRequestPacket toastPacket = new ToastRequestPacket();
            toastPacket.setTitle((CharSequence) title);
            toastPacket.setContent((CharSequence) description);
            session.sendUpstreamPacket(toastPacket);
        }
    }

    public void sendMessage(String text) {
        session.sendMessage(text);
    }

    public void resetTitle() {
        SetTitlePacket titlePacket = new SetTitlePacket();
        titlePacket.setType(SetTitlePacket.Type.CLEAR);
        titlePacket.setText("");
        titlePacket.setXuid("");
        titlePacket.setPlatformOnlineId("");
        session.sendUpstreamPacket(titlePacket);
    }

    public void sendForm(BedrockForm form) {
        debugSendForm(form);
        if (IsAvailable.floodgate()) {
            org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(javaUUID).sendForm(form.create(this).build());
        } else {
            session.sendForm(form.create(this).build());
        }
    }

    public void sendForm(BedrockMenu form) {
        debugSendForm(form);
        if (IsAvailable.floodgate()) {
            org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(javaUUID).sendForm(form.create(this));
        } else {
            session.sendForm(form.create(this));
        }
    }

    public void sendForm(BedrockModal form) {
        debugSendForm(form);
        if (IsAvailable.floodgate()) {
            org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(javaUUID).sendForm(form.create(this));
        } else {
            session.sendForm(form.create(this));
        }
    }

    private void debugSendForm(Object form) {
        if (!GE.getConfig().isDebugMode()) {
            return;
        }
        String route = IsAvailable.floodgate() ? "floodgate" : "geyser";
        String floodgatePlayer = "";
        if (IsAvailable.floodgate()) {
            floodgatePlayer = " floodgatePlayer=" + (javaUUID != null && org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(javaUUID) != null);
        }
        SERVER.debug("sendForm " + form.getClass().getSimpleName() + " via " + route + " javaUuid=" + javaUUID + floodgatePlayer);
    }

    public void setTickingState(float tickrate) {
        this.tickrate = tickrate;
    }

    public void swingArm() {
        AnimatePacket animatePacket = new AnimatePacket();
        animatePacket.setRuntimeEntityId(session.getPlayerEntity().geyserId());
        animatePacket.setAction(AnimatePacket.Action.SWING_ARM);
        session.sendUpstreamPacket(animatePacket);
    }

    public String translate(String lang) {
        return MinecraftLocale.getLocaleString(lang, session.locale());
    }

    /**
     * A text in this player's language (their Bedrock client locale), see {@link Lang}.
     */
    public String lang(String key, Object... args) {
        return Lang.get(key, session.locale(), args);
    }

    public String langOrElse(String key, String otherwise) {
        return Lang.has(key) ? lang(key) : otherwise;
    }

    @Nullable
    public static ExtrasPlayer get(UUID javaUUID) {
        long XUID = IdUtils.getBedrockXUID(javaUUID);
        if (XUID != -1) {
            return get(XUID);
        }
        for (ExtrasPlayer player : GE.connections.values()) {
            if (player.getJavaUUID() != null && player.getJavaUUID().equals(javaUUID)) {
                return player;
            }
        }
        return null;
    }

    public static ExtrasPlayer get(GeyserConnection connection) {
        return get(connection.xuid());
    }

    public static ExtrasPlayer get(long XUID) {
        return get(String.valueOf(XUID));
    }

    public static ExtrasPlayer get(String XUID) {
        return GE.connections.get(XUID);
    }

    public static boolean exists(GeyserConnection connection) {
        return exists(connection.xuid());
    }

    public static boolean exists(long XUID) {
        return exists(String.valueOf(XUID));
    }

    public static boolean exists(String XUID) {
        return GE.connections.containsKey(XUID);
    }

}
