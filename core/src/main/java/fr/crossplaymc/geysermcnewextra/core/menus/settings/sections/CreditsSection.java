package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.cache.Cache;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Label;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;

public class CreditsSection extends Section {
    @Override
    public void build(BedrockForm menu, GeyserSession session, ExtrasPlayer player) {
        // Our credits first (bundled, translated), then the original project's credits (downloaded)
        menu.add(new Label(player.lang("gmne.credits")));
        menu.add(new Label(Cache.CREDITS_TEXT));
    }

    @Override
    public List<String> getHeader(ExtrasPlayer player) {
        return List.of(BedrockLocale.OPTIONS.CREDITS, "");
    }

    @Override
    public FormImage getImage() {
        return FormImage.of(FormImage.Type.PATH, "textures/ui/permissions_member_star.png");
    }
}
