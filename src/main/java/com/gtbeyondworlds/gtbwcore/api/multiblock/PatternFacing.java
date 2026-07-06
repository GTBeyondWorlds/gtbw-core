package com.gtbeyondworlds.gtbwcore.api.multiblock;

/**
 * The four horizontal directions a multiblock controller can face, with the
 * math that maps {@link RelativePos controller-relative positions} into
 * world-space offsets.
 *
 * <p>Axis conventions follow Minecraft: +x = east, +z = south, north = -z.
 * The facing points <em>out of</em> the structure's front face (toward the
 * player who placed the controller). This class is pure — the conversion from
 * {@code net.minecraft.core.Direction} lives in the integration layer.
 */
public enum PatternFacing {
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0),
    EAST(1, 0);

    private final int stepX;
    private final int stepZ;

    PatternFacing(int stepX, int stepZ) {
        this.stepX = stepX;
        this.stepZ = stepZ;
    }

    /**
     * World-space x offset of {@code pos} for a controller facing this way.
     *
     * <p>The structure's right axis is the facing rotated clockwise (viewed
     * from above): {@code (x, z) -> (-z, x)}. The back axis is the opposite
     * of the facing.
     */
    public int offsetX(RelativePos pos) {
        return pos.right() * -stepZ + pos.back() * -stepX;
    }

    /** World-space y offset of {@code pos}; local up is world up. */
    public int offsetY(RelativePos pos) {
        return pos.up();
    }

    /** World-space z offset of {@code pos} for a controller facing this way. */
    public int offsetZ(RelativePos pos) {
        return pos.right() * stepX + pos.back() * -stepZ;
    }
}
