package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import java.util.List;

/**
 * All deterministic vein math: region grid, type selection, instance
 * derivation, and per-block geometry. Every result is a pure function of the
 * world seed and coordinates — nothing is stored, so any chunk generated at
 * any time recomputes identical veins.
 *
 * <p>The hashing here is mod-owned (splitmix64-based) rather than Minecraft's
 * {@code WorldgenRandom} so vein layouts cannot shift under MC updates and so
 * this class stays unit-testable without Minecraft on the classpath. See
 * {@code docs/design/2026-07-07-ore-vein-generation.md}.
 */
public final class VeinMath {

    /** Region edge length in chunks. */
    public static final int REGION_CHUNKS = 24;

    /** Region edge length in blocks. */
    public static final int REGION_BLOCKS = REGION_CHUNKS * 16;

    /** Center offsets roll in [CENTER_MIN, CENTER_MIN + CENTER_SPAN - 1] within the region. */
    static final int CENTER_MIN = REGION_BLOCKS / 4;          // 96
    static final int CENTER_SPAN = REGION_BLOCKS / 2;         // 192

    // Domain-separation salts for the different rolls (arbitrary odd constants).
    private static final long SALT_TYPE = 0x9E3779B97F4A7C15L;
    private static final long SALT_INSTANCE = 0xC2B2AE3D27D4EB4FL;
    private static final long SALT_PLACE = 0x165667B19E3779F9L;
    private static final long SALT_PICK = 0x27D4EB2F165667C5L;
    private static final long SALT_NOISE = 0x85EBCA77C2B2AE63L;

    private VeinMath() {}

    public static int regionFromBlock(int blockCoord) {
        return Math.floorDiv(blockCoord, REGION_BLOCKS);
    }

    public static int regionFromChunk(int chunkCoord) {
        return Math.floorDiv(chunkCoord, REGION_CHUNKS);
    }

    /**
     * Picks the vein type for a region by weighted rendezvous hashing: each
     * type scores from a hash of (seed, region, type name) shaped by its
     * spawn weight; the best score wins. Selection is independent of list
     * order, and adding a new type later only reassigns the regions the new
     * type wins — everything else keeps its vein.
     *
     * @return index into {@code types}, or -1 if the list is empty
     */
    public static int selectTypeIndex(long worldSeed, int regionX, int regionZ, List<VeinType> types) {
        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestName = null;
        for (int i = 0; i < types.size(); i++) {
            VeinType type = types.get(i);
            long hash = hashOf(worldSeed ^ SALT_TYPE, regionX, regionZ, fnv1a64(type.name()));
            // u in (0,1) strictly, so log(u) < 0 and the score is positive.
            double u = ((hash >>> 11) + 0.5) * 0x1.0p-53;
            double score = -type.spawnWeight() / Math.log(u);
            if (score > bestScore
                    || (score == bestScore && bestName != null && type.name().compareTo(bestName) < 0)) {
                best = i;
                bestScore = score;
                bestName = type.name();
            }
        }
        return best;
    }

    /**
     * Rolls the concrete vein for a region. Pure: same inputs, same instance.
     * Rolls are salted by the type name so a region re-resolved to a different
     * type (after a definition change) also gets fresh geometry.
     */
    public static VeinInstance instance(long worldSeed, int regionX, int regionZ, VeinType type) {
        long seed = hashOf(worldSeed ^ SALT_INSTANCE, regionX, regionZ, fnv1a64(type.name()));
        int centerX = regionX * REGION_BLOCKS + CENTER_MIN + rollInt(seed, 1, CENTER_SPAN);
        int centerZ = regionZ * REGION_BLOCKS + CENTER_MIN + rollInt(seed, 2, CENTER_SPAN);
        int radius = type.radiusMin() + rollInt(seed, 3, type.radiusMax() - type.radiusMin() + 1);
        int thickness = type.thicknessMin()
                + rollInt(seed, 4, type.thicknessMax() - type.thicknessMin() + 1);
        float richness = type.richnessMin()
                + (float) uniform(hashOf(seed, 5, 0, 0)) * (type.richnessMax() - type.richnessMin());
        int vReach = VeinType.verticalReach(thickness);
        int yLo = type.yMin() + vReach;
        int yHi = type.yMax() - vReach;
        int centerY = yLo + rollInt(seed, 6, yHi - yLo + 1);
        long blockSeed = hashOf(seed, 7, 0, 0);
        return new VeinInstance(type, centerX, centerY, centerZ, radius, thickness, richness, blockSeed);
    }

    /** Worst-case wobbled horizontal reach from the center, in blocks. */
    public static int maxReachXZ(VeinInstance vein) {
        return (int) Math.ceil(vein.radius() / (1.0 - VeinType.WOBBLE));
    }

