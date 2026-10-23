package fr.crossplaymc.geysermcnewextra.spigot.modules.maps;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.spigot.config.GeyserMCNewExtraSpigotConfig;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

/**
 * Coordinates on maps, locator maps, starting map, Legacy Console zoom and reduced debug info.
 * Adapted from MapXYZ by LetsGoAway (MIT, see CREDITS.md).
 */
public final class MapModule implements Listener {
    private final Plugin plugin;
    private final Supplier<GeyserMCNewExtraSpigotConfig.Maps> config;
    private final MapCoordinatesRenderer renderer;
    // Worlds whose reducedDebugInfo game rule we turned on, so it can be given back later
    private final Path reducedDebugInfoFile;
    private final Set<String> reducedDebugInfoWorlds = new LinkedHashSet<>();

    public MapModule(Plugin plugin, Supplier<GeyserMCNewExtraSpigotConfig.Maps> config) {
        this.plugin = plugin;
        this.config = config;
        this.renderer = new MapCoordinatesRenderer(config);
        this.reducedDebugInfoFile = plugin.getDataFolder().toPath().resolve("maps-reduced-debug-info-worlds.txt");
    }

    public void start() {
        try {
            if (Files.exists(reducedDebugInfoFile)) {
                Files.readAllLines(reducedDebugInfoFile).stream().map(String::trim).filter(l -> !l.isEmpty()).forEach(reducedDebugInfoWorlds::add);
            }
        } catch (IOException e) {
            SERVER.warn(Lang.console("gmne.console.maps_worlds_file_failed", reducedDebugInfoFile, e));
        }
        Bukkit.getWorlds().forEach(this::applyReducedDebugInfo);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        applyReducedDebugInfo(event.getWorld());
    }

    /**
     * Sets the game rule in the worlds of the module, and gives it back in worlds we set it in before
     * but that the option no longer covers.
     */
    private void applyReducedDebugInfo(World world) {
        GeyserMCNewExtraSpigotConfig.Maps settings = config.get();
        boolean wanted = settings.isReducedDebugInfo() && settings.isActiveIn(world);
        boolean setByUs = reducedDebugInfoWorlds.contains(world.getName());
        Boolean current = world.getGameRuleValue(GameRule.REDUCED_DEBUG_INFO);
        if (wanted && !Boolean.TRUE.equals(current)) {
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
            reducedDebugInfoWorlds.add(world.getName());
            saveReducedDebugInfoWorlds();
            SERVER.log(Lang.console("gmne.console.maps_reduced_debug_info_on", world.getName()));
        } else if (!wanted && setByUs) {
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);
            reducedDebugInfoWorlds.remove(world.getName());
            saveReducedDebugInfoWorlds();
            SERVER.log(Lang.console("gmne.console.maps_reduced_debug_info_off", world.getName()));
        }
    }

    private void saveReducedDebugInfoWorlds() {
        try {
            if (reducedDebugInfoWorlds.isEmpty()) {
                Files.deleteIfExists(reducedDebugInfoFile);
            } else {
                Files.createDirectories(reducedDebugInfoFile.getParent());
                Files.write(reducedDebugInfoFile, reducedDebugInfoWorlds);
            }
        } catch (IOException e) {
            SERVER.warn(Lang.console("gmne.console.maps_worlds_file_failed", reducedDebugInfoFile, e));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GeyserMCNewExtraSpigotConfig.Maps settings = config.get();
        if (settings.isStartingMap() && settings.isActiveIn(player.getWorld()) && !player.hasPlayedBefore()) {
            player.getInventory().addItem(new ItemStack(Material.MAP, 1));
        }
        addRendererLater(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMapInitialize(MapInitializeEvent event) {
        GeyserMCNewExtraSpigotConfig.Maps settings = config.get();
        MapView map = event.getMap();
        if (settings.isLegacyConsoleZoom() && map.getWorld() != null && settings.isActiveIn(map.getWorld())) {
            map.setScale(MapView.Scale.FAR);
        }
        addRenderer(map);
    }

    // Maps created before the module was on have no renderer yet: add it when a player gets hold of one
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        addRendererLater(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        addRendererLater(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        addRenderer(event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        addRenderer(event.getCurrentItem());
        addRenderer(event.getCursor());
    }

    private void addRendererLater(Player player) {
        if (!config.get().isEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            addRenderer(player.getInventory().getItemInMainHand());
            addRenderer(player.getInventory().getItemInOffHand());
        }, 1L);
    }

    private void addRenderer(ItemStack stack) {
        if (stack == null || stack.getType() != Material.FILLED_MAP || !(stack.getItemMeta() instanceof MapMeta meta)) {
            return;
        }
        MapView map = meta.getMapView();
        if (map != null) {
            addRenderer(map);
        }
    }

    private void addRenderer(MapView map) {
        // The renderer checks the config on every frame; it stays on maps but draws nothing when the module is off
        if (!config.get().isEnabled()) {
            return;
        }
        for (MapRenderer existing : map.getRenderers()) {
            if (existing instanceof MapCoordinatesRenderer) {
                return;
            }
        }
        map.addRenderer(renderer);
    }
}
