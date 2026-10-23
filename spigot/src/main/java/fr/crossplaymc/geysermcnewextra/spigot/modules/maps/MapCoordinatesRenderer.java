package fr.crossplaymc.geysermcnewextra.spigot.modules.maps;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.spigot.config.GeyserMCNewExtraSpigotConfig;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import java.util.function.Supplier;

/**
 * Draws the viewer's coordinates at the top of the map they hold, and other players' markers (locator maps).
 * Adapted from MapXYZ by LetsGoAway (MIT, see CREDITS.md); uses Bukkit's own Minecraft font.
 */
@SuppressWarnings("deprecation")
public final class MapCoordinatesRenderer extends MapRenderer {
    private static final byte TEXT_COLOR = MapPalette.matchColor(13, 13, 13);
    private static final int TEXT_HEIGHT = 10;
    // Blue pointers, and red ones in the Nether as on Bedrock Edition locator maps. Looked up by name: they were
    // renamed in 1.20.2 (the old RED_MARKER is another icon), and the constants of the API we compile against
    // would be renamed by the server for this plugin
    private static final boolean NEW_CURSOR_NAMES = cursorType("BLUE_MARKER") != null;
    private static final MapCursor.Type MARKER = cursorType(NEW_CURSOR_NAMES ? "BLUE_MARKER" : "BLUE_POINTER");
    private static final MapCursor.Type NETHER_MARKER = cursorType(NEW_CURSOR_NAMES ? "RED_MARKER" : "RED_POINTER");

    private final Supplier<GeyserMCNewExtraSpigotConfig.Maps> config;

    public MapCoordinatesRenderer(Supplier<GeyserMCNewExtraSpigotConfig.Maps> config) {
        // Contextual: each player gets their own canvas, so everyone sees their own coordinates
        super(true);
        this.config = config;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        // The text of the previous frame stays on a contextual canvas: draw the map back under it
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < TEXT_HEIGHT; y++) {
                canvas.setPixel(x, y, canvas.getBasePixel(x, y));
            }
        }
        removeOurMarkers(canvas.getCursors());

        GeyserMCNewExtraSpigotConfig.Maps settings = config.get();
        if (!settings.isActiveIn(player.getWorld())) {
            return;
        }
        if (settings.isLocatorMaps()) {
            drawOtherPlayers(player, canvas, map);
        }
        // Only on the map in hand: a map in an item frame would otherwise show the viewer's own position
        if (settings.isCoordinates() && isHolding(player, map)) {
            Location pos = settings.isEyeLevelPosition() ? player.getEyeLocation() : player.getLocation();
            String text = Lang.get("gmne.maps.coordinates", locale(player),
                    String.valueOf(pos.getBlockX()), String.valueOf(pos.getBlockY()), String.valueOf(pos.getBlockZ()));
            canvas.drawText(0, 0, MinecraftFont.Font, "§" + TEXT_COLOR + ";" + text);
        }
    }

    private static MapCursor.Type cursorType(String name) {
        try {
            return (MapCursor.Type) MapCursor.Type.class.getField(name).get(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static String locale(Player player) {
        ExtrasPlayer bedrock = ExtrasPlayer.get(player.getUniqueId());
        return bedrock != null ? bedrock.getSession().locale() : player.getLocale();
    }

    private static boolean isHolding(Player player, MapView map) {
        return mapId(player.getInventory().getItemInMainHand()) == map.getId()
                || mapId(player.getInventory().getItemInOffHand()) == map.getId();
    }

    private static int mapId(ItemStack stack) {
        if (stack == null || stack.getType() != Material.FILLED_MAP || !(stack.getItemMeta() instanceof MapMeta meta)) {
            return -1;
        }
        MapView view = meta.getMapView();
        return view == null ? -1 : view.getId();
    }

    private static void removeOurMarkers(MapCursorCollection cursors) {
        for (int i = cursors.size() - 1; i >= 0; i--) {
            MapCursor.Type type = cursors.getCursor(i).getType();
            if (type != null && (type.equals(MARKER) || type.equals(NETHER_MARKER))) {
                cursors.removeCursor(cursors.getCursor(i));
            }
        }
    }

    private static void drawOtherPlayers(Player viewer, MapCanvas canvas, MapView map) {
        World world = map.getWorld();
        if (world == null || !world.equals(viewer.getWorld()) || MARKER == null || NETHER_MARKER == null) {
            return;
        }
        int scale = 1 << map.getScale().getValue();
        boolean nether = world.getEnvironment() == World.Environment.NETHER;
        for (Player other : world.getPlayers()) {
            // Vanished players and spectators stay hidden
            if (other.equals(viewer) || !viewer.canSee(other) || other.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            float x = (float) (other.getLocation().getX() - map.getCenterX()) / scale;
            float z = (float) (other.getLocation().getZ() - map.getCenterZ()) / scale;
            canvas.getCursors().addCursor(new MapCursor(MapMath.clampCoordinate(x), MapMath.clampCoordinate(z),
                    MapMath.direction(other.getLocation().getYaw(), nether, other.getTicksLived()),
                    nether ? NETHER_MARKER : MARKER, true, other.getName()));
        }
    }
}
