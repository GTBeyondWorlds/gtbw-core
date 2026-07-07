package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

/**
 * One concrete vein as it exists in a specific world: the per-instance rolls
 * of a {@link VeinType} for one region. Derived deterministically by
 * {@link VeinMath#instance}; never stored.
 *
 * @param type      the definition this instance was rolled from
 * @param centerX   world X of the lens center
 * @param centerY   world Y of the lens center
 * @param centerZ   world Z of the lens center
 * @param radius    horizontal semi-axis in blocks
 * @param thickness full vertical extent in blocks (semi-axis is thickness/2)
 * @param richness  rolled ore density at the core, in (0, 1]
 * @param seed      per-instance seed for positional (per-block) hashing
 */
public record VeinInstance(VeinType type, int centerX, int centerY, int centerZ,
                           int radius, int thickness, float richness, long seed) {
}
