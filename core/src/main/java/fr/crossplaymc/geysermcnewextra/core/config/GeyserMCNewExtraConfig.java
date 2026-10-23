package fr.crossplaymc.geysermcnewextra.core.config;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@Getter

// TODO make wiki page/s to explain stuff better
public final class GeyserMCNewExtraConfig {
    @Comment("""
            Enable the GeyserMCNewExtra menu, which can be opened by players by
            double-tapping inventory, or typing `/ge`.
            \s
            The GeyserMCNewExtra Menu provides actions and configurable settings such as
            - Remapping certain actions (e.g emotes/pick block) to Java Edition only actions (e.g offhand)
            - Configurable HUD Visibility
            - Quick access to form remakes of Java Edition Menus
              (e.g Player List, Advancements, Statistics).
            \s
            You can configure default options for Bedrock Players at
            `preferences/default.json`
            and for Java Players at
            `preferences/java/default.json`.
            \s
            Setting this to `false` also disables saving of preferences.
            """)
    private boolean enableMenu = true;

    @Comment("Enable the Java Edition combat sounds.")
    private boolean enableJavaCombatSounds = true;

    @Comment("""
            Enable the Block Display entity workaround using FMBE. (EXPERIMENTAL)
            Note that the translation is not 100% accurate and some block types might be invisible (candles, doors, campfires),
            where as some blocks will render differently (fence, glass panes, some storage containers).
            Block Display billboards do not work and X/Z scale are linked to which ever one is highest,
            meaning that this should only be enabled for very simple usages of Block Displays.
            (https://wiki.bedrock.dev/commands/display-entities.html)
            """)
    private boolean enableBlockDisplayWorkaround = false;


    @Comment("""
            Requires PacketEvents and Java Edition clients on 1.21.9 or above.
            https://modrinth.com/plugin/packetevents

            Enables the cape workaround. (EXPERIMENTAL)
            
            This shows Bedrock Edition player capes on Java Edition clients,
            by using fake mannequin player entities.
            It copies the packets of the player, so the position is identical.
            The hitbox of the mannequin is identical to the player and
            whenever the mannequin is attacked by the player it is translated as an attack
            to the bedrock player.
            
            This may break anti-cheats or plugins, and is not perfect.
            
            The Bedrock Edition player's cape will sometimes slightly clip
            inside the Bedrock Player when wearing armor and if the player attacks.
            
            The cape is visually removed when entering mounts that can support more than one
            player (including chest boats).
            
            This only works with capes that are available on Java Edition, that are also on
            Bedrock Edition.
            """)
    private boolean enableBedrockCapesOnJavaWorkaround = true;

    @Comment("When the server closes or Geyser reloads, should GeyserMCNewExtra automatically reconnect players to the server.")
    private boolean autoReconnect = true;

    @Comment(
            """
            The language that Java Edition players read when using a GeyserMCNewExtra command (e.g, `/muteemotechat`).
            This is automatically set on Bedrock Edition from the player's settings.
            The available locale codes are listed in `cache/langs/language_names.json`.
            """)
    private String defaultLocale = "en_US";

    @Comment("""
            Enables downloading of Bedrock Player skins.
            This will download the Geometry, Animations, and Textures of your players skins/capes.
            Persona skins are currently bugged.
            """)
    private boolean enableSkinSaving = false;


    @Comment("""
            Whether to disable the Paper Doll or not.
            The Paper Doll is the small player graphic in the top left of bedrock players screens
            This can give players a competitive advantage over java in some cases, for example,
            you can see what armor you have on without pressing the inventory key
            you can see what armor has broken during combat
            """)
    private boolean disablePaperDoll = false;

    @Comment("""
            Whether to check for updates or not: new GeyserMCNewExtra releases on GitHub
            (https://github.com/Fazerdino1/geysermcnewextra/releases, logged at startup).
            This also disables updating of the extras resource pack,
            however you can manually update them by deleting the 'GeyserMCNewExtra/cache/' folder.
            """)
    private boolean checkForUpdates = true;

    @Comment("""
            The priority level for the extras resource pack resource pack.
            Higher values = higher priority (packs with higher priority override files from lower-priority packs
            when file names conflict, and the client cannot easily merge them).
            Valid range: -100 to 100
            Common values:
            - -100: Lowest priority
            - 0: Normal priority
            - 100: Highest priority (default)

            Only change this if you have other resource packs that need to override extras resource pack,
            or if extras resource pack needs lower priority than your custom packs.

            Note: If you serve extras resource pack externally via CDN (using UrlPackCodec),
            GeyserMCNewExtra will automatically detect it by UUID and skip registering its own copy.
            """)
    private int resourcePackPriority = 100;

    @Comment("""
            Delay in milliseconds before running a GeyserMCNewExtra command typed by a Bedrock player.
            The Bedrock client discards forms that arrive while its chat screen is still open,
            so without a delay commands like `/ge` can show nothing. 0 runs commands immediately.
            """)
    private int bedrockCommandDelayMs = 500;

    @Comment("""
            Command aliases to turn off, e.g. [l, tab] if another plugin uses /l for local chat.
            /geysermcnewextra itself cannot be turned off. Available aliases:
            ge, emotechat, muteemotechat, unmuteemotechat, playerlist, l, players, listplayers, tab, tablist
            """)
    private List<String> disabledCommandAliases = new ArrayList<>();

    @Comment("Only enable if you know what you are doing.")
    private boolean debugMode = false;

    @Comment("The version of the config. DO NOT CHANGE!")
    private int version = ConfigLoader.LATEST_VERSION;
}
