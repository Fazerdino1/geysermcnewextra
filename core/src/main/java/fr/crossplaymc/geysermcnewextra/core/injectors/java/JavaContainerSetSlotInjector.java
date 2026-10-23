package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.features.recipemenus.RecipeMenuLayout;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaContainerSetSlotTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;

// Recipe menus: move the server menu's slot into the chest shown to the player
public class JavaContainerSetSlotInjector extends JavaContainerSetSlotTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundContainerSetSlotPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        RecipeMenuLayout layout = player == null ? null : player.getRecipeMenus().layoutOf(packet.getContainerId());
        if (layout != null) {
            player.getRecipeMenus().stateId(packet.getStateId());
            int slot = layout.toChest(packet.getSlot());
            if (slot == RecipeMenuLayout.NONE) {
                return;
            }
            packet = packet.withSlot(slot);
        }
        super.translate(session, packet);
    }
}
