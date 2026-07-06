package com.gtbeyondworlds.gtbwcore.content.machine.cokeoven;

import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockControllerBlock;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockLayout;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockPattern;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;
import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.common.util.Lazy;

/**
 * Coke Oven controller: a hollow 3x3x3 of coke oven bricks (25 bricks + this
 * controller), with the controller centered on the front face, second layer.
 */
public class CokeOvenBlock extends MultiblockControllerBlock {
    public static final MapCodec<CokeOvenBlock> CODEC = simpleCodec(CokeOvenBlock::new);

    private static final MultiblockLayout LAYOUT = MultiblockLayout.builder('C')
            .layer("BBB", "BBB", "BBB")
            .layer("BCB", "B B", "BBB")
            .layer("BBB", "BBB", "BBB")
            .build();

    // Lazy: the brick block cannot be resolved until registration has run.
    private static final Lazy<MultiblockPattern> PATTERN = Lazy.of(() -> MultiblockPattern.of(LAYOUT)
            .where('B', ModBlocks.COKE_OVEN_BRICKS)
            .build());

    public CokeOvenBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CokeOvenBlock> codec() {
        return CODEC;
    }

    @Override
    protected MultiblockPattern pattern() {
        return PATTERN.get();
    }
}
