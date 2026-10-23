package dev.letsgoaway.geyserextras.bungee;

import dev.letsgoaway.geyserextras.core.ExtrasPlayer;
import dev.letsgoaway.geyserextras.core.GeyserExtras;
import dev.letsgoaway.geyserextras.core.utils.TickMath;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.concurrent.TimeUnit;

public class BungeeExtrasPlayer extends ExtrasPlayer {
    public ProxiedPlayer player;

    public TaskScheduler scheduler;

    public BungeeExtrasPlayer(GeyserConnection connection) {
        super(connection);
        scheduler = GeyserExtrasBungee.BUNGEE.getProxy().getScheduler();
    }

    @Override
    public void startGame() {
        super.startGame();
        GeyserExtras.SERVER.getTickUtil().runIn(1L, this::tickAndReschedule, this);
        this.player = GeyserExtrasBungee.BUNGEE.getProxy().getPlayer(getJavaUUID());
    }

    private void tickAndReschedule() {
        // Stops once the player disconnected; it used to reschedule itself forever (upstream #32)
        if (safeTick()) {
            scheduler.schedule(GeyserExtrasBungee.BUNGEE, this::tickAndReschedule, TickMath.toNanos(this.tickrate), TimeUnit.NANOSECONDS);
        }
    }
}
