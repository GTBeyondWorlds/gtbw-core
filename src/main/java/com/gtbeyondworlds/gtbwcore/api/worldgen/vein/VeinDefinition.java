package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;

/**
 * A {@link VeinType} bound to actual ore blocks: entry {@code i} of the block
 * list corresponds to ore index {@code i} in the type. This is the class vein
 * content is declared with; the math core only ever sees the {@code VeinType}.
 *
 * <p>Blocks are suppliers so definitions can be created during registration
 * (e.g. from {@code DeferredBlock}s) before blocks exist.
 */
public final class VeinDefinition {

    /** The stone-hosted and deepslate-hosted forms of one ore entry. */
    public record OreBlocks(Supplier<Block> stone, Supplier<Block> deepslate) {}

    private final VeinType type;
    private final List<OreBlocks> ores;

    private VeinDefinition(VeinType type, List<OreBlocks> ores) {
        this.type = type;
        this.ores = List.copyOf(ores);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public VeinType type() {
        return type;
    }

    public OreBlocks ore(int index) {
        return ores.get(index);
    }

    /** Mirrors {@link VeinType.Builder}, additionally collecting the blocks. */
    public static final class Builder {
        private final VeinType.Builder typeBuilder;
        private final List<OreBlocks> ores = new ArrayList<>();

        private Builder(String name) {
            this.typeBuilder = VeinType.builder(name);
        }

        public Builder ore(Supplier<Block> stone, Supplier<Block> deepslate, int weight) {
            typeBuilder.addOreWeight(weight);
            ores.add(new OreBlocks(stone, deepslate));
            return this;
        }

        public Builder richness(float min, float max) {
            typeBuilder.richness(min, max);
            return this;
        }

        public Builder yBand(int min, int max) {
            typeBuilder.yBand(min, max);
            return this;
        }

        public Builder radius(int min, int max) {
            typeBuilder.radius(min, max);
            return this;
        }

        public Builder thickness(int min, int max) {
            typeBuilder.thickness(min, max);
            return this;
        }

        public Builder spawnWeight(int weight) {
            typeBuilder.spawnWeight(weight);
            return this;
        }

        public VeinDefinition build() {
            return new VeinDefinition(typeBuilder.build(), ores);
        }
    }
}
