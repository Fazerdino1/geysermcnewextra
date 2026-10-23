package fr.crossplaymc.geysermcnewextra.spigot;

import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import org.bukkit.Bukkit;

public class SpigotTickUtil implements TickUtil {

    @Override
    public void runIn(long ticks, Runnable func, ExtrasPlayer player) {
        Bukkit.getScheduler().runTaskLater(GeyserMCNewExtraSpigot.SPIGOT, func, ticks);
    }

    @Override
    public void runSync(Runnable func, ExtrasPlayer player) {
        Bukkit.getScheduler().runTask(GeyserMCNewExtraSpigot.SPIGOT, func);
    }
}
