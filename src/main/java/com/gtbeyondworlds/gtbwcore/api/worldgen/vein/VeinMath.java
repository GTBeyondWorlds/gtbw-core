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
