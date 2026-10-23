package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Slider;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Toggle;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;

public class MenuSection extends Section {
    @Override
    public void build(BedrockForm menu, GeyserSession session, ExtrasPlayer player) {
        menu.add(new Toggle(player.lang("gmne.settings.menu.doubleClickInventoryShortcut"), player.getPreferences().isEnableDoubleClickShortcut(), (b) -> {
            player.getPreferences().setEnableDoubleClickShortcut(b);
        }));

        menu.add(new Slider(player.lang("gmne.settings.menu.doubleClickTime"), 150, 350, 10, player.getPreferences().getDoubleClickMS(),
                (i) -> {
                    player.getPreferences().setDoubleClickMS(Math.round(i));
                }));

        menu.add(new Toggle(player.translate("options.chat.links.prompt"), player.getPreferences().isPromptOnLinks(), (b) -> {
            player.getPreferences().setPromptOnLinks(b);
        }));

        menu.add(new Toggle(player.lang("gmne.settings.menu.sendSystemToasts"), player.getPreferences().isSendSystemToasts(), (b) -> {
            player.getPreferences().setSendSystemToasts(b);
        }));
    }

    @Override
    public List<String> getHeader(ExtrasPlayer player) {
        return List.of(player.lang("gmne.settings.menu.title"), "");
    }

    @Override
    public FormImage getImage() {
        return FormImage.of(FormImage.Type.PATH, "textures/ui/accessibility_glyph_color.png");
    }
}
