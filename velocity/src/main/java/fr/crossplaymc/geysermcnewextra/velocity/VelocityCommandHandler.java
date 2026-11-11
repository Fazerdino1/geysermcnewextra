package fr.crossplaymc.geysermcnewextra.velocity;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import fr.crossplaymc.geysermcnewextra.core.commands.GeyserMCNewExtraCommand;

public class VelocityCommandHandler implements RawCommand {
    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            String command = "/" + invocation.alias() + " " + invocation.arguments();

            ExtrasPlayer bedrockPlayer = ExtrasPlayer.get(player.getUniqueId());
            if (bedrockPlayer != null) {
                CommandExecutor.runForBedrock(bedrockPlayer, () -> CommandExecutor.runFromInput(bedrockPlayer, command));
            } else {
                CommandExecutor.runFromInput(player.getUniqueId(), command);
            }
        } else if (invocation.arguments().trim().equalsIgnoreCase("reload")
                && (invocation.alias().equalsIgnoreCase(CommandExecutor.MAIN_COMMAND) || invocation.alias().equalsIgnoreCase("ge"))) {
            // Proxy console
            GeyserMCNewExtraCommand.reloadFromConsole();
        }
    }
}
