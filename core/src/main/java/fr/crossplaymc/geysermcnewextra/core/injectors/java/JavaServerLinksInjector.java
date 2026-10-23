package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundServerLinksPacket;

@Translator(packet = ClientboundServerLinksPacket.class)
public class JavaServerLinksInjector extends PacketTranslator<ClientboundServerLinksPacket> {
    @Override
    public void translate(GeyserSession session, ClientboundServerLinksPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player == null) {
            return;
        }
        player.getServerLinksData().setServerLinks(packet.getLinks());
    }
}
