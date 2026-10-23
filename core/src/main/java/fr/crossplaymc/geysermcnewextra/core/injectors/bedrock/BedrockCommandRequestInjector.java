package fr.crossplaymc.geysermcnewextra.core.injectors.bedrock;

import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.bedrock.BedrockCommandRequestTranslator;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;

@Translator(packet = CommandRequestPacket.class)
public class BedrockCommandRequestInjector extends BedrockCommandRequestTranslator {
    @Override
    public void translate(GeyserSession session, CommandRequestPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player != null && CommandExecutor.isExtrasCommand(packet.getCommand())) {
            CommandExecutor.runForBedrock(player, () -> CommandExecutor.runFromInput(player, packet.getCommand()));
        } else {
            super.translate(session, packet);
        }
    }
}
