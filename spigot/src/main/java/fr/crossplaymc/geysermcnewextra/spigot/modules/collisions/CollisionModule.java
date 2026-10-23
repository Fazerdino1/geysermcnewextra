package fr.crossplaymc.geysermcnewextra.spigot.modules.collisions;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.spigot.config.GeyserMCNewExtraSpigotConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

/**
 * Removes the server-side collision of bamboo and pointed dripstone, which Bedrock clients predict differently
 * and get pulled back on. Adapted from Hurricane by GeyserMC (MIT, see CREDITS.md).
 * <p>
 * The shapes are static fields of the server's block classes: the change is server-wide, applied once at startup
 * and undone when the plugin is disabled.
 */
public final class CollisionModule implements Listener {
    private static final Material POINTED_DRIPSTONE = Material.matchMaterial("POINTED_DRIPSTONE");

    // Bamboo's collision box, used to stop players placing bamboo inside themselves now that it has none
    private static final BoundingBox BAMBOO_BOX = box(6.5, 0, 6.5, 9.5, 16, 9.5);

    private final List<ShapePatcher.Patch> patches = new ArrayList<>();
    private boolean bamboo;
    private boolean pointedDripstone;

    /**
     * @return whether at least one block lost its collision (the listener is then needed)
     */
    public boolean start(GeyserMCNewExtraSpigotConfig.Collisions settings) {
        if (!settings.isEnabled()) {
            return false;
        }
        if (Bukkit.getPluginManager().getPlugin("Hurricane") != null) {
            SERVER.warn(Lang.console("gmne.console.collisions_hurricane_installed"));
            return false;
        }
        ShapePatcher patcher;
        try {
            patcher = new ShapePatcher();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            SERVER.warn(Lang.console("gmne.console.collisions_unsupported", e));
            return false;
        }
        if (settings.isBamboo()) {
            bamboo = patch(patcher, "bamboo", () -> patches.addAll(patcher.removeBambooCollision()));
        }
        if (settings.isPointedDripstone() && POINTED_DRIPSTONE != null) {
            pointedDripstone = patch(patcher, "pointed_dripstone", () -> patches.addAll(patcher.removeDripstoneCollision()));
        }
        return bamboo || pointedDripstone;
    }

    private static boolean patch(ShapePatcher patcher, String block, ShapePatcher.Action action) {
        try {
            action.run();
            SERVER.log(Lang.console("gmne.console.collisions_removed", block));
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            SERVER.warn(Lang.console("gmne.console.collisions_failed", block, e));
            return false;
        }
    }

    public void stop() {
        for (ShapePatcher.Patch patch : patches) {
            try {
                patch.undo();
            } catch (RuntimeException | LinkageError e) {
                SERVER.warn(Lang.console("gmne.console.collisions_restore_failed", e));
            }
        }
        patches.clear();
    }

    /**
     * Blocks without collision can be placed inside players: refuse it, as far as we can tell.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        BoundingBox box;
        if (bamboo && placed.getType() == Material.BAMBOO) {
            box = BAMBOO_BOX;
        } else if (pointedDripstone && placed.getType() == POINTED_DRIPSTONE) {
            box = dripstoneBox(placed.getBlockData().getAsString());
        } else {
            return;
        }
        if (event.getPlayer().getBoundingBox().overlaps(box.clone().shift(placed.getLocation()))) {
            event.setBuild(false);
        }
    }

    /**
     * The original shape of a pointed dripstone, from its block state (e.g. {@code thickness=tip,vertical_direction=down}).
     */
    static BoundingBox dripstoneBox(String blockState) {
        String state = blockState.toLowerCase();
        if (state.contains("thickness=tip_merge")) {
            return box(5, 0, 5, 11, 16, 11);
        }
        if (state.contains("thickness=tip")) {
            return state.contains("vertical_direction=down") ? box(5, 5, 5, 11, 16, 11) : box(5, 0, 5, 11, 11, 11);
        }
        if (state.contains("thickness=frustum")) {
            return box(4, 0, 4, 12, 16, 12);
        }
        if (state.contains("thickness=middle")) {
            return box(3, 0, 3, 13, 16, 13);
        }
        return box(2, 0, 2, 14, 16, 14);
    }

    // Same units as the server's Block#box: sixteenths of a block
    private static BoundingBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BoundingBox(minX / 16, minY / 16, minZ / 16, maxX / 16, maxY / 16, maxZ / 16);
    }
}
