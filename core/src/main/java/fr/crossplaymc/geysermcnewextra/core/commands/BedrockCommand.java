package fr.crossplaymc.geysermcnewextra.core.commands;
import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;

import java.util.List;
import java.util.UUID;

public interface BedrockCommand {
    void onExecute(ExtrasPlayer player, List<String> args, String label);

    default void onJavaExecute(UUID playerUUID, List<String> args, String label){};
}
