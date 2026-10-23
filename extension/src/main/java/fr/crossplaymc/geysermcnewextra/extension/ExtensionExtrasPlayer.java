package fr.crossplaymc.geysermcnewextra.extension;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.utils.TickMath;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExtensionExtrasPlayer extends ExtrasPlayer {
    private ScheduledFuture<?> tickThread = null;

    public ExtensionExtrasPlayer(GeyserConnection connection) {
        super(connection);
    }

    @Override
    public void startGame() {
        super.startGame();
        this.setTickingState(20.0f);
    }

    @Override
    public void setTickingState(float tickrate) {
        super.setTickingState(tickrate);
        if (isDisconnected()) {
            return;
        }
        // Replace the previous task: each /tick rate change used to add one more (upstream #32)
        if (tickThread != null) {
            tickThread.cancel(false);
        }
        tickThread = session.getTickEventLoop().scheduleAtFixedRate(this::safeTick, TickMath.toNanos(tickrate), TickMath.toNanos(tickrate), TimeUnit.NANOSECONDS);
    }

    @Override
    public void onDisconnect() {
        if (tickThread != null) {
            tickThread.cancel(true);
            tickThread = null;
        }
        super.onDisconnect();
    }
}
