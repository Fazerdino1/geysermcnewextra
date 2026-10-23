package fr.crossplaymc.geysermcnewextra.core.menus.settings.menus;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.form.BedrockForm;
import fr.crossplaymc.geysermcnewextra.core.form.elements.Label;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.Settings;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.sections.*;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.text.ChatColor;

import java.util.List;

public class InGameSettingsMenu extends BedrockForm {
    @Override
    public CustomForm.Builder create(ExtrasPlayer player) {
        setTitle(player.lang("gmne.settings"));
        GeyserSession session = player.getSession();
        List<Section> sections = Settings.list();

        add(new Label(player.lang("gmne.settings.reset.ingame")));

        for (Section section : sections) {
            section.create(this, session, player, true);
        }

        return super.create(player);
    }

    @Override
    public void onSubmit(ExtrasPlayer player) {
        player.getPreferences().save();
    }
}
