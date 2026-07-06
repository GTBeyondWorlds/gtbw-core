package com.gtbeyondworlds.gtbwcore.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Base class for multiblock controller blocks.
 *
 * <p>The controller carries {@code FACING} (set on placement, pointing at the
 * placer) and {@link #FORMED}. Right-clicking validates the structure: on
 * success the block becomes formed, on failure the player is told the first
 * wrong position and what belongs there. A formed controller re-validates on
 * neighbor changes and via a slow scheduled tick, so breaking any structure
 * block un-forms it within {@value #RECHECK_INTERVAL_TICKS} ticks.
 *
 * <p>Controllers are stateless placeholders until machines gain processing
 * logic; see the roadmap in {@code docs/ARCHITECTURE.md}.
 */
public abstract class MultiblockControllerBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private static final int RECHECK_INTERVAL_TICKS = 20;

    protected MultiblockControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
    }

    /**
     * The structure pattern. Implementations must resolve blocks lazily; this
     * is only called once registries are populated.
     */
    protected abstract MultiblockPattern pattern();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            switch (pattern().match(serverLevel, pos, state.getValue(FACING))) {
                case MultiblockPattern.Success ignored -> {
                    if (!state.getValue(FORMED)) {
                        serverLevel.setBlock(pos, state.setValue(FORMED, true), Block.UPDATE_ALL);
                        serverLevel.scheduleTick(pos, this, RECHECK_INTERVAL_TICKS);
                    }
                    player.displayClientMessage(
                            Component.translatable("gtbwcore.multiblock.formed", getName()), true);
                }
                case MultiblockPattern.Mismatch mismatch -> {
                    if (state.getValue(FORMED)) {
                        serverLevel.setBlock(pos, state.setValue(FORMED, false), Block.UPDATE_ALL);
                    }
                    player.displayClientMessage(
                            Component.translatable("gtbwcore.multiblock.incomplete", mismatch.expected(),
                                    mismatch.pos().getX(), mismatch.pos().getY(), mismatch.pos().getZ()),
                            true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FORMED)) {
            return;
        }
        if (pattern().match(level, pos, state.getValue(FACING)) instanceof MultiblockPattern.Mismatch) {
            level.setBlock(pos, state.setValue(FORMED, false), Block.UPDATE_ALL);
        } else {
            level.scheduleTick(pos, this, RECHECK_INTERVAL_TICKS);
        }
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        // Cheap instant feedback for the structure blocks that touch the controller;
        // the scheduled tick covers the rest of the structure.
        if (level instanceof ServerLevel serverLevel
                && state.getValue(FORMED)
                && pattern().match(serverLevel, pos, state.getValue(FACING)) instanceof MultiblockPattern.Mismatch) {
            serverLevel.setBlock(pos, state.setValue(FORMED, false), Block.UPDATE_ALL);
        }
    }
}
