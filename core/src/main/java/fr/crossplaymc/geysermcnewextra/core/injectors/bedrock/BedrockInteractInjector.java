package fr.crossplaymc.geysermcnewextra.core.injectors.bedrock;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Action;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Remappable;
import org.cloudburstmc.protocol.bedrock.packet.InteractPacket;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.translator.protocol.bedrock.entity.player.BedrockInteractTranslator;

import java.util.concurrent.TimeUnit;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

@Translator(packet = InteractPacket.class)
public class BedrockInteractInjector extends BedrockInteractTranslator {
    @Override
    public void translate(GeyserSession session, InteractPacket packet) {
        ExtrasPlayer player = ExtrasPlayer.get(session);
        if (packet.getAction().equals(InteractPacket.Action.OPEN_INVENTORY)) {
            SERVER.debug("OPEN_INVENTORY xuid=" + session.xuid() + " known=" + (player != null) + " sneaking=" + session.isSneaking() + " t=" + System.currentTimeMillis());
        }
        if (player == null || !packet.getAction().equals(InteractPacket.Action.OPEN_INVENTORY)) {
            super.translate(session, packet);
        } else {
            if ((player.getPreferences().isEnableDoubleClickShortcut() && GE.getConfig().isEnableMenu())
                    && !(player.getSession().isSneaking() && !player.getPreferences().isDefault(Remappable.SNEAK_INVENTORY))) {
                // Double click
                if (player.getPreferences().getDoubleClickMS() > System.currentTimeMillis() - player.getLastInventoryClickTime()) {
                    if (player.getDoubleClickShortcutFuture() != null && !player.getDoubleClickShortcutFuture().isCancelled() && !player.getDoubleClickShortcutFuture().isDone()) {
                        player.getDoubleClickShortcutFuture().cancel(false);
                        // open menu
                        Action.OPEN_GE_MENU.run(player);
                    } else {
                        player.setLastInventoryClickTime(System.currentTimeMillis());
                        player.setDoubleClickShortcutFuture(session.scheduleInEventLoop(() -> {
                            player.getPreferences().runAction(Remappable.OPEN_INVENTORY);
                        }, player.getPreferences().getDoubleClickMS() + 20, TimeUnit.MILLISECONDS));
                    }
                    return;
                } else {
                    player.setLastInventoryClickTime(System.currentTimeMillis());
                    player.setDoubleClickShortcutFuture(session.scheduleInEventLoop(() -> {
                        player.getPreferences().runAction(Remappable.OPEN_INVENTORY);
                    }, player.getPreferences().getDoubleClickMS() + 20, TimeUnit.MILLISECONDS));
                    return;
                }
            }
            Remappable bind = player.getSession().isSneaking() ? Remappable.SNEAK_INVENTORY : Remappable.OPEN_INVENTORY;
            if (player.getPreferences().isDefault(bind) || player.getPreferences().getAction(bind).equals(Action.OPEN_INVENTORY)) {
                super.translate(session, packet);
                return;
            }
            Entity entity;
            if (packet.getRuntimeEntityId() == session.getPlayerEntity().geyserId()) {
                //Player is not in entity cache
                entity = session.getPlayerEntity();
            } else {
                entity = session.getEntityCache().getEntityByGeyserId(packet.getRuntimeEntityId());
            }
            if (entity == null)
                return;
            player.getPreferences().getAction(bind).run(player);
        }
    }
}
