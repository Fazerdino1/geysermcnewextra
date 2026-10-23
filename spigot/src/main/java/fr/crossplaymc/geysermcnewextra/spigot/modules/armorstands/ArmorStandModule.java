package fr.crossplaymc.geysermcnewextra.spigot.modules.armorstands;

import fr.crossplaymc.geysermcnewextra.spigot.config.GeyserMCNewExtraSpigotConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

/**
 * Bedrock Edition armor stands for everyone: arms when placed, and sneak + use to cycle poses.
 * Written from scratch for GeyserMCNewExtra (the "Bedrock Armorstands" plugin has no license).
 */
public final class ArmorStandModule implements Listener {
    public static final String POSE_PERMISSION = "geysermcnewextra.armorstands.pose";

    private final Supplier<GeyserMCNewExtraSpigotConfig.ArmorStands> config;
    private final NamespacedKey poseKey;

    public ArmorStandModule(Plugin plugin, Supplier<GeyserMCNewExtraSpigotConfig.ArmorStands> config) {
        this.config = config;
        this.poseKey = new NamespacedKey(plugin, "armor_stand_pose");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand) || event.getPlayer() == null) {
            return;
        }
        GeyserMCNewExtraSpigotConfig.ArmorStands settings = config.get();
        if (settings.isArms() && settings.isActiveIn(stand.getWorld())) {
            stand.setArms(true);
        }
    }

    // HIGH and ignoreCancelled: protection plugins (claims, regions) cancel earlier and win
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !(event.getRightClicked() instanceof ArmorStand stand)) {
            return;
        }
        Player player = event.getPlayer();
        GeyserMCNewExtraSpigotConfig.ArmorStands settings = config.get();
        if (!player.isSneaking() || !settings.isPoseCycling() || !settings.isActiveIn(stand.getWorld())
                || !player.hasPermission(POSE_PERMISSION)) {
            return;
        }
        // Invisible or marker stands are usually holograms or decorations owned by other plugins
        if (!stand.isVisible() || stand.isMarker()) {
            return;
        }
        // Sneak + use changes the pose instead of swapping items, as on Bedrock Edition
        event.setCancelled(true);
        Integer current = stand.getPersistentDataContainer().get(poseKey, PersistentDataType.INTEGER);
        ArmorStandPose next = ArmorStandPose.after(current == null ? 0 : current);
        next.applyTo(stand);
        stand.getPersistentDataContainer().set(poseKey, PersistentDataType.INTEGER, next.ordinal());
    }
}
