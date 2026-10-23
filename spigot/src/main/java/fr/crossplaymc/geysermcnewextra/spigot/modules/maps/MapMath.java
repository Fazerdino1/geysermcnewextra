package fr.crossplaymc.geysermcnewextra.spigot.modules.maps;

/**
 * Map cursor maths, adapted from MapXYZ by LetsGoAway (MIT).
 */
public final class MapMath {
    private MapMath() {
    }

    /**
     * @param coordinate blocks from the map center, divided by the map scale
     * @return the cursor coordinate, pinned to the map's edge when the player is outside it
     */
    public static byte clampCoordinate(float coordinate) {
        if (coordinate <= -63.0F) {
            return -128;
        }
        if (coordinate >= 63.0F) {
            return 127;
        }
        return (byte) (int) (coordinate * 2.0F + 0.5);
    }

    /**
     * @return the cursor direction (0 to 15, 0 = south) for a player's yaw; spins randomly in the Nether,
     * like the vanilla player marker
     */
    public static byte direction(float yaw, boolean nether, int ticksLived) {
        if (nether) {
            int i = ticksLived / 10;
            return (byte) (i * i * 34187121 + i * 121 >> 15 & 15);
        }
        double wrapped = ((yaw % 360) + 360) % 360;
        return (byte) ((int) Math.floor((wrapped + 11.25) / 22.5) & 15);
    }
}
