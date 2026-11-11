package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.entity.JavaEntityEventTranslator;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EntityEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityEventPacket;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class JavaEntityEventInjector extends JavaEntityEventTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundEntityEventPacket packet) {
        super.translate(session, packet);
        Entity entity = session.getEntityCache().getEntityByJavaId(packet.getEntityId());
        if (entity == null)
            return;
        // The Bedrock client does not play the shield block sound on its own
        if (packet.getEvent() == EntityEvent.LIVING_SHIELD_BLOCK && GE.getConfig().isEnableShieldBlockSound()) {
            if (packet.getEntityId() == session.getPlayerEntity().getEntityId()) {
                PlaySoundPacket playSoundPacket = new PlaySoundPacket();
                playSoundPacket.setSound("item.shield.block");
                playSoundPacket.setPosition(entity.getPosition());
                playSoundPacket.setVolume(0.8f);
                playSoundPacket.setPitch(1.0f);
                session.sendUpstreamPacket(playSoundPacket);
            }
        }

    }
}
