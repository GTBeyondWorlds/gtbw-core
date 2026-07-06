package com.gtbeyondworlds.gtbwcore.api.multiblock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Pins the local-frame → world-frame mapping for all four controller facings.
 *
 * <p>Conventions under test: {@code back} extends into the structure (away from
 * the controller's visible face), {@code right} is the structure's right when a
 * player stands in front of the machine looking at the controller — i.e. the
 * player's LEFT. Minecraft world axes: +x = east, +z = south.
 */
class PatternFacingTest {

    private static final RelativePos P = new RelativePos(1, 2, 3);

    @Test
    void northFacingMapsBackToSouthAndRightToEast() {
        // Controller face visible from the north; structure extends south.
        assertEquals(1, PatternFacing.NORTH.offsetX(P));
        assertEquals(2, PatternFacing.NORTH.offsetY(P));
        assertEquals(3, PatternFacing.NORTH.offsetZ(P));
    }

    @Test
    void southFacingMapsBackToNorthAndRightToWest() {
        assertEquals(-1, PatternFacing.SOUTH.offsetX(P));
        assertEquals(2, PatternFacing.SOUTH.offsetY(P));
        assertEquals(-3, PatternFacing.SOUTH.offsetZ(P));
    }

    @Test
    void eastFacingMapsBackToWestAndRightToSouth() {
        assertEquals(-3, PatternFacing.EAST.offsetX(P));
        assertEquals(2, PatternFacing.EAST.offsetY(P));
        assertEquals(1, PatternFacing.EAST.offsetZ(P));
    }

    @Test
    void westFacingMapsBackToEastAndRightToNorth() {
        assertEquals(3, PatternFacing.WEST.offsetX(P));
        assertEquals(2, PatternFacing.WEST.offsetY(P));
        assertEquals(-1, PatternFacing.WEST.offsetZ(P));
    }

    @Test
    void controllerOriginMapsToZeroForEveryFacing() {
        RelativePos origin = new RelativePos(0, 0, 0);
        for (PatternFacing facing : PatternFacing.values()) {
            assertEquals(0, facing.offsetX(origin));
            assertEquals(0, facing.offsetY(origin));
            assertEquals(0, facing.offsetZ(origin));
        }
    }
}
