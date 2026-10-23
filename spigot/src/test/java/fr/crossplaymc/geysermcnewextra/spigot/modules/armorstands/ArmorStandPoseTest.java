package fr.crossplaymc.geysermcnewextra.spigot.modules.armorstands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorStandPoseTest {
    @Test
    void cyclesThroughThirteenBedrockPoses() {
        assertEquals(13, ArmorStandPose.values().length);
        assertEquals(ArmorStandPose.NONE, ArmorStandPose.after(ArmorStandPose.DEFAULT.ordinal()));
        assertEquals(ArmorStandPose.DEFAULT, ArmorStandPose.after(ArmorStandPose.HERO.ordinal()));
    }

    @Test
    void unknownStoredPoseStartsOver() {
        assertEquals(ArmorStandPose.DEFAULT, ArmorStandPose.after(42));
        assertEquals(ArmorStandPose.DEFAULT, ArmorStandPose.after(-1));
    }
}
