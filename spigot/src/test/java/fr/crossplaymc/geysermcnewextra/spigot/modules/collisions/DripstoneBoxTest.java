package fr.crossplaymc.geysermcnewextra.spigot.modules.collisions;

import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DripstoneBoxTest {
    @Test
    void boxFollowsThicknessAndDirection() {
        BoundingBox tipDown = CollisionModule.dripstoneBox("minecraft:pointed_dripstone[thickness=tip,vertical_direction=down,waterlogged=false]");
        assertEquals(5 / 16.0, tipDown.getMinY(), 1e-9);
        BoundingBox tipUp = CollisionModule.dripstoneBox("minecraft:pointed_dripstone[thickness=tip,vertical_direction=up,waterlogged=false]");
        assertEquals(11 / 16.0, tipUp.getMaxY(), 1e-9);
        BoundingBox merge = CollisionModule.dripstoneBox("minecraft:pointed_dripstone[thickness=tip_merge,vertical_direction=up,waterlogged=false]");
        assertEquals(1.0, merge.getMaxY(), 1e-9);
        BoundingBox base = CollisionModule.dripstoneBox("minecraft:pointed_dripstone[thickness=base,vertical_direction=up,waterlogged=false]");
        assertEquals(2 / 16.0, base.getMinX(), 1e-9);
    }
}
