package com.gtbeyondworlds.gtbwcore.content.machine.blastfurnace;

import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockControllerBlock;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockLayout;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockPattern;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;
import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.common.util.Lazy;

/**
 * Bricked Blast Furnace controller: 3x3 footprint, 4 blocks high, solid base,
 * hollow center column above it (open at the top like a chimney). 32 bricks +
 * this controller, which sits centered on the front face, second layer.
 */
public class BrickedBlastFurnaceBlock extends MultiblockControllerBlock {
    public static final MapCodec<BrickedBlastFurnaceBlock> CODEC = simpleCodec(BrickedBlastFurnaceBlock::new);

    private static final MultiblockLayout LAYOUT = MultiblockLayout.builder('C')
            .layer("BBB", "BBB", "BBB")
            .layer("BCB", "B B", "BBB")
            .layer("BBB", "B B", "BBB")
            .layer("BBB", "B B", "BBB")
            .build();

    // Lazy: the brick block cannot be resolved until registration has run.
    private static final Lazy<MultiblockPattern> PATTERN = Lazy.of(() -> MultiblockPattern.of(LAYOUT)
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
