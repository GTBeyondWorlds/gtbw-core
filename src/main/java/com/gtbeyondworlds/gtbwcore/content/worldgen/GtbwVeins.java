package com.gtbeyondworlds.gtbwcore.content.worldgen;

import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinDefinition;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinRegistry;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;

import net.minecraft.world.level.block.Blocks;

/**
 * The vein roster: one readable definition per vein type. Numbers are
 * starting points from the design record; tune here after in-world review.
 *
 * <p>Vein names are permanent world-visible identity (they seed the region
 * assignment) — renaming one reshuffles which regions it owns. Add, don't
 * rename.
 */
public final class GtbwVeins {

    public static final VeinDefinition CASSITERITE = VeinRegistry.register(
            VeinDefinition.builder("cassiterite")
                    // ::get adapts DeferredBlock<DropExperienceBlock> to Supplier<Block> (generics are invariant); resolution stays lazy.
                    .ore(ModBlocks.TIN_ORE::get, ModBlocks.DEEPSLATE_TIN_ORE::get, 80)
                    .ore(() -> Blocks.COPPER_ORE, () -> Blocks.DEEPSLATE_COPPER_ORE, 20)
                    .richness(0.20f, 0.50f)
                    .yBand(20, 70)
                    .radius(48, 80)
                    .thickness(12, 24)
                    .spawnWeight(10)
                    .build());

    public static final VeinDefinition CHALCOPYRITE = VeinRegistry.register(
            VeinDefinition.builder("chalcopyrite")
                    .ore(() -> Blocks.COPPER_ORE, () -> Blocks.DEEPSLATE_COPPER_ORE, 75)
                    .ore(() -> Blocks.IRON_ORE, () -> Blocks.DEEPSLATE_IRON_ORE, 25)
                    .richness(0.25f, 0.60f)
                    .yBand(0, 60)
                    .radius(48, 80)
                    .thickness(12, 24)
                    .spawnWeight(10)
                    .build());

    public static final VeinDefinition MAGNETITE = VeinRegistry.register(
            VeinDefinition.builder("magnetite")
                    .ore(() -> Blocks.IRON_ORE, () -> Blocks.DEEPSLATE_IRON_ORE, 100)
                    .richness(0.30f, 0.65f)
                    .yBand(-30, 40)
                    .radius(48, 80)
                    .thickness(12, 24)
                    .spawnWeight(10)
                    .build());

    public static final VeinDefinition COAL_SEAM = VeinRegistry.register(
            VeinDefinition.builder("coal_seam")
                    .ore(() -> Blocks.COAL_ORE, () -> Blocks.DEEPSLATE_COAL_ORE, 100)
                    .richness(0.35f, 0.70f)
                    .yBand(30, 80)
                    .radius(48, 80)
                    .thickness(12, 24)
                    .spawnWeight(12)
                    .build());

    private GtbwVeins() {}

    /** Forces class load so the definitions above register during mod construction. */
    public static void init() {}
}
