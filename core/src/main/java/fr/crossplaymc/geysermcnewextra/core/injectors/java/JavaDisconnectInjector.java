package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.text.MessageTranslator;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
// Doesnt work lmao
@Translator(packet = ClientboundDisconnectPacket.class)
public class JavaDisconnectInjector extends PacketTranslator<ClientboundDisconnectPacket> {
    @Override
    public void translate(GeyserSession session, ClientboundDisconnectPacket packet) {
        if (GE.getConfig().isAutoReconnect()) {
            ExtrasPlayer player = ExtrasPlayer.get(session);
            if (player == null) {
                return;
            }

            player.sendToast("", MessageTranslator.convertMessage(packet.getReason(), session.locale()));

            player.reconnect();
        }
    }
}
