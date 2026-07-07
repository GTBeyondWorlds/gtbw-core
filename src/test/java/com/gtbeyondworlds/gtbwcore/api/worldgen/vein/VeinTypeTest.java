package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VeinTypeTest {

    /** A fully valid builder; individual tests break one thing at a time. */
    private static VeinType.Builder valid() {
        return VeinType.builder("cassiterite")
                .addOreWeight(80)
                .addOreWeight(20)
                .richness(0.20f, 0.50f)
                .yBand(20, 70)
                .radius(48, 80)
                .thickness(12, 24)
                .spawnWeight(10);
    }

    @Test
    void buildsAndExposesValues() {
        VeinType type = valid().build();
        assertEquals("cassiterite", type.name());
        assertEquals(2, type.oreCount());
        assertEquals(80, type.oreWeight(0));
        assertEquals(20, type.oreWeight(1));
        assertEquals(100, type.totalOreWeight());
        assertEquals(0.20f, type.richnessMin());
        assertEquals(0.50f, type.richnessMax());
        assertEquals(20, type.yMin());
        assertEquals(70, type.yMax());
        assertEquals(48, type.radiusMin());
        assertEquals(80, type.radiusMax());
        assertEquals(12, type.thicknessMin());
        assertEquals(24, type.thicknessMax());
        assertEquals(10, type.spawnWeight());
    }

    @Test
    void rejectsBadNames() {
        assertThrows(IllegalArgumentException.class, () -> VeinType.builder("").build());
        assertThrows(IllegalArgumentException.class, () -> VeinType.builder("Bad Name").build());
        assertThrows(IllegalArgumentException.class, () -> VeinType.builder("UPPER").build());
    }

    @Test
    void rejectsMissingOrNonPositiveOres() {
        VeinType.Builder noOres = VeinType.builder("x").richness(0.2f, 0.5f).yBand(0, 60)
                .radius(48, 80).thickness(12, 24).spawnWeight(1);
        assertThrows(IllegalArgumentException.class, noOres::build);
        assertThrows(IllegalArgumentException.class, () -> valid().addOreWeight(0).build());
        assertThrows(IllegalArgumentException.class, () -> valid().addOreWeight(-5).build());
    }

    @Test
    void rejectsBadRichness() {
        assertThrows(IllegalArgumentException.class, () -> valid().richness(0.0f, 0.5f).build());
        assertThrows(IllegalArgumentException.class, () -> valid().richness(0.6f, 0.5f).build());
        assertThrows(IllegalArgumentException.class, () -> valid().richness(0.5f, 1.01f).build());
    }

    @Test
    void rejectsRadiusOverStructuralCap() {
        // 84 is the largest radius whose wobbled reach stays inside the region guarantee.
        assertEquals(84, VeinType.MAX_CONFIG_RADIUS);
        assertThrows(IllegalArgumentException.class, () -> valid().radius(48, 85).build());
        assertThrows(IllegalArgumentException.class, () -> valid().radius(0, 80).build());
        assertThrows(IllegalArgumentException.class, () -> valid().radius(80, 48).build());
    }

    @Test
    void rejectsBandTooThinForThickness() {
        // Band must fit the wobbled vertical reach: 2 * ceil((tMax/2) / (1 - WOBBLE)).
        // tMax = 24 -> 2 * ceil(12 / 0.88) = 2 * 14 = 28.
        assertThrows(IllegalArgumentException.class, () -> valid().yBand(0, 27).build());
        // Exactly 28 is legal.
        valid().yBand(0, 28).build();
    }

    @Test
    void rejectsBadThicknessAndWeight() {
        assertThrows(IllegalArgumentException.class, () -> valid().thickness(0, 24).build());
        assertThrows(IllegalArgumentException.class, () -> valid().thickness(24, 12).build());
        assertThrows(IllegalArgumentException.class, () -> valid().spawnWeight(0).build());
    }

    @Test
    void errorMessagesNameTheVein() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> valid().richness(0.9f, 0.2f).build());
        assertTrue(e.getMessage().contains("cassiterite"), "message should name the vein: " + e.getMessage());
    }
}
