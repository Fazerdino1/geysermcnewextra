package fr.crossplaymc.geysermcnewextra.bungee;

import fr.crossplaymc.geysermcnewextra.Server;
import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.utils.IdUtils;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.parity.bedrock.EmoteUtils;
import fr.crossplaymc.geysermcnewextra.core.preferences.JavaPreferencesData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.level.JavaDimension;

import java.nio.file.Path;
import java.util.UUID;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class GeyserMCNewExtraBungee extends Plugin implements Server {
    public static GeyserMCNewExtra CORE;
    public static Plugin BUNGEE;
    private static BungeeTickUtil bungeeTickUtil;

    public GeyserMCNewExtraBungee() {
        ServerType.type = ServerType.BUNGEECORD;
        BUNGEE = this;
        bungeeTickUtil = new BungeeTickUtil();
    }

    @Override
    public void onLoad() {
        // use the function instead of IsAvailable.PACKETEVENTS because packetevents is loaded before GeyserMCNewExtra is loaded
        // where the IsAvailable class is initialised
        if (IsAvailable.packetevents()) {
            fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler.load(
                   io.github.retrooper.packetevents.bungee.factory.BungeePacketEventsBuilder.build(this)
            );
        }
    }

    @Override
    public void onEnable() {
        CORE = new GeyserMCNewExtra(this);

        if (IsAvailable.packetevents()) {
            fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler.init();
        }
    }

    @Override
    public void onDisable() {
        // CORE is null when the start failed; disabling must still never throw (upstream #47)
        GeyserMCNewExtra.runSafely("auto-reconnect", () -> {
            if (CORE != null) {
                CORE.autoReconnectAll();
            }
        });
        if (IsAvailable.packetevents()) {
            GeyserMCNewExtra.runSafely("PacketEvents shutdown", fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler::terminate);
        }
    }

    @Override
    public void onGeyserMCNewExtraInitialize() {
        for (String label : CommandExecutor.ids.keySet()) {
            ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeeCommandHandler(label));
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeListener());
    }

    @Override
    public TickUtil getTickUtil() {
        return bungeeTickUtil;
    }

    @Override
    public ExtrasPlayer createPlayer(GeyserConnection connection) {
        return new BungeeExtrasPlayer(connection);
    }

    @Override
    public void log(String string) {
        this.getLogger().info(string);
    }

    @Override
    public void warn(String string) {
        this.getLogger().warning(string);
    }

    @Override
    public Path getPluginFolder() {
        return getDataFolder().toPath();
    }

    @Override
    public void sendEmoteChat(ExtrasPlayer player, String emoteChat) {
        BungeeExtrasPlayer bungeePlayer = (BungeeExtrasPlayer) player;
        Vector3f playerPos = bungeePlayer.getSession().getPlayerEntity().getPosition();

        TextComponent component = new TextComponent();
        component.setText(ChatColor.translateAlternateColorCodes('§', emoteChat));

        JavaDimension dimension = bungeePlayer.getSession().getDimensionType();
        for (ProxiedPlayer playerNear : bungeePlayer.player.getServer().getInfo().getPlayers()) {
            UUID javaUUID = playerNear.getUniqueId();

            boolean isBedrock = IdUtils.isBedrockPlayer(javaUUID);
            if (isBedrock) continue;

            JavaPreferencesData userPrefs = GE.getJavaPreferencesData(javaUUID);
            if (userPrefs != null && userPrefs.muteEmoteChat) continue;

            PlayerEntity entity = bungeePlayer.getSession().getEntityCache().getPlayerEntity(javaUUID);
            if (entity == null) continue;

            JavaDimension playerNearDimension = player.getPlayerDimensionsMap().get(entity.getEntityId());
            if (playerNearDimension != dimension) continue;

            if (EmoteUtils.EMOTE_DISTANCE >= playerPos.distance(entity.getPosition())) {
                playerNear.sendMessage(ChatMessageType.SYSTEM, component);
            }
        }
    }

    @Override
    public void sendRawMessage(UUID javaPlayer, String message) {
        ProxyServer.getInstance().getPlayer(javaPlayer).sendMessage(message);
    }
    @Override
    public void sendMessage(UUID javaPlayer, String message) {
        TextComponent component = new TextComponent();
        component.setText(ChatColor.translateAlternateColorCodes('§', message));
        ProxyServer.getInstance().getPlayer(javaPlayer).sendMessage(ChatMessageType.SYSTEM, component);
    }
}
