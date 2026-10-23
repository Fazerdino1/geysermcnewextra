package fr.crossplaymc.geysermcnewextra.spigot.modules.maps;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapMathTest {
    @Test
    void coordinatesArePinnedToTheEdge() {
        assertEquals(0, MapMath.clampCoordinate(0));
        assertEquals(20, MapMath.clampCoordinate(10));
        assertEquals(127, MapMath.clampCoordinate(500));
        assertEquals(-128, MapMath.clampCoordinate(-500));
    }

    @Test
    void directionFollowsTheYaw() {
        assertEquals(0, MapMath.direction(0, false, 0));
        assertEquals(4, MapMath.direction(90, false, 0));
        assertEquals(8, MapMath.direction(180, false, 0));
        assertEquals(12, MapMath.direction(-90, false, 0));
        assertEquals(0, MapMath.direction(-5, false, 0));
        assertEquals(0, MapMath.direction(359, false, 0));
    }
}
