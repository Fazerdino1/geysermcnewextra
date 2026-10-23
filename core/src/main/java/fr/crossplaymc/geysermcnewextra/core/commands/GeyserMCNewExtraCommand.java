package fr.crossplaymc.geysermcnewextra.core.commands;

import fr.crossplaymc.geysermcnewextra.core.config.ConfigLoader;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.menus.MainMenu;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.menus.InGameSettingsMenu;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.menus.SettingsMenu;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Action;

import java.util.List;

public class GeyserMCNewExtraCommand implements BedrockCommand {

    @Override
    public void onExecute(ExtrasPlayer player, List<String> args, String label) {
        if (!args.isEmpty()) {
            String subcommand = args.get(0);
            /*
            if (subcommand.equalsIgnoreCase("reload")) {
                ConfigLoader.load();
                return;
            }
            else
            */
            if (subcommand.equalsIgnoreCase("settings")) {
                player.sendForm(new SettingsMenu());
                return;
            }
        }
        Action.OPEN_GE_MENU.run(player);
    }
}
