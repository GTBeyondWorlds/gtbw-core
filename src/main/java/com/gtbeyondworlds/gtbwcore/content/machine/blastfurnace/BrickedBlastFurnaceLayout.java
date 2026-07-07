package com.gtbeyondworlds.gtbwcore.content.machine.blastfurnace;

import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockLayout;

/**
 * The Bricked Blast Furnace shape: 3x3 footprint, 5 blocks high, solid brick
 * base, hollow center column above it (open at the top like a chimney),
 * controller centered on the front face, second layer. 40 bricks + the
 * controller = 41 blocks.
 *
 * <p>Kept free of Minecraft imports so the unit tests can pin the real layout.
 */
public final class BrickedBlastFurnaceLayout {
    public static final MultiblockLayout LAYOUT = MultiblockLayout.builder('C')
            .layer("BBB", "BBB", "BBB")
            .layer("BCB", "B B", "BBB")
            .layer("BBB", "B B", "BBB")
            .layer("BBB", "B B", "BBB")
            .layer("BBB", "B B", "BBB")
            .build();

    private BrickedBlastFurnaceLayout() {}
}
