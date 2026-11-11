package fr.crossplaymc.geysermcnewextra.core.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.player.User;
import fr.crossplaymc.geysermcnewextra.core.protocol.handlers.workarounds.MannequinCapeWorkaround;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

// This contains work arounds for Java only clients
public class ProtocolHandler implements PacketListener {
    static ProtocolHandler PE;

    // Updated from several Netty threads
    static Map<UUID, JavaPlayer> connections = new ConcurrentHashMap<>();

    private static boolean initialized = false;

    public static void load(PacketEventsAPI<?> builder) {
        PacketEvents.setAPI(builder);
        PacketEvents.getAPI().load();
        ProtocolHandler.PE = new ProtocolHandler();
        PacketEvents.getAPI().getEventManager().registerListener(ProtocolHandler.PE, PacketListenerPriority.HIGHEST);
    }

    public static void init() {
        PacketEvents.getAPI().init();
        initialized = true;
        CapeLoader.init();
    }

    public static void terminate() {
        if (!initialized || PacketEvents.getAPI() == null) {
            return;
        }
        initialized = false;
        PacketEvents.getAPI().terminate();
    }

    @Nullable
    public static JavaPlayer getJavaPlayer(UUID uuid) {
        return connections.get(uuid);
    }

    public static boolean javaPlayerExists(UUID uuid) {
        return connections.containsKey(uuid);
    }

    public static void add(User user) {
        UUID uuid = user.getUUID();
        if (uuid == null || isBedrockPlayer(uuid)) {
            // Geyser's own connection for a Bedrock player also goes through PacketEvents: the Java-only
            // workarounds (mannequins...) must not be applied to it, Geyser would show them as real entities
            return;
        }
        connections.computeIfAbsent(uuid, id -> new JavaPlayer(user));
    }

    private static boolean isBedrockPlayer(UUID uuid) {
        try {
            return GE != null && GE.geyserApi != null && GE.geyserApi.isBedrockPlayer(uuid);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static void remove(User user) {
        if (user.getUUID() != null) {
            connections.remove(user.getUUID());
        }
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        add(event.getUser());
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        remove(event.getUser());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        UUID uuid = event.getUser().getUUID();
        JavaPlayer player = uuid == null ? null : getJavaPlayer(uuid);

        // Only handle this when we are sending packets to java players
        // Bedrock players get their packets in core/injectors/
        if (player == null) {
            return;
        }

        player.onPacketSend(event);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        UUID uuid = event.getUser().getUUID();
        JavaPlayer player = uuid == null ? null : getJavaPlayer(uuid);

        // Only handle this when we are sending packets to java players
        // Bedrock players get their packets in core/injectors/
        if (player == null) {
            return;
        }

        player.onPacketReceive(event);
    }
}
