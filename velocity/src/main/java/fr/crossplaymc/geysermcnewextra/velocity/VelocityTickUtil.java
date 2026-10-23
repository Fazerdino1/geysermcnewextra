package fr.crossplaymc.geysermcnewextra.velocity;

import fr.crossplaymc.geysermcnewextra.core.utils.TickUtil;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.utils.TickMath;

import java.util.concurrent.TimeUnit;

public class VelocityTickUtil implements TickUtil {

    @Override
    public void runIn(long ticks, Runnable func, ExtrasPlayer player) {
        GeyserMCNewExtraVelocity.server.getScheduler()
                .buildTask(GeyserMCNewExtraVelocity.VELOCITY, func)
                .delay(TickMath.toNanos(player.getTickrate()) * ticks, TimeUnit.NANOSECONDS)
                .schedule();
    }

    @Override
    public void runSync(Runnable func, ExtrasPlayer player) {
        GeyserMCNewExtraVelocity.server.getScheduler()
                .buildTask(GeyserMCNewExtraVelocity.VELOCITY, func)
                .delay(TickMath.toNanos(player.getTickrate()), TimeUnit.NANOSECONDS)
                .clearRepeat()
                .schedule();
    }
}
