package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaContainerSetDataTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetDataPacket;

// Recipe menus: the anvil cost is shown on an item of the chest
public class JavaContainerSetDataInjector extends JavaContainerSetDataTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundContainerSetDataPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player != null && player.getRecipeMenus().onProperty(session, packet.getContainerId(), packet.getRawProperty(), packet.getValue())) {
            return;
        }
        super.translate(session, packet);
    }
}
