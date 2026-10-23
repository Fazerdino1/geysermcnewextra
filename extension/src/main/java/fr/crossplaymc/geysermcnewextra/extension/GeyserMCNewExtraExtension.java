package fr.crossplaymc.geysermcnewextra.extension;

import fr.crossplaymc.geysermcnewextra.Server;
import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import org.cloudburstmc.protocol.bedrock.packet.ShowStoreOfferPacket;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.text.ChatColor;

import java.nio.file.Path;
import java.util.UUID;

public class GeyserMCNewExtraExtension implements Extension, Server {
    public static GeyserMCNewExtra CORE;
    public static GeyserMCNewExtraExtension EXTENSION;
    private ExtensionTickUtil tickUtil;

    public GeyserMCNewExtraExtension() {
        ServerType.type = IsAvailable.classExists("org.geysermc.geyser.platform.standalone.GeyserStandaloneBootstrap") ? ServerType.STANDALONE : ServerType.EXTENSION;
        EXTENSION = this;
    }

    @Subscribe
    public void onEnable(GeyserPostInitializeEvent ev) {
        CORE = new GeyserMCNewExtra(this);
        tickUtil = new ExtensionTickUtil();
    }


    @Override
    public TickUtil getTickUtil() {
        return tickUtil;
    }

    @Override
    public ExtrasPlayer createPlayer(GeyserConnection connection) {
        return new ExtensionExtrasPlayer(connection);
    }

    @Override
    public void log(String string) {
        this.logger().info(string);
    }

    @Override
    public void warn(String string) {
        this.logger().warning(string);
    }

    @Override
    public Path getPluginFolder() {
        return this.dataFolder();
    }

    @Override
    public void sendEmoteChat(ExtrasPlayer player, String emoteChat) {
    }

    @Override
    public void sendRawMessage(UUID javaPlayer, String message) {

    }


    @Override
    public void sendMessage(UUID javaPlayer, String message) {}
}
