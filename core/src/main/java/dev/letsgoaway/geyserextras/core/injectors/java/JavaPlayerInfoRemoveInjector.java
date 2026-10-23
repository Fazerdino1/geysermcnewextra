package dev.letsgoaway.geyserextras.core.injectors.java;

import dev.letsgoaway.geyserextras.core.ExtrasPlayer;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.java.entity.player.JavaPlayerInfoRemoveTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;

// Players that left were never removed from the player list menu
@Translator(packet = ClientboundPlayerInfoRemovePacket.class)
public class JavaPlayerInfoRemoveInjector extends JavaPlayerInfoRemoveTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundPlayerInfoRemovePacket packet) {
        super.translate(session, packet);
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player != null) {
            player.getTabListData().remove(packet.getProfileIds());
        }
    }
}
