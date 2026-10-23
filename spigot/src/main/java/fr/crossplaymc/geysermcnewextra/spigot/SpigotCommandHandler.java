package fr.crossplaymc.geysermcnewextra.spigot;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpigotCommandHandler implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player spigotPlayer) {
            ExtrasPlayer player = ExtrasPlayer.get(spigotPlayer.getUniqueId());
            GeyserMCNewExtra.SERVER.debug("Command /" + label + " from " + spigotPlayer.getUniqueId() + " bedrockPlayer=" + (player != null));
            if (player != null) {
                CommandExecutor.runForBedrock(player, () -> CommandExecutor.runFromCommandInput(player, label, args));
            }
            else {
                CommandExecutor.runFromCommandInput(spigotPlayer.getUniqueId(), label, args);
            }
        }
        return true;
    }
}
