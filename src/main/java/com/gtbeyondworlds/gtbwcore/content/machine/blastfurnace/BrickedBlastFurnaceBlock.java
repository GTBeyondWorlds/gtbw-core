package com.gtbeyondworlds.gtbwcore.content.machine.blastfurnace;

import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockControllerBlock;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockPattern;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;
import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.common.util.Lazy;

/**
 * Bricked Blast Furnace controller. The shape lives in
 * {@link BrickedBlastFurnaceLayout} so the unit tests can pin it.
 */
public class BrickedBlastFurnaceBlock extends MultiblockControllerBlock {
    public static final MapCodec<BrickedBlastFurnaceBlock> CODEC = simpleCodec(BrickedBlastFurnaceBlock::new);

    // Lazy: the brick block cannot be resolved until registration has run.
    private static final Lazy<MultiblockPattern> PATTERN = Lazy.of(() -> MultiblockPattern.of(BrickedBlastFurnaceLayout.LAYOUT)
            .where('B', ModBlocks.BRICKED_BLAST_FURNACE_BRICKS)
            .build());

    public BrickedBlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BrickedBlastFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    protected MultiblockPattern pattern() {
        return PATTERN.get();
    }
}
