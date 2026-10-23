package fr.crossplaymc.geysermcnewextra.core.menus.settings.menus;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockMenu;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Button;
import fr.crossplaymc.geysermcnewextra.core.locale.BedrockLocale;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.Settings;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.sections.Section;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;

public class SettingsMenu extends BedrockMenu {
    @Override

    public SimpleForm create(ExtrasPlayer player) {
        setTitle(player.lang("gmne.settings"));
        setHeader(player.lang("gmne.settings.section.submit_warning"));
        GeyserSession session = player.getSession();
        List<Section> sections = Settings.list();

        for (Section section : sections) {
            section.create(this, session, player);
        }

        add(new Button(BedrockLocale.CONTROLLER.RESET_TO_DEFAULT, () -> {
            player.sendForm(new ResetModal());
        }));

        return super.create(player);
    }
}
