package fr.crossplaymc.geysermcnewextra.core.commands;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.parity.java.menus.tablist.PlayerListMenu;

import java.util.List;
import java.util.UUID;

public class TabListCommand implements BedrockCommand {

    @Override
    public void onExecute(ExtrasPlayer player, List<String> args, String label) {
        player.sendForm(new PlayerListMenu());
    }

}
