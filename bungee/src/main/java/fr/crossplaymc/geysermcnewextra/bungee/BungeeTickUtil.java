package fr.crossplaymc.geysermcnewextra.bungee;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.utils.TickMath;
import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;

import java.util.concurrent.TimeUnit;

public class BungeeTickUtil implements TickUtil {

    @Override
    public void runIn(long ticks, Runnable func, ExtrasPlayer player) {
        if (player instanceof BungeeExtrasPlayer bungeeExtrasPlayer) {
            bungeeExtrasPlayer.scheduler.schedule(GeyserMCNewExtraBungee.BUNGEE, func, TickMath.toNanos(player.getTickrate()) * ticks, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void runSync(Runnable func, ExtrasPlayer player) {
        if (player instanceof BungeeExtrasPlayer bungeeExtrasPlayer) {
            bungeeExtrasPlayer.scheduler.schedule(GeyserMCNewExtraBungee.BUNGEE, func, TickMath.toNanos(player.getTickrate()), TimeUnit.NANOSECONDS);
        }
    }
}
