package fr.crossplaymc.geysermcnewextra.extension;

import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.utils.TickMath;

import java.util.concurrent.TimeUnit;

public class ExtensionTickUtil implements TickUtil {
    @Override
    public void runIn(long ticks, Runnable func, ExtrasPlayer player) {
        player.getSession().scheduleInEventLoop(func, TickMath.toNanos(player.getTickrate()) * ticks, TimeUnit.NANOSECONDS);
    }

    @Override
    public void runSync(Runnable func, ExtrasPlayer player) {
        player.getSession().getTickEventLoop().schedule(func, 0, TimeUnit.NANOSECONDS);
    }
}
