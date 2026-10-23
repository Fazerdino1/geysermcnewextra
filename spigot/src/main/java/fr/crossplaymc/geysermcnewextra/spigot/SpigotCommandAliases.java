package fr.crossplaymc.geysermcnewextra.spigot;

import fr.crossplaymc.geysermcnewextra.core.commands.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

/**
 * Removes the aliases listed in disabled-command-aliases from the server's command map, so another
 * plugin can own them (upstream #35). plugin.yml aliases cannot be changed after loading, and the
 * command map is not public in the Spigot API we build against, hence the reflection.
 */
public final class SpigotCommandAliases {
    private SpigotCommandAliases() {
    }

    public static void disable(Plugin plugin, List<String> aliases) {
        if (aliases.isEmpty()) {
            return;
        }
        try {
            Method getCommandMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) getCommandMap.invoke(Bukkit.getServer());
            Field knownField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> known = (Map<String, Command>) knownField.get(commandMap);

            for (String raw : aliases) {
                String alias = raw.toLowerCase(Locale.ROOT);
                if (alias.equals(CommandExecutor.MAIN_COMMAND)) {
                    continue;
                }
                Command command = known.get(alias);
                if (command instanceof PluginCommand pluginCommand && pluginCommand.getPlugin() == plugin) {
                    known.remove(alias);
                    // Hand the alias to another plugin that registered it too (e.g. otherplugin:l)
                    for (Map.Entry<String, Command> entry : known.entrySet()) {
                        if (entry.getKey().endsWith(":" + alias) && !(entry.getValue() instanceof PluginCommand other && other.getPlugin() == plugin)) {
                            known.put(alias, entry.getValue());
                            break;
                        }
                    }
                    SERVER.log("Command alias /" + alias + " disabled.");
                }
            }
            // Let clients see the new command tree (CraftServer#syncCommands, when available)
            try {
                Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
            } catch (NoSuchMethodException ignored) {
                // Older servers resend the tree on their own
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            SERVER.warn("Could not disable command aliases " + aliases + ": " + e);
        }
    }
}