    /** Worst-case wobbled vertical reach from the center, in blocks. */
    public static int maxReachY(VeinInstance vein) {
        return VeinType.verticalReach(vein.thickness());
    }

    /**
     * Normalized wobbled distance of a block from the vein core; the block is
     * inside the lens iff the result is < 1. Smooth value noise (8-block
     * lattice) warps the boundary so lenses are not perfect ellipsoids.
     */
    public static double normalizedDistance(VeinInstance vein, int x, int y, int z) {
        double dx = (x - vein.centerX()) / (double) vein.radius();
        double dz = (z - vein.centerZ()) / (double) vein.radius();
        double dy = (y - vein.centerY()) / (vein.thickness() / 2.0);
        double base = Math.sqrt(dx * dx + dz * dz + dy * dy);
        double wobble = valueNoise(mix(vein.seed() ^ SALT_NOISE), x * 0.125, y * 0.125, z * 0.125);
        return base * (1.0 + VeinType.WOBBLE * wobble);
    }

    /** Richness multiplier by distance: saturated core, fringe thinning to 0. */
    public static double falloff(double d) {
        if (d >= 1.0) {
            return 0.0;
        }
        return Math.min(1.0, 1.25 * (1.0 - d));
    }

    /**
     * Decides what to place at a block: -1 to leave the rock alone, otherwise
     * an ore index into the vein's definition. Purely positional — never
     * sequential — so all chunks agree about shared veins.
     */
    public static int rollOre(VeinInstance vein, int x, int y, int z) {
        double d = normalizedDistance(vein, x, y, z);
        double p = vein.richness() * falloff(d);
        if (p <= 0.0) {
            return -1;
        }
        if (uniform(hashOf(vein.seed() ^ SALT_PLACE, x, y, z)) >= p) {
            return -1;
        }
        VeinType type = vein.type();
        int pick = (int) Math.floorMod(hashOf(vein.seed() ^ SALT_PICK, x, y, z),
                (long) type.totalOreWeight());
        for (int i = 0; i < type.oreCount(); i++) {
            pick -= type.oreWeight(i);
            if (pick < 0) {
                return i;
            }
        }
        return type.oreCount() - 1; // unreachable; guards float edge cases
    }

    /** Bounded roll k from an instance seed: uniform int in [0, bound). */
    private static int rollInt(long seed, int k, int bound) {
        return (int) Math.floorMod(hashOf(seed, k, 0x5DEECE66DL, 0), (long) bound);
    }

    /**
     * Smooth value noise in [-1, 1]: uniform lattice values, trilinear
     * interpolation with smoothstep. Enough to roughen a lens boundary.
     */
    private static double valueNoise(long seed, double x, double y, double z) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int z0 = (int) Math.floor(z);
        double tx = smooth(x - x0);
        double ty = smooth(y - y0);
        double tz = smooth(z - z0);
        double c000 = corner(seed, x0, y0, z0);
        double c100 = corner(seed, x0 + 1, y0, z0);
        double c010 = corner(seed, x0, y0 + 1, z0);
        double c110 = corner(seed, x0 + 1, y0 + 1, z0);
        double c001 = corner(seed, x0, y0, z0 + 1);
        double c101 = corner(seed, x0 + 1, y0, z0 + 1);
        double c011 = corner(seed, x0, y0 + 1, z0 + 1);
        double c111 = corner(seed, x0 + 1, y0 + 1, z0 + 1);
        double x00 = lerp(tx, c000, c100);
        double x10 = lerp(tx, c010, c110);
        double x01 = lerp(tx, c001, c101);
        double x11 = lerp(tx, c011, c111);
        double y0v = lerp(ty, x00, x10);
        double y1v = lerp(ty, x01, x11);
        return lerp(tz, y0v, y1v);
    }

    private static double corner(long seed, int x, int y, int z) {
        return uniform(hashOf(seed, x, y, z)) * 2.0 - 1.0;
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    // ---------------------------------------------------------------------
    // Hashing primitives (splitmix64 finalizer). Package-private for reuse by
    // the geometry half of this class and its tests.
    // ---------------------------------------------------------------------

    /** splitmix64 finalizer: a high-quality 64-bit bijective mix. */
    static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** Order-sensitive hash of a seed and three values. */
    static long hashOf(long seed, long a, long b, long c) {
        long h = mix(seed ^ 0x2545F4914F6CDD1DL);
        h = mix(h ^ a);
        h = mix(h ^ b);
        h = mix(h ^ c);
        return h;
    }

    /** Maps a hash to a uniform double in [0, 1). */
    static double uniform(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    /** Stable 64-bit FNV-1a of a string; the durable identity of a vein name. */
    static long fnv1a64(String s) {
        long h = 0xCBF29CE484222325L;
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x100000001B3L;
        }
        return h;
    }
}
