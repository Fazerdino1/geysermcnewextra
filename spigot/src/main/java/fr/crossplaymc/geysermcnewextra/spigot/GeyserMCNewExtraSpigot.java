package fr.crossplaymc.geysermcnewextra.spigot;


import fr.crossplaymc.geysermcnewextra.Server;
import fr.crossplaymc.geysermcnewextra.ServerType;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra;
import fr.crossplaymc.geysermcnewextra.core.parity.bedrock.EmoteUtils;
import fr.crossplaymc.geysermcnewextra.core.preferences.JavaPreferencesData;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.spigot.config.GeyserMCNewExtraSpigotConfig;
import fr.crossplaymc.geysermcnewextra.spigot.config.SpigotConfigLoader;
import fr.crossplaymc.geysermcnewextra.spigot.modules.armorstands.ArmorStandModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.nio.file.Path;
import java.util.UUID;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class GeyserMCNewExtraSpigot extends JavaPlugin implements Server {
    public static GeyserMCNewExtra CORE;

    public static GeyserMCNewExtraSpigot SPIGOT;

    private static SpigotTickUtil spigotTickUtil;


    @Getter
    @Setter
    private GeyserMCNewExtraSpigotConfig platformConfig;

    public GeyserMCNewExtraSpigot() {
        ServerType.type = ServerType.SPIGOT;
        SPIGOT = this;
        spigotTickUtil = new SpigotTickUtil();


    }

    @Override
    public void onLoad() {
        // use the function instead of IsAvailable.PACKETEVENTS because packetevents is loaded before GeyserMCNewExtra is loaded
        // where the IsAvailable class is initialised
        if (IsAvailable.packetevents()) {
            fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler.load(
                    io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build(this)
            );
        }
    }

    @Override
    public void onEnable() {
        SpigotCommandHandler handler = new SpigotCommandHandler();

        PluginCommand geCommand = this.getCommand("geysermcnewextra");
        if (geCommand != null){
            geCommand.setExecutor(handler);
        }

        PluginCommand platformlistCommand = this.getCommand("platformlist");
        if (platformlistCommand != null){
            platformlistCommand.setExecutor(handler);
        }

        PluginCommand playerlistCommand = this.getCommand("playerlist");
        if (playerlistCommand != null){
            playerlistCommand.setExecutor(handler);
        }

        PluginCommand emoteChatCommand = this.getCommand("emotechat");
        if (emoteChatCommand != null){
            emoteChatCommand.setExecutor(handler);
        }

        this.getServer().getPluginManager().registerEvents(new SpigotListener(), this);
        CORE = new GeyserMCNewExtra(this);
        SpigotCommandAliases.disable(this, CORE.getConfig().getDisabledCommandAliases());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            CORE.serverTick();
        }, 0L, 0L);

        if (IsAvailable.packetevents()) {
            fr.crossplaymc.geysermcnewextra.core.protocol.ProtocolHandler.init();
        }

        // The platform config is missing when the core could not start
        if (platformConfig != null) {
            startModules();
        }
    }

    // Each module starts on its own: one failing does not stop the others
    private void startModules() {
        // Listeners read the config on every event, so a module can be turned off by changing it
        GeyserMCNewExtra.runSafely("armor-stands module", () ->
                getServer().getPluginManager().registerEvents(new ArmorStandModule(this, () -> platformConfig.getArmorStands()), this));
    }

    @Override
    public void onConfigLoad() {
        SpigotConfigLoader.load();
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
    public TickUtil getTickUtil() {
        return spigotTickUtil;
    }

    @Override
    public ExtrasPlayer createPlayer(GeyserConnection connection) {
        return new SpigotExtrasPlayer(connection);
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
        SpigotExtrasPlayer spigotPlayer = (SpigotExtrasPlayer) player;
        spigotTickUtil.runSync(() -> {
            for (Player playerNear : spigotPlayer.player.getWorld().getEntitiesByClass(Player.class)) {
                boolean isBedrockPlayer = GE.geyserApi.isBedrockPlayer(playerNear.getUniqueId());
                if (isBedrockPlayer) continue;

                JavaPreferencesData userPrefs = GE.getJavaPreferencesData(playerNear.getUniqueId());
                if (userPrefs != null && userPrefs.muteEmoteChat) continue;

                if (EmoteUtils.EMOTE_DISTANCE >= spigotPlayer.player.getLocation().distance(playerNear.getLocation())) {
                    playerNear.sendMessage(ChatColor.translateAlternateColorCodes('§', emoteChat));
                }
            }
        }, player);
    }

    @Override
    public void sendRawMessage(UUID javaPlayer, String message) {
        Player player = Bukkit.getPlayer(javaPlayer);
        if (player == null) {
            return;
        }
        player.sendRawMessage(message);
    }

    @Override
    public void sendMessage(UUID javaPlayer, String message) {
        Player player = Bukkit.getPlayer(javaPlayer);
        if (player == null) {
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('§', message));
    }
}
