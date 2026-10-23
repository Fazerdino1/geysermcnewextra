package fr.crossplaymc.geysermcnewextra.core.commands;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class CommandExecutor {
    public static Map<String, BedrockCommand> ids;

    public static void loadCommands() {
        ids = new HashMap<>();
        add(List.of("ge", "geysermcnewextra"), new GeyserMCNewExtraCommand());
        add(List.of("emotechat", "muteemotechat", "unmuteemotechat"), new EmoteChatCommand());
        // "playerlist" is the Spigot command name (plugin.yml) and was missing, so /playerlist did nothing
        add(List.of("playerlist", "l", "players", "listplayers", "tab", "tablist"), new TabListCommand());
        for (String alias : GE.getConfig().getDisabledCommandAliases()) {
            if (!alias.equalsIgnoreCase(MAIN_COMMAND)) {
                ids.remove(alias.toLowerCase(Locale.ROOT));
            }
        }
    }

    public static final String MAIN_COMMAND = "geysermcnewextra";


    /**
     * Runs a command of a Bedrock player after {@code bedrock-command-delay-ms}, on the player's
     * Geyser event loop: forms sent while the client's chat screen is still open are discarded.
     * Based on paper0319/GeyserMCNewExtra@35d1877 (Velocity only there).
     */
    public static void runForBedrock(ExtrasPlayer player, Runnable command) {
        int delay = GE.getConfig().getBedrockCommandDelayMs();
        if (delay <= 0) {
            command.run();
            return;
        }
        player.getSession().scheduleInEventLoop(command, delay, TimeUnit.MILLISECONDS);
    }

    public static void add(List<String> aliases, BedrockCommand cmd) {
        for (String alias : aliases) {
            ids.put(alias, cmd);
        }
    }

    public static boolean isExtrasCommand(String input) {
        ArrayList<String> args = new ArrayList<>(List.of(input.substring(1).split(" ")));
        String root = args.get(0);
        return ids.containsKey(root);
    }

    public static void runFromInput(ExtrasPlayer player, String input) {
        ArrayList<String> args = new ArrayList<>(List.of(input.substring(1).split(" ")));
        String root = args.get(0);
        if (ids.containsKey(root)) {
            args.remove(0);
            ids.get(root).onExecute(player, args, root);
        }
    }

    public static void runFromCommandInput(ExtrasPlayer player, String label, String[] args) {
        if (ids.containsKey(label)) {
            ids.get(label).onExecute(player, Arrays.asList(args), label);
        }
    }

    public static void runFromInput(UUID player, String input) {
        ArrayList<String> args = new ArrayList<>(List.of(input.substring(1).split(" ")));
        String root = args.get(0);
        if (ids.containsKey(root)) {
            args.remove(0);
            ids.get(root).onJavaExecute(player, args, root);
        }
    }

    public static void runFromCommandInput(UUID player, String label, String[] args) {
        if (ids.containsKey(label)) {
            ids.get(label).onJavaExecute(player, Arrays.asList(args), label);
        }
    }

}
