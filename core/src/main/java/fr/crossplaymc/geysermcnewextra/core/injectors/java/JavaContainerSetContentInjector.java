package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.features.recipemenus.RecipeMenuLayout;
import fr.crossplaymc.geysermcnewextra.core.features.recipemenus.RecipeMenus;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaContainerSetContentTranslator;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;

// Recipe menus: move the server menu's slots into the chest shown to the player
public class JavaContainerSetContentInjector extends JavaContainerSetContentTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundContainerSetContentPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        RecipeMenuLayout layout = player == null ? null : player.getRecipeMenus().layoutOf(packet.getContainerId());
        if (layout != null) {
            try {
                player.getRecipeMenus().stateId(packet.getStateId());
                super.translate(session, packet.withItems(player.getRecipeMenus().toChestContents(session, layout, packet.getItems())));
                return;
            } catch (RuntimeException | LinkageError e) {
                player.getRecipeMenus().closed();
                RecipeMenus.disable(e);
            }
        }
        super.translate(session, packet);
    }
}
