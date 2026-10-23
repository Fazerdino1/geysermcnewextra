package fr.crossplaymc.geysermcnewextra.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.*;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import fr.crossplaymc.geysermcnewextra.core.utils.IdUtils;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import fr.crossplaymc.geysermcnewextra.core.version.PluginVersion;
import fr.crossplaymc.geysermcnewextra.Server;
import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.parity.bedrock.EmoteUtils;
import fr.crossplaymc.geysermcnewextra.core.preferences.JavaPreferencesData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.level.JavaDimension;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

@Plugin(
        authors = {"LetsGoAway", "CrossPlayMC"},
        url = "https://github.com/Fazerdino1/geysermcnewextra",
        description = "Java Edition features for Bedrock players on CrossPlayMC (based on the MIT project by LetsGoAway)",
        name = "GeyserMCNewExtra",
        version = PluginVersion.GE_VERSION,
        id = "geysermcnewextra",
        dependencies = {
                @Dependency(id = "geyser"),
                @Dependency(id = "packetevents", optional = true),
        }
)

public class GeyserMCNewExtraVelocity implements Server {
    public static GeyserMCNewExtra CORE;
    public static GeyserMCNewExtraVelocity VELOCITY;
    public static ProxyServer server;
    private final Logger logger;
    private final PluginContainer container;
    private final VelocityTickUtil velocityTickUtil;

    private final Path dataDirectory;

    @Inject
    public GeyserMCNewExtraVelocity(ProxyServer server, Logger logger,
                            @DataDirectory Path dataDirectory,
                            PluginContainer container) {
        ServerType.type = ServerType.VELOCITY;
        VELOCITY = this;
        GeyserMCNewExtraVelocity.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.container = container;
        velocityTickUtil = new VelocityTickUtil();
        // use the function instead of IsAvailable.PACKETEVENTS because packetevents is loaded before GeyserMCNewExtra is loaded
        // where the IsAvailable class is initialised
        if (IsAvailable.packetevents()) {
            fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler.load(
                    io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder.build(server, container, logger, this.dataDirectory)
            );
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CORE = new GeyserMCNewExtra(this);
        if (IsAvailable.packetevents()) {
            fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler.init();
        }
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent ev) {
        if (CORE != null) {
            CORE.autoReconnectAll();
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent ev) {
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
        CommandManager commandManager = server.getCommandManager();
        for (String label : CommandExecutor.ids.keySet()) {
            commandManager.register(commandManager.metaBuilder(label).build(), new VelocityCommandHandler());
        }

        server.getEventManager().register(this, new VelocityListener());
    }

    @Override
    public TickUtil getTickUtil() {
        return velocityTickUtil;
    }

    @Override
    public ExtrasPlayer createPlayer(GeyserConnection connection) {
        return new VelocityExtrasPlayer(connection);
    }

    @Override
    public void log(String string) {
        this.logger.info(string);
    }

    @Override
    public void warn(String string) {
        this.logger.warn(string);
    }

    @Override
    public Path getPluginFolder() {
        return dataDirectory;
    }

    @Override
    public void sendEmoteChat(ExtrasPlayer player, String emoteChat) {
        VelocityExtrasPlayer velocityPlayer = (VelocityExtrasPlayer) player;
        Vector3f playerPos = velocityPlayer.getSession().getPlayerEntity().getPosition();
        JavaDimension dimension = velocityPlayer.getSession().getDimensionType();

        for (Player playerNear : getServerPlayers(velocityPlayer.player)) {
            UUID javaUUID = playerNear.getUniqueId();

            boolean isBedrock = IdUtils.isBedrockPlayer(javaUUID);
            if (isBedrock) continue;

            JavaPreferencesData userPrefs = GE.getJavaPreferencesData(javaUUID);
            if (userPrefs != null && userPrefs.muteEmoteChat) continue;

            PlayerEntity entity = velocityPlayer.getSession().getEntityCache().getPlayerEntity(javaUUID);
            if (entity == null) continue;

            JavaDimension playerNearDimension = player.getPlayerDimensionsMap().get(entity.getEntityId());
            if (playerNearDimension != dimension) continue;

            if (EmoteUtils.EMOTE_DISTANCE >= playerPos.distance(entity.getPosition())) {
                playerNear.sendMessage(LegacyComponentSerializer.legacySection().deserialize(emoteChat));
            }
        }
    }

    private Collection<Player> getServerPlayers(Player player) {
        ServerConnection server;
        Optional<ServerConnection> connectionOptional = player.getCurrentServer();
        if (connectionOptional.isPresent()) {
            server = connectionOptional.get();
            return server.getServer().getPlayersConnected();
        }
        return Collections.emptySet();
    }

    @Override
    public void sendRawMessage(UUID javaPlayer, String message) {
        Optional<Player> optPlayer = server.getPlayer(javaPlayer);
        optPlayer.ifPresent(player -> player.sendPlainMessage(message));
    }

    @Override
    public void sendMessage(UUID javaPlayer, String message) {
        Optional<Player> optPlayer = server.getPlayer(javaPlayer);
        optPlayer.ifPresent(player -> player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message)));
    }
}
