package fr.crossplaymc.geysermcnewextra.bungee;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {
    @EventHandler
    public void onPlayerJoin(ServerConnectEvent ev) {
        if (GeyserMCNewExtraBungee.CORE != null) {
            GeyserMCNewExtraBungee.CORE.onJavaPlayerJoin(ev.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent ev) {
        if (GeyserMCNewExtraBungee.CORE != null) {
            GeyserMCNewExtraBungee.CORE.onJavaPlayerLeave(ev.getPlayer().getUniqueId());
        }
    }
}
