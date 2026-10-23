package fr.crossplaymc.geysermcnewextra.velocity;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;

public class VelocityListener {
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerJoin(LoginEvent ev) {
        if (GeyserMCNewExtraVelocity.CORE != null) {
            GeyserMCNewExtraVelocity.CORE.onJavaPlayerJoin(ev.getPlayer().getUniqueId());
        }
    }
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerKick(KickedFromServerEvent ev) {
        // Only the kicked player: this used to reconnect every Bedrock player on the proxy
        // whenever anyone was kicked from a backend server
        if (GeyserMCNewExtraVelocity.CORE == null || !GeyserMCNewExtraVelocity.CORE.getConfig().isAutoReconnect()) {
            return;
        }
        ExtrasPlayer player = ExtrasPlayer.get(ev.getPlayer().getUniqueId());
        if (player != null) {
            player.reconnect();
        }
    }
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerLeave(DisconnectEvent ev) {
        if (GeyserMCNewExtraVelocity.CORE != null) {
            GeyserMCNewExtraVelocity.CORE.onJavaPlayerLeave(ev.getPlayer().getUniqueId());
        }
    }
}
