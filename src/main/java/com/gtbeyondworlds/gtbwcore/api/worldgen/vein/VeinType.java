package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Immutable definition of a vein type: which ores it contains (as weights —
 * block binding lives in {@code VeinDefinition}), how rich it rolls, where it
 * sits vertically, and how big it gets.
 *
 * <p>Deliberately free of Minecraft classes so the whole math core is
 * unit-testable (see the test rule in {@code build.gradle}). Ores are
 * addressed by index; {@code VeinDefinition} keeps the parallel block list.
 *
 * <p>All validation happens in {@link Builder#build()} and fails fast with
 * the vein's name in the message, so a bad definition can never reach
 * worldgen. See {@code docs/design/2026-07-07-ore-vein-generation.md}.
 */
public final class VeinType {

    /**
     * Largest configurable horizontal radius. A vein's wobbled edge reaches at
     * most {@code ceil(radius / (1 - WOBBLE))} blocks from its center; at 84
     * that is 96, and centers in adjacent regions are always at least 193
     * blocks apart — so overlap is structurally impossible.
     */
    public static final int MAX_CONFIG_RADIUS = 84;

    /** Boundary wobble amplitude, as a fraction of normalized distance. */
    public static final double WOBBLE = 0.12;

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9_]+");

    private final String name;
    private final int[] oreWeights;
    private final int totalOreWeight;
    private final float richnessMin;
    private final float richnessMax;
    private final int yMin;
    private final int yMax;
    private final int radiusMin;
    private final int radiusMax;
    private final int thicknessMin;
    private final int thicknessMax;
    private final int spawnWeight;

    private VeinType(Builder builder) {
        this.name = builder.name;
        this.oreWeights = builder.oreWeights.stream().mapToInt(Integer::intValue).toArray();
        this.totalOreWeight = java.util.Arrays.stream(this.oreWeights).sum();
        this.richnessMin = builder.richnessMin;
        this.richnessMax = builder.richnessMax;
        this.yMin = builder.yMin;
        this.yMax = builder.yMax;
        this.radiusMin = builder.radiusMin;
        this.radiusMax = builder.radiusMax;
        this.thicknessMin = builder.thicknessMin;
        this.thicknessMax = builder.thicknessMax;
        this.spawnWeight = builder.spawnWeight;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String name() {
        return name;
    }

    public int oreCount() {
        return oreWeights.length;
    }

    public int oreWeight(int index) {
        return oreWeights[index];
    }

    public int totalOreWeight() {
        return totalOreWeight;
    }

    public float richnessMin() {
        return richnessMin;
    }

    public float richnessMax() {
        return richnessMax;
    }

    public int yMin() {
        return yMin;
    }

    public int yMax() {
        return yMax;
    }

    public int radiusMin() {
        return radiusMin;
    }

    public int radiusMax() {
        return radiusMax;
    }

    public int thicknessMin() {
        return thicknessMin;
    }

    public int thicknessMax() {
        return thicknessMax;
    }

    public int spawnWeight() {
        return spawnWeight;
    }

    /** Builder; see class docs. Setter order does not matter. */
    public static final class Builder {
        private final String name;
        private final List<Integer> oreWeights = new ArrayList<>();
        private float richnessMin;
        private float richnessMax;
        private int yMin;
        private int yMax;
        private int radiusMin;
        private int radiusMax;
        private int thicknessMin;
        private int thicknessMax;
        private int spawnWeight = 1;

        private Builder(String name) {
            this.name = name;
        }

        public Builder addOreWeight(int weight) {
            oreWeights.add(weight);
            return this;
        }

        public Builder richness(float min, float max) {
            this.richnessMin = min;
            this.richnessMax = max;
            return this;
        }

        public Builder yBand(int min, int max) {
            this.yMin = min;
            this.yMax = max;
            return this;
        }

        public Builder radius(int min, int max) {
            this.radiusMin = min;
            this.radiusMax = max;
            return this;
        }

        public Builder thickness(int min, int max) {
            this.thicknessMin = min;
            this.thicknessMax = max;
            return this;
        }

        public Builder spawnWeight(int weight) {
            this.spawnWeight = weight;
            return this;
        }

        public VeinType build() {
            check(name != null && NAME_PATTERN.matcher(name).matches(),
                    "name must be non-empty lowercase [a-z0-9_]+, got '" + name + "'");
            check(!oreWeights.isEmpty(), "at least one ore is required");
            for (int weight : oreWeights) {
                check(weight > 0, "ore weights must be positive, got " + weight);
            }
            check(richnessMin > 0 && richnessMin <= richnessMax && richnessMax <= 1,
                    "richness must satisfy 0 < min <= max <= 1, got " + richnessMin + ".." + richnessMax);
            check(radiusMin >= 1 && radiusMin <= radiusMax && radiusMax <= MAX_CONFIG_RADIUS,
                    "radius must satisfy 1 <= min <= max <= " + MAX_CONFIG_RADIUS
                            + ", got " + radiusMin + ".." + radiusMax);
            check(thicknessMin >= 1 && thicknessMin <= thicknessMax,
                    "thickness must satisfy 1 <= min <= max, got " + thicknessMin + ".." + thicknessMax);
            int minBand = 2 * verticalReach(thicknessMax);
            check(yMax - yMin >= minBand,
                    "y band " + yMin + ".." + yMax + " is thinner than " + minBand
                            + " blocks needed by max thickness " + thicknessMax);
            check(spawnWeight > 0, "spawnWeight must be positive, got " + spawnWeight);
            return new VeinType(this);
        }

        private void check(boolean condition, String message) {
            if (!condition) {
                throw new IllegalArgumentException("Vein '" + name + "': " + message);
            }
        }
    }

    /** Worst-case wobbled vertical reach of a lens with the given thickness. */
    static int verticalReach(int thickness) {
        return (int) Math.ceil((thickness / 2.0) / (1.0 - WOBBLE));
    }
}
