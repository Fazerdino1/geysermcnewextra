package dev.letsgoaway.geyserextras.core.injectors.java;

import dev.letsgoaway.geyserextras.core.ExtrasPlayer;
import dev.letsgoaway.geyserextras.core.parity.java.menus.tablist.TabListData;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.java.entity.player.JavaPlayerInfoUpdateTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;


@Translator(packet = ClientboundPlayerInfoUpdatePacket.class)
public class JavaPlayerInfoUpdateInjector extends JavaPlayerInfoUpdateTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundPlayerInfoUpdatePacket packet) {
        super.translate(session, packet);
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player == null || !TabListData.isTrackingAvailable()) {
            return;
        }
        try {
            player.getTabListData().apply(packet.getActions(), packet.getEntries());
        } catch (LinkageError e) {
            // e.g. the extension jar inside Geyser-Spigot, where Adventure is relocated (upstream #54)
            TabListData.disableTracking(e);
        }
    }
}
