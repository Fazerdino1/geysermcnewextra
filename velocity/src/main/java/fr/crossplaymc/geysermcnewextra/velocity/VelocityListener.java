package fr.crossplaymc.geysermcnewextra.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;

public class VelocityListener {
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerJoin(LoginEvent ev) {
        if (GeyserMCNewExtraVelocity.CORE != null) {
            GeyserMCNewExtraVelocity.CORE.onJavaPlayerJoin(ev.getPlayer().getUniqueId());
        }
    }
    @Subscribe(order = PostOrder.LAST)
    public void onPlayerLeave(DisconnectEvent ev) {
        if (GeyserMCNewExtraVelocity.CORE != null) {
            GeyserMCNewExtraVelocity.CORE.onJavaPlayerLeave(ev.getPlayer().getUniqueId());
        }
    }
}
