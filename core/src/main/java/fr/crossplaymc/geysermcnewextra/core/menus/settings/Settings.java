package fr.crossplaymc.geysermcnewextra.core.menus.settings;

import fr.crossplaymc.geysermcnewextra.core.menus.settings.sections.*;

import java.util.List;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class Settings {
    public static List<Section> list() {
        if (GE.getConfig().isDebugMode()) {
            return List.of(
                    new VideoSection(),
                    new MenuSection(),
                    new HudSection(),
                    new BindingsSection(),
                    new DebugSection(),
                    new CreditsSection()
            );
        }
        else {
            return List.of(
                    new VideoSection(),
                    new MenuSection(),
                    new HudSection(),
                    new BindingsSection(),
                    new CreditsSection()
            );
        }
    }
}
