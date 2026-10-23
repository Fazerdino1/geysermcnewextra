package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.java.JavaTickingStateTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTickingStatePacket;
@Translator(packet = ClientboundTickingStatePacket.class)
public class JavaTickingStateInjector extends JavaTickingStateTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundTickingStatePacket packet) {
        super.translate(session, packet);
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player != null) {
            player.setTickingState(packet.getTickRate());
        }
    }
}
