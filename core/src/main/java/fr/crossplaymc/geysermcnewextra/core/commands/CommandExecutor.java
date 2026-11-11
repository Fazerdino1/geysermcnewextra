package fr.crossplaymc.geysermcnewextra.core.commands;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;

public class CommandExecutor {
    public static Map<String, BedrockCommand> ids;

    public static void loadCommands() {
        // Built aside then swapped: commands keep working while /ge reload runs
        Map<String, BedrockCommand> commands = new HashMap<>();
        add(commands, List.of("ge", "geysermcnewextra"), new GeyserMCNewExtraCommand());
        add(commands, List.of("emotechat", "muteemotechat", "unmuteemotechat"), new EmoteChatCommand());
        // "playerlist" is the Spigot command name (plugin.yml) and was missing, so /playerlist did nothing
        add(commands, List.of("playerlist", "l", "players", "listplayers", "tab", "tablist"), new TabListCommand());
        for (String alias : GE.getConfig().getDisabledCommandAliases()) {
            if (!alias.equalsIgnoreCase(MAIN_COMMAND)) {
                commands.remove(alias.toLowerCase(Locale.ROOT));
            }
        }
        ids = commands;
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
        add(ids, aliases, cmd);
    }

    private static void add(Map<String, BedrockCommand> commands, List<String> aliases, BedrockCommand cmd) {
        for (String alias : aliases) {
            commands.put(alias.toLowerCase(Locale.ROOT), cmd);
        }
    }

    /**
     * @return the command for this root ("/GE", "/geysermcnewextra:ge"...), or null
     */
    private static BedrockCommand find(String root) {
        if (root == null) {
            return null;
        }
        String name = root.toLowerCase(Locale.ROOT);
        int namespace = name.indexOf(':');
        if (namespace >= 0) {
            name = name.substring(namespace + 1);
        }
        Map<String, BedrockCommand> commands = ids;
        return commands == null ? null : commands.get(name);
    }

    /**
     * Splits "/root a b" into its words, ignoring repeated spaces. Empty when there is no command.
     */
    private static List<String> split(String input) {
        if (input == null) {
            return new ArrayList<>();
        }
        String command = input.strip();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        command = command.strip();
        if (command.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(command.split("\\s+")));
    }

    public static boolean isExtrasCommand(String input) {
        List<String> args = split(input);
        return !args.isEmpty() && find(args.get(0)) != null;
    }

    public static void runFromInput(ExtrasPlayer player, String input) {
        List<String> args = split(input);
        if (args.isEmpty()) {
            return;
        }
        String root = args.remove(0);
        BedrockCommand command = find(root);
        if (command != null) {
            command.onExecute(player, args, root);
        }
    }

    public static void runFromCommandInput(ExtrasPlayer player, String label, String[] args) {
        BedrockCommand command = find(label);
        if (command != null) {
            command.onExecute(player, Arrays.asList(args), label);
        }
    }

    public static void runFromInput(UUID player, String input) {
        List<String> args = split(input);
        if (args.isEmpty()) {
            return;
        }
        String root = args.remove(0);
        BedrockCommand command = find(root);
        if (command != null) {
            command.onJavaExecute(player, args, root);
        }
    }

    public static void runFromCommandInput(UUID player, String label, String[] args) {
        BedrockCommand command = find(label);
        if (command != null) {
            command.onJavaExecute(player, Arrays.asList(args), label);
        }
    }

}
