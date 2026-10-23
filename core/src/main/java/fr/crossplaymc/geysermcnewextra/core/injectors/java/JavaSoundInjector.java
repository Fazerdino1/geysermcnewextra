package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.parity.java.combat.SoundReplacer;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.java.level.JavaSoundTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSoundPacket;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

@Translator(packet = ClientboundSoundPacket.class)
public class JavaSoundInjector extends JavaSoundTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundSoundPacket packet) {
        // Only the combat sounds are replaced; everything else goes through Geyser unchanged
        if (!GE.getConfig().isEnableJavaCombatSounds() || !SoundReplacer.soundMap.containsKey(packet.getSound().getName())) {
            super.translate(session, packet);
            return;
        }

        PlaySoundPacket playSoundPacket = new PlaySoundPacket();
        playSoundPacket.setSound(SoundReplacer.getSound(packet.getSound().getName()));
        playSoundPacket.setPosition(Vector3f.from(packet.getX(), packet.getY(), packet.getZ()));
        playSoundPacket.setVolume(packet.getVolume());
        playSoundPacket.setPitch(packet.getPitch());
        session.sendUpstreamPacket(playSoundPacket);
    }
}
