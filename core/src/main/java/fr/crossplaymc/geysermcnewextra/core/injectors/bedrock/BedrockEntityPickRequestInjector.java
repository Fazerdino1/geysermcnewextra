package fr.crossplaymc.geysermcnewextra.core.injectors.bedrock;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Remappable;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import org.cloudburstmc.protocol.bedrock.packet.EntityPickRequestPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.bedrock.BedrockEntityPickRequestTranslator;

@Translator(packet = EntityPickRequestPacket.class)
public class BedrockEntityPickRequestInjector extends BedrockEntityPickRequestTranslator {

    @Override
    public void translate(GeyserSession session, EntityPickRequestPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player == null || player.getPreferences().isDefault(Remappable.PICK_BLOCK))
            super.translate(session, packet);
        else
            player.getPreferences().runAction(Remappable.PICK_BLOCK);
    }
}
