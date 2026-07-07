package com.gtbeyondworlds.gtbwcore.content.machine.cokeoven;

import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockControllerBlock;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockPattern;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;
import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.common.util.Lazy;

/**
 * Coke Oven controller. The shape lives in {@link CokeOvenLayout} so the unit
 * tests can pin it.
 */
public class CokeOvenBlock extends MultiblockControllerBlock {
    public static final MapCodec<CokeOvenBlock> CODEC = simpleCodec(CokeOvenBlock::new);

    // Lazy: the brick block cannot be resolved until registration has run.
    private static final Lazy<MultiblockPattern> PATTERN = Lazy.of(() -> MultiblockPattern.of(CokeOvenLayout.LAYOUT)
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
