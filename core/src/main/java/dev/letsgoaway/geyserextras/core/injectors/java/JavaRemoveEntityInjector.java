package dev.letsgoaway.geyserextras.core.injectors.java;

import dev.letsgoaway.geyserextras.core.ExtrasPlayer;
import dev.letsgoaway.geyserextras.core.injectors.GeyserHandler;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.entity.JavaRemoveEntitiesTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;

public class JavaRemoveEntityInjector extends JavaRemoveEntitiesTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundRemoveEntitiesPacket packet) {
        super.translate(session, packet);
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player == null) {
            return;
        }
        for (int entityId : packet.getEntityIds()) {
            player.getPlayerDimensionsMap().remove(entityId);
        }
    }
}
