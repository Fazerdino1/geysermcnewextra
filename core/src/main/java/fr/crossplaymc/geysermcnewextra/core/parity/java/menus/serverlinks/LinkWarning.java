package fr.crossplaymc.geysermcnewextra.core.parity.java.menus.serverlinks;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockModal;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.geyser.text.ChatColor;
import org.geysermc.mcprotocollib.protocol.data.game.ServerLink;

public class LinkWarning extends BedrockModal {
    private final ServerLink link;

    public LinkWarning(ServerLink link) {
        this.link = link;
    }

    @Override
    public ModalForm create(ExtrasPlayer player) {
        setTitle("");
        setContent(player.translate("chat.link.confirm") + "\n\n" + link.link() + "\n" + ChatColor.RED + player.translate("chat.link.warning"));
        return super.create(player);
    }

    @Override
    public void onSubmit(ExtrasPlayer player, boolean accepted) {
        if (accepted) {
            player.sendForm(new LinkMenu(link));
        } else {
            player.sendForm(new ServerLinksMenu());
        }
        super.onSubmit(player, accepted);
    }
}
