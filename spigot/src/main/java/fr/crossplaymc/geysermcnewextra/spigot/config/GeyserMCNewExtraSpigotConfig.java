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

}
