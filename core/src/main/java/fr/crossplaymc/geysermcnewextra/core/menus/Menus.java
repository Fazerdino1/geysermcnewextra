package fr.crossplaymc.geysermcnewextra.core.menus;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.menus.InGameSettingsMenu;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.util.SettingsUtils;

public enum Menus {
    GEYSER_SETTINGS,
    GE_SETTINGS;

    public CustomForm open(ExtrasPlayer player) {
        switch (this) {
            case GEYSER_SETTINGS -> {
                return SettingsUtils.buildForm(player.getSession());
            }
            case GE_SETTINGS -> {
                return new InGameSettingsMenu().create(player).build();
            }
        }
        return SettingsUtils.buildForm(player.getSession());
    }
}
