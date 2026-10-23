package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.elements.*;
import fr.crossplaymc.geysermcnewextra.core.preferences.Perspectives;
import fr.crossplaymc.geysermcnewextra.core.utils.GUIElements;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class HudSection extends Section {
    @Override
    public void build(BedrockForm menu, GeyserSession session, ExtrasPlayer player) {
        for (GUIElements element : GUIElements.values()) {
            if (GE.getConfig().isDisablePaperDoll() && element.equals(GUIElements.PAPER_DOLL)) continue;
            menu.add(createToggle(element, player));
        }
    }

    @Override
    public List<String> getHeader(ExtrasPlayer player) {
        return List.of("HUD", "");
    }

    private static Toggle createToggle(GUIElements element, ExtrasPlayer player) {
        GeyserSession session = player.getSession();
        return new Toggle(element.translateOptions(player),
                element.isHidden(session), (hide) -> {
            if (hide && !element.isHidden(session)) {
                element.hide(session);
            } else if (!hide && element.isHidden(session)) {
                element.show(session);
            }
        });
    }

    @Override
    public FormImage getImage() {
        return FormImage.of(FormImage.Type.PATH, "textures/ui/heart_new.png");
    }
}
