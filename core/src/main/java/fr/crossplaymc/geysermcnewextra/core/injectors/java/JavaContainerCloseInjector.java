package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaContainerCloseTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;

public class JavaContainerCloseInjector extends JavaContainerCloseTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundContainerClosePacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player != null && player.getRecipeMenus().layoutOf(packet.getContainerId()) != null) {
            player.getRecipeMenus().closed();
        }
        super.translate(session, packet);
    }
}
