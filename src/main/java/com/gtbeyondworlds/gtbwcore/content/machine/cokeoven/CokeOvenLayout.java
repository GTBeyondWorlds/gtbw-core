package com.gtbeyondworlds.gtbwcore.content.machine.cokeoven;

import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockLayout;

/**
 * The Coke Oven shape: a hollow 3x3x3 of coke oven bricks (25 bricks + the
 * controller), controller centered on the front face, second layer.
 *
 * <p>Kept free of Minecraft imports so the unit tests can pin the real layout.
 */
public final class CokeOvenLayout {
    public static final MultiblockLayout LAYOUT = MultiblockLayout.builder('C')
            .layer("BBB", "BBB", "BBB")
            .layer("BCB", "B B", "BBB")
            .layer("BBB", "BBB", "BBB")
            .build();

    private CokeOvenLayout() {}
}
