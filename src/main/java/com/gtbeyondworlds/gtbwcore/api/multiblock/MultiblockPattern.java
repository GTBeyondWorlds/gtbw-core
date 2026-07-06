package com.gtbeyondworlds.gtbwcore.api.multiblock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A {@link MultiblockLayout} bound to actual block requirements, matchable
 * against a level.
 *
 * <p>Build one per structure via {@link #of(MultiblockLayout)}, mapping every
 * block character of the layout to a predicate. Air cells (spaces) are handled
 * automatically. Matching reports the first mismatching world position along
 * with what belongs there, so controllers can tell the player exactly what to
 * fix.
 */
public final class MultiblockPattern {
    private final MultiblockLayout layout;
    private final Map<Character, Element> elements;

    private MultiblockPattern(MultiblockLayout layout, Map<Character, Element> elements) {
        this.layout = layout;
        this.elements = Map.copyOf(elements);
    }

    public static Builder of(MultiblockLayout layout) {
        return new Builder(layout);
    }

    /** A block requirement plus the name shown when it is missing. */
    public record Element(Predicate<BlockState> predicate, Supplier<Component> description) {
    }

    /** Result of matching the pattern against a level. */
    public sealed interface MatchResult {
    }

    /** The structure is complete. */
    public record Success() implements MatchResult {
    }

    /** The first wrong position and the block that belongs there. */
    public record Mismatch(BlockPos pos, Component expected) implements MatchResult {
    }

    /**
     * Checks the structure anchored at {@code controllerPos} with the
     * controller facing {@code facing} (a horizontal direction).
     */
    public MatchResult match(LevelReader level, BlockPos controllerPos, Direction facing) {
        PatternFacing patternFacing = toPatternFacing(facing);
        for (Map.Entry<RelativePos, Character> cell : this.layout.cells().entrySet()) {
            RelativePos pos = cell.getKey();
            BlockPos worldPos = controllerPos.offset(
                    patternFacing.offsetX(pos), patternFacing.offsetY(pos), patternFacing.offsetZ(pos));
            BlockState state = level.getBlockState(worldPos);
            char key = cell.getValue();
            if (key == ' ') {
                if (!state.isAir()) {
                    return new Mismatch(worldPos, Blocks.AIR.getName());
                }
            } else {
                Element element = this.elements.get(key);
                if (!element.predicate().test(state)) {
                    return new Mismatch(worldPos, element.description().get());
                }
            }
        }
        return new Success();
    }

    private static PatternFacing toPatternFacing(Direction direction) {
        return switch (direction) {
            case NORTH -> PatternFacing.NORTH;
            case SOUTH -> PatternFacing.SOUTH;
            case WEST -> PatternFacing.WEST;
            case EAST -> PatternFacing.EAST;
            default -> throw new IllegalArgumentException("Multiblock controllers only face horizontally, got " + direction);
        };
    }

    public static final class Builder {
        private final MultiblockLayout layout;
        private final Map<Character, Element> elements = new HashMap<>();

        private Builder(MultiblockLayout layout) {
            this.layout = layout;
        }

        /** Requires cells marked {@code key} to be exactly {@code block}. */
        public Builder where(char key, Supplier<? extends Block> block) {
            this.elements.put(key, new Element(
                    state -> state.is(block.get()),
                    () -> block.get().getName()));
            return this;
        }

        public MultiblockPattern build() {
            for (char key : this.layout.blockKeys()) {
                if (!this.elements.containsKey(key)) {
                    throw new IllegalStateException("Layout character '" + key + "' has no block requirement");
                }
            }
            return new MultiblockPattern(this.layout, this.elements);
        }
    }
}
