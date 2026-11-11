package fr.crossplaymc.geysermcnewextra.core.commands;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;
import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.core.menus.settings.menus.SettingsMenu;
import fr.crossplaymc.geysermcnewextra.core.preferences.bindings.Action;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class GeyserMCNewExtraCommand implements BedrockCommand {
    public static final String RELOAD_PERMISSION = "geysermcnewextra.reload";

    @Override
    public void onExecute(ExtrasPlayer player, List<String> args, String label) {
        if (!args.isEmpty()) {
            String subcommand = args.get(0);
            if (subcommand.equalsIgnoreCase("reload")) {
                // Geyser asks the platform (Bukkit, Velocity, Bungee permissions) for its players
                if (!player.getSession().hasPermission(RELOAD_PERMISSION)) {
                    player.getSession().sendMessage(player.lang("gmne.command.reload.no_permission"));
                    return;
                }
                reload(message -> player.getSession().sendMessage(message), player.getSession().locale());
                return;
            }
            if (subcommand.equalsIgnoreCase("settings")) {
                player.sendForm(new SettingsMenu());
                return;
            }
        }
        Action.OPEN_GE_MENU.run(player);
    }

    @Override
    public void onJavaExecute(UUID playerUUID, List<String> args, String label) {
        if (!args.isEmpty() && args.get(0).equalsIgnoreCase("reload")) {
            if (!SERVER.hasPermission(playerUUID, RELOAD_PERMISSION)) {
                SERVER.sendRawMessage(playerUUID, Lang.console("gmne.command.reload.no_permission"));
                return;
            }
            reload(message -> SERVER.sendRawMessage(playerUUID, message), null);
        }
    }

    /**
     * {@code /ge reload} from the server console.
     */
    public static void reloadFromConsole() {
        reload(SERVER::log, null);
    }

    private static void reload(Consumer<String> reply, String locale) {
        boolean loaded;
        try {
            loaded = GE.reload();
        } catch (RuntimeException e) {
            reply.accept(Lang.get("gmne.command.reload.failed", locale, e));
            return;
        }
        reply.accept(Lang.get(loaded ? "gmne.command.reload.done" : "gmne.command.reload.kept_config", locale));
    }
}
