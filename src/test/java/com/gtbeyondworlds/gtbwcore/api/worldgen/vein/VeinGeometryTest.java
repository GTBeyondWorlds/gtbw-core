package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VeinGeometryTest {

    private static final VeinType TYPE = VeinType.builder("cassiterite")
            .addOreWeight(80)
            .addOreWeight(20)
            .richness(0.20f, 0.50f)
            .yBand(20, 70)
            .radius(48, 80)
            .thickness(12, 24)
            .spawnWeight(10)
            .build();

    // Tightest legal configuration: radius pinned to VeinType.MAX_CONFIG_RADIUS,
    // giving the maximum possible wobbled reach (96 blocks) and the thinnest
    // (1-block) margin against the adjacent-region center confinement.
    private static final VeinType MAX_REACH_TYPE = VeinType.builder("max_reach")
            .addOreWeight(1)
            .richness(0.99f, 1.0f)
            .yBand(-60, 60)
            .radius(84, 84)
            .thickness(24, 24)
            .spawnWeight(1)
            .build();

    @Test
    void instanceIsDeterministic() {
        VeinInstance a = VeinMath.instance(42L, 3, -7, TYPE);
        VeinInstance b = VeinMath.instance(42L, 3, -7, TYPE);
        assertEquals(a, b);
    }

    @Test
    void instanceStaysWithinConfiguredRanges() {
        for (long seed : new long[] {0L, 42L, -99L, 0xDEADBEEFL}) {
            for (int rx = -10; rx <= 10; rx += 2) {
                for (int rz = -10; rz <= 10; rz += 2) {
                    VeinInstance vein = VeinMath.instance(seed, rx, rz, TYPE);
                    int minBlockX = rx * VeinMath.REGION_BLOCKS;
                    int minBlockZ = rz * VeinMath.REGION_BLOCKS;
                    // Center in the central half of the region: offsets [96, 287].
                    assertTrue(vein.centerX() >= minBlockX + 96 && vein.centerX() <= minBlockX + 287,
                            "centerX offset " + (vein.centerX() - minBlockX));
                    assertTrue(vein.centerZ() >= minBlockZ + 96 && vein.centerZ() <= minBlockZ + 287,
                            "centerZ offset " + (vein.centerZ() - minBlockZ));
                    assertTrue(vein.radius() >= 48 && vein.radius() <= 80, "radius " + vein.radius());
                    assertTrue(vein.thickness() >= 12 && vein.thickness() <= 24,
                            "thickness " + vein.thickness());
                    assertTrue(vein.richness() >= 0.20f && vein.richness() <= 0.50f,
                            "richness " + vein.richness());
                    // Vertical: wobbled lens must fit the band.
                    int vReach = VeinMath.maxReachY(vein);
                    assertTrue(vein.centerY() - vReach >= 20 && vein.centerY() + vReach <= 70,
                            "centerY " + vein.centerY() + " reach " + vReach);
                }
            }
        }
    }

    @Test
    void adjacentRegionVeinsNeverOverlap() {
        assertAdjacentRegionVeinsNeverOverlap(TYPE);
    }

    @Test
    void adjacentRegionVeinsNeverOverlapAtMaxRadius() {
        // Same guarantee, but at the tightest legal margin: radius pinned to
        // MAX_CONFIG_RADIUS gives the largest possible wobbled reach (96
        // blocks), leaving only a 1-block margin against the 193-block
        // minimum center separation. An off-by-one in the center-confinement
        // or ceil math would slip through the looser TYPE fixture but not
        // this one.
        assertAdjacentRegionVeinsNeverOverlap(MAX_REACH_TYPE);
    }

    private void assertAdjacentRegionVeinsNeverOverlap(VeinType type) {
        // The load-bearing guarantee. Sample many seeds; check the wobbled
        // reach boxes of veins in horizontally/vertically/diagonally adjacent
        // regions are strictly disjoint.
        for (long seed = 0; seed < 300; seed++) {
            for (int rx = -3; rx <= 3; rx++) {
                for (int rz = -3; rz <= 3; rz++) {
                    VeinInstance a = VeinMath.instance(seed, rx, rz, type);
                    int ra = VeinMath.maxReachXZ(a);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dz == 0) {
                                continue;
                            }
                            VeinInstance b = VeinMath.instance(seed, rx + dx, rz + dz, type);
                            int rb = VeinMath.maxReachXZ(b);
                            boolean disjointX = a.centerX() + ra < b.centerX() - rb
                                    || b.centerX() + rb < a.centerX() - ra;
                            boolean disjointZ = a.centerZ() + ra < b.centerZ() - rb
                                    || b.centerZ() + rb < a.centerZ() - ra;
                            assertTrue(disjointX || disjointZ,
                                    "overlap at seed " + seed + " regions (" + rx + "," + rz
                                            + ")x(" + (rx + dx) + "," + (rz + dz) + ")");
                        }
                    }
                }
            }
        }
    }

    @Test
    void falloffAnchorsAndMonotonicity() {
        assertEquals(1.0, VeinMath.falloff(0.0), 1e-9);
        assertEquals(1.0, VeinMath.falloff(0.2), 1e-9);   // 1.25 * 0.8 = 1.0 exactly
        assertEquals(0.0, VeinMath.falloff(1.0), 1e-9);
        assertEquals(0.0, VeinMath.falloff(1.5), 1e-9);
        double prev = Double.MAX_VALUE;
        for (double d = 0; d <= 1.2; d += 0.01) {
            double f = VeinMath.falloff(d);
            assertTrue(f <= prev + 1e-12, "falloff must be non-increasing at d=" + d);
            prev = f;
        }
    }

    @Test
    void rollOreIsPositionalNotSequential() {
        VeinInstance vein = VeinMath.instance(42L, 0, 0, TYPE);
        // Same position always rolls the same result, in any call order.
        int first = VeinMath.rollOre(vein, vein.centerX() + 5, vein.centerY(), vein.centerZ() - 3);
        VeinMath.rollOre(vein, vein.centerX(), vein.centerY() + 1, vein.centerZ());
        int again = VeinMath.rollOre(vein, vein.centerX() + 5, vein.centerY(), vein.centerZ() - 3);
        assertEquals(first, again);
    }

    @Test
    void nothingPlacesOutsideMaxReach() {
        VeinInstance vein = VeinMath.instance(42L, 0, 0, TYPE);
        int reach = VeinMath.maxReachXZ(vein);
        int vReach = VeinMath.maxReachY(vein);
        for (int i = 0; i < 2000; i++) {
            // Ring of positions just past the reach on each axis.
            int x = vein.centerX() + (i % 2 == 0 ? reach + 1 + i % 7 : -(reach + 1 + i % 5));
            int z = vein.centerZ() + (i % 3 == 0 ? reach + 1 + i % 11 : 0);
            assertEquals(-1, VeinMath.rollOre(vein, x, vein.centerY(), z));
            assertEquals(-1, VeinMath.rollOre(vein, vein.centerX(),
                    vein.centerY() + vReach + 1 + i % 3, vein.centerZ()));
        }
    }

    @Test
    void coreDensityMatchesRichnessAndWeights() {
        VeinInstance vein = VeinMath.instance(7L, 2, 2, TYPE);
        // Sample the tight core (d <= ~0.15 where falloff == 1): placement
        // rate should approximate the rolled richness, ore split ~80/20.
        int box = Math.max(2, (int) (vein.radius() * 0.1));
        int placed = 0;
        int total = 0;
        int ore0 = 0;
        for (int x = -box; x <= box; x++) {
            for (int z = -box; z <= box; z++) {
                for (int y = -1; y <= 1; y++) {
                    int roll = VeinMath.rollOre(vein, vein.centerX() + x, vein.centerY() + y,
                            vein.centerZ() + z);
                    total++;
                    if (roll >= 0) {
                        placed++;
                        if (roll == 0) {
                            ore0++;
                        }
                    }
                }
            }
        }
        double rate = placed / (double) total;
        assertTrue(Math.abs(rate - vein.richness()) < 0.08,
                "core placement rate " + rate + " should be near richness " + vein.richness());
        double share = ore0 / (double) placed;
        assertTrue(Math.abs(share - 0.80) < 0.10,
                "ore 0 share " + share + " should be near its 80% weight");
    }

    @Test
    void differentTypeInSameRegionGetsDifferentGeometry() {
        VeinType other = VeinType.builder("coal_seam")
                .addOreWeight(1).richness(0.35f, 0.70f).yBand(30, 80)
                .radius(48, 80).thickness(12, 24).spawnWeight(12).build();
        VeinInstance a = VeinMath.instance(42L, 1, 1, TYPE);
        VeinInstance b = VeinMath.instance(42L, 1, 1, other);
        // Per-block seeds must differ (rolls are salted by type name); this is
        // hash-collision-proof rather than asserting on individual small rolls.
        assertTrue(a.seed() != b.seed(), "instance rolls must be salted by type name");
    }
}
