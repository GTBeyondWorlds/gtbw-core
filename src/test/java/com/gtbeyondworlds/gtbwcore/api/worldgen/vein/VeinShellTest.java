package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class VeinShellTest {

    private static final VeinType TYPE = VeinType.builder("cassiterite")
            .addOreWeight(80)
            .addOreWeight(20)
            .richness(0.20f, 0.50f)
            .yBand(20, 70)
            .radius(48, 80)
            .thickness(12, 24)
            .spawnWeight(10)
            .build();

    @Test
    void shellIsNonEmptyAndCapped() {
        VeinInstance vein = VeinMath.instance(42L, 0, 0, TYPE);
        List<int[]> shell = VeinMath.boundaryShell(vein, 600);
        assertFalse(shell.isEmpty(), "a real vein must have a boundary shell");
        assertTrue(shell.size() <= 600, "cap must be respected, got " + shell.size());
    }

    @Test
    void shellCellsAreInsideNearTheBoundaryAndWithinReach() {
        VeinInstance vein = VeinMath.instance(7L, 2, -3, TYPE);
        int reach = VeinMath.maxReachXZ(vein);
        int vReach = VeinMath.maxReachY(vein);
        for (int[] cell : VeinMath.boundaryShell(vein, 600)) {
            double d = VeinMath.normalizedDistance(vein, cell[0], cell[1], cell[2]);
            assertTrue(d < 1.0, "shell cells must be inside the lens, got d=" + d);
            assertTrue(d > 0.2, "shell cells must not be deep in the core, got d=" + d);
            assertTrue(Math.abs(cell[0] - vein.centerX()) <= reach, "x within reach");
            assertTrue(Math.abs(cell[1] - vein.centerY()) <= vReach, "y within reach");
            assertTrue(Math.abs(cell[2] - vein.centerZ()) <= reach, "z within reach");
        }
    }

    @Test
    void shellIsDeterministic() {
        VeinInstance vein = VeinMath.instance(99L, -4, 5, TYPE);
        List<int[]> a = VeinMath.boundaryShell(vein, 600);
        List<int[]> b = VeinMath.boundaryShell(vein, 600);
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i)[0], b.get(i)[0]);
            assertEquals(a.get(i)[1], b.get(i)[1]);
            assertEquals(a.get(i)[2], b.get(i)[2]);
        }
    }

    @Test
    void tinyCapStillSpreadsAcrossTheShell() {
        VeinInstance vein = VeinMath.instance(42L, 0, 0, TYPE);
        List<int[]> shell = VeinMath.boundaryShell(vein, 50);
        assertTrue(shell.size() <= 50, "cap must be respected");
        assertTrue(shell.size() >= 25, "thinning should approach the cap, got " + shell.size());
        // Thinned shell must still span the vein horizontally, not cluster on one side.
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (int[] cell : shell) {
            minX = Math.min(minX, cell[0]);
            maxX = Math.max(maxX, cell[0]);
        }
        assertTrue(maxX - minX > vein.radius(), "shell samples must span the lens");
    }
}
