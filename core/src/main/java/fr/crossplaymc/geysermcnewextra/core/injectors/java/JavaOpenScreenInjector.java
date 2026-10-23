package fr.crossplaymc.geysermcnewextra.core.injectors.java;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.features.recipemenus.RecipeMenuLayout;
import fr.crossplaymc.geysermcnewextra.core.features.recipemenus.RecipeMenus;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.java.inventory.JavaOpenScreenTranslator;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;

public class JavaOpenScreenInjector extends JavaOpenScreenTranslator {
    @Override
    public void translate(GeyserSession session, ClientboundOpenScreenPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (player != null && !RecipeMenus.isDisabled()) {
            try {
                // Any new screen replaces the recipe menu that was open
                player.getRecipeMenus().closed();
                RecipeMenuLayout layout = player.getRecipeMenus().layoutFor(session, packet.getType());
                if (layout != null) {
                    player.getRecipeMenus().opened(packet.getContainerId(), layout);
                    super.translate(session, packet.withType(ContainerType.GENERIC_9X3));
                    return;
                }
            } catch (RuntimeException | LinkageError e) {
                player.getRecipeMenus().closed();
                RecipeMenus.disable(e);
            }
        }
        super.translate(session, packet);
    }
}
