package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class VeinSelectionTest {

    private static VeinType type(String name, int spawnWeight) {
        return VeinType.builder(name)
                .addOreWeight(1)
                .richness(0.2f, 0.5f)
                .yBand(0, 60)
                .radius(48, 80)
                .thickness(12, 24)
                .spawnWeight(spawnWeight)
                .build();
    }

    private static final List<VeinType> FOUR = List.of(
            type("cassiterite", 10), type("chalcopyrite", 10),
            type("magnetite", 10), type("coal_seam", 12));

    @Test
    void regionMathUsesFloorDivision() {
        assertEquals(0, VeinMath.regionFromBlock(0));
        assertEquals(0, VeinMath.regionFromBlock(383));
        assertEquals(1, VeinMath.regionFromBlock(384));
        assertEquals(-1, VeinMath.regionFromBlock(-1));
        assertEquals(-1, VeinMath.regionFromBlock(-384));
        assertEquals(-2, VeinMath.regionFromBlock(-385));
        assertEquals(0, VeinMath.regionFromChunk(23));
        assertEquals(1, VeinMath.regionFromChunk(24));
        assertEquals(-1, VeinMath.regionFromChunk(-1));
        // Block and chunk views of the same position agree.
        assertEquals(VeinMath.regionFromBlock(400), VeinMath.regionFromChunk(400 >> 4));
    }

    @Test
    void selectionIsDeterministic() {
        for (long seed : new long[] {0L, 1L, -1L, 123456789L, Long.MIN_VALUE}) {
            for (int r = -5; r <= 5; r++) {
                assertEquals(VeinMath.selectTypeIndex(seed, r, -r, FOUR),
                        VeinMath.selectTypeIndex(seed, r, -r, FOUR));
            }
        }
    }

    @Test
    void selectionIsIndependentOfListOrder() {
        List<VeinType> reversed = new ArrayList<>(FOUR);
        java.util.Collections.reverse(reversed);
        for (int rx = -20; rx <= 20; rx++) {
            for (int rz = -20; rz <= 20; rz++) {
                int a = VeinMath.selectTypeIndex(42L, rx, rz, FOUR);
                int b = VeinMath.selectTypeIndex(42L, rx, rz, reversed);
                assertEquals(FOUR.get(a).name(), reversed.get(b).name(),
                        "region " + rx + "," + rz + " must pick the same vein regardless of order");
            }
        }
    }

    @Test
    void emptyListSelectsNothing() {
        assertEquals(-1, VeinMath.selectTypeIndex(42L, 0, 0, List.of()));
    }

    @Test
    void selectionRespectsSpawnWeightsRoughly() {
        // coal_seam has weight 12 of 42 total = ~28.6%. Over 10k regions expect
        // a generous window; this guards against grossly broken weighting.
        int coal = 0;
        int total = 0;
        for (int rx = 0; rx < 100; rx++) {
            for (int rz = 0; rz < 100; rz++) {
                int picked = VeinMath.selectTypeIndex(7L, rx, rz, FOUR);
                if (FOUR.get(picked).name().equals("coal_seam")) {
                    coal++;
                }
                total++;
            }
        }
        double fraction = coal / (double) total;
        assertTrue(fraction > 0.24 && fraction < 0.34,
                "coal_seam fraction should be near 12/42=0.286, got " + fraction);
    }

    @Test
    void addingATypeOnlyReassignsRegionsItWins() {
        List<VeinType> withNew = new ArrayList<>(FOUR);
        VeinType newcomer = type("scheelite", 10);
        withNew.add(newcomer);
        int changed = 0;
        int wonByNew = 0;
        int total = 0;
        for (int rx = -50; rx < 50; rx++) {
            for (int rz = -50; rz < 50; rz++) {
                VeinType before = FOUR.get(VeinMath.selectTypeIndex(99L, rx, rz, FOUR));
                VeinType after = withNew.get(VeinMath.selectTypeIndex(99L, rx, rz, withNew));
                total++;
                if (after == newcomer) {
                    wonByNew++;
                }
                if (!before.name().equals(after.name()) && after != newcomer) {
                    changed++; // a region changed to something other than the new type: forbidden
                }
            }
        }
        assertEquals(0, changed, "existing regions may only change by being won by the new type");
        double fraction = wonByNew / (double) total;
        // newcomer weight 10 of 52 total = ~19.2%
        assertTrue(fraction > 0.15 && fraction < 0.24,
                "new type should win roughly its fair share, got " + fraction);
    }

    @Test
    void differentSeedsGiveDifferentLayouts() {
        int same = 0;
        int total = 400;
        for (int rx = 0; rx < 20; rx++) {
            for (int rz = 0; rz < 20; rz++) {
                if (VeinMath.selectTypeIndex(1L, rx, rz, FOUR) == VeinMath.selectTypeIndex(2L, rx, rz, FOUR)) {
                    same++;
                }
            }
        }
        assertTrue(same < total / 2, "two seeds must not produce near-identical layouts");
    }
}
