package fr.crossplaymc.geysermcnewextra.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.utils.TickMath;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.concurrent.TimeUnit;

import static com.velocitypowered.api.scheduler.TaskStatus.SCHEDULED;

public class VelocityExtrasPlayer extends ExtrasPlayer {
    public Player player;
    Scheduler.TaskBuilder builder;
    ScheduledTask task;

    Scheduler scheduler;

    public VelocityExtrasPlayer(GeyserConnection connection) {
        super(connection);
        this.scheduler = GeyserMCNewExtraVelocity.server.getScheduler();
    }

    @Override
    public void startGame() {
        super.startGame();
        this.setTickingState(20.0f);

        GeyserMCNewExtraVelocity.server.getPlayer(getJavaUUID()).ifPresent(value -> this.player = value);
    }

    @Override
    public void setTickingState(float tickrate) {
        super.setTickingState(tickrate);
        if (isDisconnected()) {
            return;
        }
        if (builder == null) {
            builder = scheduler.buildTask(GeyserMCNewExtraVelocity.VELOCITY, this::safeTick);
        }
        if (task != null && task.status().equals(SCHEDULED)) {
            task.cancel();
            task = null;
        }
        builder.clearRepeat();
        builder.repeat(TickMath.toNanos(tickrate), TimeUnit.NANOSECONDS);
        task = builder.schedule();
    }

    @Override
    public void onDisconnect() {
        // The repeating task kept running (and kept this player in memory) after disconnecting (upstream #32)
        if (task != null) {
            task.cancel();
            task = null;
        }
        super.onDisconnect();
    }
}
