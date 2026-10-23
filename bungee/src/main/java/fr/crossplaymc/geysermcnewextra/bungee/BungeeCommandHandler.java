package fr.crossplaymc.geysermcnewextra.bungee;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.injectors.GeyserHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCommandHandler extends Command {
    public BungeeCommandHandler(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            ExtrasPlayer bedrockPlayer = ExtrasPlayer.get(player.getUniqueId());
            if (bedrockPlayer != null) {
                // Run as bedrock player
                CommandExecutor.runForBedrock(bedrockPlayer, () -> CommandExecutor.runFromCommandInput(bedrockPlayer, this.getName(), args));
            }
            else {
                // Run as java player
                CommandExecutor.runFromCommandInput(player.getUniqueId(), this.getName(), args);
            }
        }
    }
}
