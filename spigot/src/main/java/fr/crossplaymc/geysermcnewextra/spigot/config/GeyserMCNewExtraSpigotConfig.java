package fr.crossplaymc.geysermcnewextra.spigot.config;

import lombok.Getter;
import org.bukkit.World;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@Getter

// TODO make wiki page/s to explain stuff better
public final class GeyserMCNewExtraSpigotConfig {
    @Comment("""
            Forces block placements to not be hold-bridgable/scaffold-bridgeable.
            Requires 'disable-bedrock-scaffolding: true' in Geyser's config for forward block placements.
            """)
    private boolean enableJavaOnlyBlockPlacement = false;

    @Comment("Updates the blocks at the player's line of sight every tick to remove ghost blocks.")
    private boolean enableBlockGhostingWorkaround = false;

   /*
    @Comment("""
            Enables the Knockback Attack Sprint Workaround.
            In Java Edition, when you execute a Knockback Attack, the player stops sprinting for one tick.
            GeyserMCNewExtra works around this by tricking the Bedrock Edition client into having no hunger
            for one game tick, however this temporarily makes the hunger bar appear empty.
            """)
    private boolean enableKnockbackAttackSprintWorkaround = false;
    */

    /*
 @Comment("""
         Plays the Java Edition crop break and place sound for Bedrock Edition Players.
         """)
 private boolean enableCropSoundWorkaround = true;
*/

    @Comment("""
            Bedrock Edition armor stands, for every player: placed armor stands get arms, and
            sneaking + using an armor stand cycles through Bedrock Edition's poses
            (permission geysermcnewextra.armorstands.pose, given to everyone by default).
            Replaces the "Bedrock Armorstands" plugin: do not run both.
            """)
    private ArmorStands armorStands = new ArmorStands();

    @Comment("""
            Coordinates on maps, like Legacy Console Edition (based on MapXYZ by LetsGoAway, MIT).
            Replaces the "MapXYZ" plugin: do not run both.
            """)
    private Maps maps = new Maps();

    @Comment("""
            Removes the server-side collision of bamboo and pointed dripstone, so Bedrock players stop being
            pulled back when they walk into them (based on Hurricane by GeyserMC, MIT).
            WARNING for PvP servers: a modified client, Java or Bedrock, can then walk through these blocks.
            This changes the blocks for the whole server: it cannot be limited to some worlds, and it only
            applies (or stops) after a restart. Paper and its forks only. Skipped if Hurricane is installed.
            """)
    private Collisions collisions = new Collisions();

    @Comment("The version of the config. DO NOT CHANGE!")
    private int version = SpigotConfigLoader.LATEST_VERSION;

    /**
     * Base of the modules that can be turned on for some worlds only.
     */
    @Getter
    @ConfigSerializable
    public abstract static class WorldModule {
        @Comment("Turns the module on.")
        protected boolean enabled = false;

        @Comment("Worlds where the module works (folder names, e.g. world_nether). Empty: every world.")
        protected List<String> worlds = new ArrayList<>();

        public boolean isActiveIn(World world) {
            return enabled && world != null && WorldScope.contains(worlds, world.getName());
        }
    }

    @Getter
    @ConfigSerializable
    public static final class ArmorStands extends WorldModule {
        @Comment("Give arms to armor stands placed by players.")
        private boolean arms = true;

        @Comment("Sneak + use an armor stand to change its pose. Invisible armor stands (holograms...) are left alone.")
        private boolean poseCycling = true;
    }

    @Getter
    @ConfigSerializable
    public static final class Maps extends WorldModule {
        @Comment("Show the X, Y and Z coordinates at the top of the map the player is holding.")
        private boolean coordinates = true;

        @Comment("Show the eye level position instead of the feet position.")
        private boolean eyeLevelPosition = false;

        @Comment("Give an empty map to players joining the server for the first time.")
        private boolean startingMap = false;

        @Comment("New maps start zoomed out at 1:8 (level 3/4), like Legacy Console Edition.")
        private boolean legacyConsoleZoom = false;

        @Comment("""
                Locator maps: other players of the world appear on maps, with their name.
                PvP: this reveals where every player is. Keep it off on PvP worlds.
                """)
        private boolean locatorMaps = false;

        @Comment("""
                Set the reducedDebugInfo game rule in these worlds, which hides the coordinates of the F3 screen.
                PvP: this only affects Java players. Worlds changed by this option get their game rule back
                when the option is turned off.
                """)
        private boolean reducedDebugInfo = false;
    }

    @Getter
    @ConfigSerializable
    public static final class Collisions {
        @Comment("Turns the module on.")
        private boolean enabled = false;

        @Comment("No collision for bamboo.")
        private boolean bamboo = true;

        @Comment("No collision for pointed dripstone. It can then no longer be targeted easily either.")
        private boolean pointedDripstone = true;
    }
}
