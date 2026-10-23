package fr.crossplaymc.geysermcnewextra.bungee;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra;
import fr.crossplaymc.geysermcnewextra.core.utils.TickMath;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.concurrent.TimeUnit;

public class BungeeExtrasPlayer extends ExtrasPlayer {
    public ProxiedPlayer player;

    public TaskScheduler scheduler;

    public BungeeExtrasPlayer(GeyserConnection connection) {
        super(connection);
        scheduler = GeyserMCNewExtraBungee.BUNGEE.getProxy().getScheduler();
    }

    @Override
    public void startGame() {
        super.startGame();
        GeyserMCNewExtra.SERVER.getTickUtil().runIn(1L, this::tickAndReschedule, this);
        this.player = GeyserMCNewExtraBungee.BUNGEE.getProxy().getPlayer(getJavaUUID());
    }

    private void tickAndReschedule() {
        // Stops once the player disconnected; it used to reschedule itself forever (upstream #32)
        if (safeTick()) {
            scheduler.schedule(GeyserMCNewExtraBungee.BUNGEE, this::tickAndReschedule, TickMath.toNanos(this.tickrate), TimeUnit.NANOSECONDS);
        }
    }
}
