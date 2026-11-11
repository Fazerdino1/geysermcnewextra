package fr.crossplaymc.geysermcnewextra.spigot;


import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class SpigotListener implements Listener {
    public static final List<String> cropTypes = Arrays.asList(
            "WHEAT",
            "WHEAT_SEEDS",
            "SEEDS",
            "CROPS",
            "CARROT",
            "CARROTS",
            "CARROT_ITEM",
            "POTATO",
            "POTATOES",
            "POTATO_ITEM",
            "BEETROOT",
            "BEETROOT_SEEDS",
            "BEETROOT_BLOCK",
            "BEETROOTS",
            "MELON_SEEDS",
            "MELON_STEM",
            "PUMPKIN_SEEDS",
            "PUMPKIN_STEM"
    );



    public static void stopSoundForBlock(String blockName, Player player) {
        player.stopSound("minecraft:dig." + blockName);
        player.stopSound("minecraft:use." + blockName);
        player.stopSound("minecraft:step." + blockName);
        player.stopSound("minecraft:land." + blockName);
        player.stopSound("minecraft:jump." + blockName);
        player.stopSound("minecraft:hit." + blockName);
        player.stopSound("minecraft:fall." + blockName);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (GeyserMCNewExtraSpigot.CORE != null) {
            GeyserMCNewExtraSpigot.CORE.onJavaPlayerJoin(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    // A kick cancelled by another plugin is not a departure
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    // ignoreCancelled: a placement refused by a protection plugin must stay refused, and this
    // handler only ever cancels, it never un-cancels
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBlockPlace(BlockPlaceEvent ev) {
        Player player = ev.getPlayer();

        // Java Only Block Placement (ported from v1.0)
        ExtrasPlayer user = ExtrasPlayer.get(player.getUniqueId());
        if (user instanceof SpigotExtrasPlayer extrasPlayer) {
            if (GeyserMCNewExtraSpigot.SPIGOT.getPlatformConfig().isEnableJavaOnlyBlockPlacement()) {
                Block block = ev.getBlockPlaced();
                double interactionRange = extrasPlayer.getSession().getPlayerEntity().getBlockInteractionRange();

                boolean refuse;
                if (block.getType().equals(Material.AIR) || ev.getBlockAgainst().getType().equals(Material.AIR)) {
                    refuse = true;
                } else {
                    RayTraceResult blockCast = player.rayTraceBlocks(interactionRange, FluidCollisionMode.ALWAYS);
                    refuse = blockCast != null && !block.equals(blockCast.getHitBlock());
                }
                if (refuse) {
                    // Cancelling is enough for the server to restore the block; only resync the client
                    ev.setCancelled(true);
                    Vector velocity = player.getVelocity().clone();
                    player.teleport(player.getLocation());
                    SERVER.getTickUtil().runIn(2L, () -> {
                        player.setVelocity(velocity);
                    }, extrasPlayer);
                    player.updateInventory();
                }
            }
        }
    }

    private void cropStop(Player player) {
        stopSoundForBlock("grass", player);
        stopSoundForBlock("gravel", player);
        stopSoundForBlock("dirt", player);
        player.stopSound("minecraft:block.crop.break");
        player.stopSound("minecraft:item.crop.plant");
    }

    public void onPlayerLeave(Player player) {
        if (GeyserMCNewExtraSpigot.CORE != null) {
            GeyserMCNewExtraSpigot.CORE.onJavaPlayerLeave(player.getUniqueId());
        }
    }
}
