package fr.crossplaymc.geysermcnewextra.spigot.config;

import java.util.List;

public final class WorldScope {
    private WorldScope() {
    }

    /**
     * @return whether a module limited to {@code worlds} works in {@code world}; an empty list means every world
     */
    public static boolean contains(List<String> worlds, String world) {
        if (worlds == null || worlds.isEmpty()) {
            return true;
        }
        for (String name : worlds) {
            if (name != null && name.trim().equalsIgnoreCase(world)) {
                return true;
            }
        }
        return false;
    }
}
