package fr.crossplaymc.geysermcnewextra.core.injectors.bedrock;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.injectors.bedrock.input.BedrockBlockInteractions;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.bedrock.entity.player.input.BedrockPlayerAuthInputTranslator;

@Translator(packet = PlayerAuthInputPacket.class)
// Why is the BedrockPlayerAuthInputTranslator a final class???
// I have to use this to work around that
public class BedrockPlayerAuthInputInjector extends PacketTranslator<PlayerAuthInputPacket> {

    BedrockPlayerAuthInputTranslator translator = new BedrockPlayerAuthInputTranslator();
    @Override
    public void translate(GeyserSession session, PlayerAuthInputPacket packet) {
        translator.translate(session, packet);

        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player == null) {
            return;
        }
        player.setEmoting(packet.getInputData().contains(PlayerAuthInputData.EMOTING));

        for (PlayerAuthInputData input : packet.getInputData()) {
            switch (input) {
                case PERFORM_BLOCK_ACTIONS -> BedrockBlockInteractions.translate(session, packet.getPlayerActions());
                // MISSED_SWING: Geyser itself sends the swing animation back to touch clients now
            }
        }
    }
}
