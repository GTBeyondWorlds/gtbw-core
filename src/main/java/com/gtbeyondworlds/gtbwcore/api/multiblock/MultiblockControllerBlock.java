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
 * <p>Structures form and un-form automatically. The controller carries
 * {@code FACING} (set on placement, pointing at the placer) and {@link #FORMED}.
 * A structure check is scheduled whenever the controller is placed, a neighbor
 * changes, or a {@link MultiblockPartBlock} is placed or removed nearby; the
 * check flips {@code FORMED} to match reality, so completing the build forms
 * the machine and breaking any part un-forms it on the next tick. A slow
 * polling heartbeat backs this up in both states, catching changes that
 * produce no callback (e.g. a foreign block pushed into — or cleared out of —
 * a cell that must stay air).
 *
 * <p>Right-clicking never changes anything; it just reports the structure
 * status, including the first wrong position and what belongs there.
 *
 * <p>Controllers are stateless placeholders until machines gain processing
 * logic; see the roadmap in {@code docs/ARCHITECTURE.md}.
 */
public abstract class MultiblockControllerBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    /**
     * How far (Chebyshev distance, blocks) a controller can be from any part of
     * its structure. Must cover the largest registered pattern; parts use it to
     * find controllers to notify.
     */
    private static final int PART_SCAN_RADIUS = 3;

    private static final int FORMED_RECHECK_TICKS = 20;
    private static final int UNFORMED_RECHECK_TICKS = 40;

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

    /** Asks every controller within {@link #PART_SCAN_RADIUS} of {@code pos} to re-check its structure. */
    public static void requestChecksAround(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (BlockPos cursor : BlockPos.betweenClosed(
                pos.offset(-PART_SCAN_RADIUS, -PART_SCAN_RADIUS, -PART_SCAN_RADIUS),
                pos.offset(PART_SCAN_RADIUS, PART_SCAN_RADIUS, PART_SCAN_RADIUS))) {
            if (!level.hasChunkAt(cursor)) {
                continue;
            }
            BlockState state = level.getBlockState(cursor);
            if (state.getBlock() instanceof MultiblockControllerBlock controller) {
                controller.scheduleCheck(level, cursor.immutable());
            }
        }
    }

    private void scheduleCheck(Level level, BlockPos pos) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            scheduleCheck(level, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            // Status report only; formation is automatic.
            switch (pattern().match(serverLevel, pos, state.getValue(FACING))) {
                case MultiblockPattern.Success ignored -> player.displayClientMessage(
                        Component.translatable("gtbwcore.multiblock.formed", getName()), true);
                case MultiblockPattern.Mismatch mismatch -> player.displayClientMessage(
                        Component.translatable("gtbwcore.multiblock.incomplete", mismatch.expected(),
                                mismatch.pos().getX(), mismatch.pos().getY(), mismatch.pos().getZ()),
                        true);
            }
            // Keep the blockstate honest even if a callback was ever missed.
            scheduleCheck(level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean complete = pattern().match(level, pos, state.getValue(FACING)) instanceof MultiblockPattern.Success;
        if (complete != state.getValue(FORMED)) {
            level.setBlock(pos, state.setValue(FORMED, complete), Block.UPDATE_ALL);
        }
        // Part callbacks give instant reactions; this slow poll is the safety net
        // for changes that produce no callback in either direction — e.g. a
        // foreign block appearing in (or vanishing from) a must-be-air cell that
        // is not adjacent to the controller. The pending tick persists with the
        // chunk, so the heartbeat survives save/load.
        level.scheduleTick(pos, this, complete ? FORMED_RECHECK_TICKS : UNFORMED_RECHECK_TICKS);
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            scheduleCheck(level, pos);
        }
    }
}
