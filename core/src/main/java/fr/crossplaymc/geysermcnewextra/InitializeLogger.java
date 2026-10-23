package fr.crossplaymc.geysermcnewextra;

import fr.crossplaymc.geysermcnewextra.core.locale.Lang;
import fr.crossplaymc.geysermcnewextra.core.utils.IsAvailable;
import fr.crossplaymc.geysermcnewextra.core.version.PluginVersion;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.GE;
import static fr.crossplaymc.geysermcnewextra.core.GeyserMCNewExtra.SERVER;

public class InitializeLogger {

    private static Instant start;

    public static void start() {
        start = Instant.now();
        info("-----------" + PluginVersion.DISPLAY_NAME + "-----------");
        info(Lang.console("gmne.console.version", PluginVersion.GE_VERSION));
        info(Lang.console("gmne.console.server_type", ServerType.get()));
        info(Lang.console("gmne.console.platform_type", ServerType.platform().platformName()));
        if (ServerType.type != ServerType.BUNGEECORD && ServerType.type != ServerType.EXTENSION) {
            info(Lang.console("gmne.console.floodgate_installed", (IsAvailable.floodgate() ? Lang.console("gmne.console.yes") : Lang.console("gmne.console.no"))));
        }
        if (ServerType.type != ServerType.EXTENSION) {
            info(Lang.console("gmne.console.packetevents_installed", (IsAvailable.packetevents() ? Lang.console("gmne.console.yes") : Lang.console("gmne.console.no"))));
            // Only when it is missing (this warning used to show when PacketEvents was installed, upstream #55)
            if (!IsAvailable.packetevents()) {
                warn(Lang.console("gmne.console.packetevents_missing"));
            }
        }

    }

    public static void end() {
        if (GE.getConfig().isEnableBlockDisplayWorkaround() && !GE.isBlockDisplayRegistered()) {
            // Happens when GeyserMCNewExtra starts after Geyser defined its entities (e.g. as an extension)
            warn(Lang.console("gmne.console.block_display_unregistered"));
        }
        DecimalFormat r3 = new DecimalFormat("0.000");
        Instant finish = Instant.now();
        info(Lang.console("gmne.console.done", r3.format(Duration.between(start, finish).toMillis() / 1000.0d)));
        if (ServerType.type != ServerType.STANDALONE)
            info("----------------------------------------");
        else
            info("-----------------------------------------------");

        SERVER.onGeyserMCNewExtraInitialize();
    }

    public static void endNoDone() {
        info("----------------------------------------");
    }

    public static void logTask(String start, Runnable task, String end) {
        info(start);
        task.run();
        info(end);
    }

    public static void logTask(Runnable task, String onComplete) {
        task.run();
        info(onComplete);
    }

    private static void info(String s) {
        SERVER.log(s);
    }

    private static void warn(String s) {
        SERVER.warn(s);
    }
}
