package com.gtbeyondworlds.gtbwcore.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block that can be part of a multiblock structure.
 *
 * <p>Placing or removing one pings every {@link MultiblockControllerBlock}
 * nearby, which is what makes structures form and un-form automatically —
 * no interaction required. This covers player builds, pistons, explosions,
 * and anything else that goes through normal block updates.
 */
public class MultiblockPartBlock extends Block {

    public MultiblockPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        MultiblockControllerBlock.requestChecksAround(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            MultiblockControllerBlock.requestChecksAround(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
