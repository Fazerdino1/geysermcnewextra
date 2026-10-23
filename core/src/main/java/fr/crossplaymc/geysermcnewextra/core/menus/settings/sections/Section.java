package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockMenu;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Button;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Label;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.menus.SectionMenu;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;

public class Section {
    public void create(BedrockForm menu, GeyserSession session, ExtrasPlayer player, boolean includeHeader) {
        if (includeHeader) {
            menu.add(new SectionLabel(getHeader(player).get(0), getHeader(player).get(1)));
        }
        else {
            // Add padding because bedrock doesnt lmao
            menu.add(new Label(""));
        }
        build(menu, session, player);
    }

    public void create(BedrockMenu menu, GeyserSession session, ExtrasPlayer player) {
        menu.add(new Button(getHeader(player).get(0), getImage().type(), getImage().data(), () -> {
            player.sendForm(new SectionMenu(this));
        }));
    }

    public void build(BedrockForm menu, GeyserSession session, ExtrasPlayer player) {
    }

    public List<String> getHeader(ExtrasPlayer player) {
        return List.of("SectionTitle", "SectionDescription");
    }

    // Used in the GeyserMCNewExtra Menu Settings instead of the game settings as its easier to navigate on consoles
    public FormImage getImage() {
        return FormImage.of(FormImage.Type.PATH, "textures/ui/settings_glyph_color_2x.png");
    }
}
