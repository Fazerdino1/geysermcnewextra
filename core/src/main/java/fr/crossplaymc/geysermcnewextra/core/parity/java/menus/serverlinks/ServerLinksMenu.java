package fr.crossplaymc.geysermcnewextra.core.parity.java.menus.serverlinks;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockMenu;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Button;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.data.game.ServerLink;

import java.util.List;

public class ServerLinksMenu extends BedrockMenu {

    @Override
    public SimpleForm create(ExtrasPlayer player) {
        GeyserSession session = player.getSession();
        setTitle(player.translate("menu.server_links.title"));
        List<ServerLink> links = player.getServerLinksData().getServerLinks();
        for (ServerLink link : links) {
            String linkText = ServerLinksData.getLinkText(link, player);

            add(new Button(linkText, () -> {
                if (player.getPreferences().isPromptOnLinks()) {
                    player.sendForm(new LinkWarning(link));
                }
                else {
                    player.sendForm(new LinkMenu(link));
                }
            }));
        }
        return super.create(player);
    }
}
