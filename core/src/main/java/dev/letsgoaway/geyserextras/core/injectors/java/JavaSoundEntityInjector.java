package dev.letsgoaway.geyserextras.core.injectors.java;

import dev.letsgoaway.geyserextras.core.parity.java.combat.SoundReplacer;
import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.java.entity.JavaSoundEntityTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSoundEntityPacket;

import static dev.letsgoaway.geyserextras.core.GeyserExtras.GE;

@Translator(packet = ClientboundSoundEntityPacket.class)
public class JavaSoundEntityInjector extends JavaSoundEntityTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundSoundEntityPacket packet) {
        // Only the combat sounds are replaced; everything else goes through Geyser unchanged
        if (!GE.getConfig().isEnableJavaCombatSounds() || !SoundReplacer.soundMap.containsKey(packet.getSound().getName())) {
            super.translate(session, packet);
            return;
        }

        Entity entity = session.getEntityCache().getEntityByJavaId(packet.getEntityId());
        if (entity == null) {
            return;
        }

        PlaySoundPacket playSoundPacket = new PlaySoundPacket();
        playSoundPacket.setSound(SoundReplacer.getSound(packet.getSound().getName()));
        // Same position Geyser uses for entity sounds (includes the Bedrock offset, e.g. player eye height)
        playSoundPacket.setPosition(entity.bedrockPosition());
        playSoundPacket.setVolume(packet.getVolume());
        playSoundPacket.setPitch(packet.getPitch());
        session.sendUpstreamPacket(playSoundPacket);
    }
}
