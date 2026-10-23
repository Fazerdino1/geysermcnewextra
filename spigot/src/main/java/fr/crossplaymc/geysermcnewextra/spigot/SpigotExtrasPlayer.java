package fr.crossplaymc.geysermcnewextra.spigot;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.Set;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;
import static fr.crossplaymc.geysermcnewextra.spigot.GeyserMCNewExtraSpigot.SPIGOT;

public class SpigotExtrasPlayer extends ExtrasPlayer {
    public Player player;

    public SpigotExtrasPlayer(GeyserConnection connection) {
        super(connection);
    }

    @Override
    public void startGame() {
        super.startGame();
        player = Bukkit.getPlayer(getJavaUUID());

    }

    @Override
    public void tick() {
        super.tick();
        if (SPIGOT.getPlatformConfig().isEnableBlockGhostingWorkaround() && ticks % 2 == 1) {
            float interactionRange = (float) session.getPlayerEntity().getBlockInteractionRange();
            player.getLineOfSight(Set.of(Material.AIR), Math.round(interactionRange)).forEach((targetBlock) -> {
                if (targetBlock != null) {
                    player.sendBlockChange(targetBlock.getLocation(), targetBlock.getBlockData());
                }
            });

        }
    }

    @Override
    public void hungerSprintCancel() {
    }
}
