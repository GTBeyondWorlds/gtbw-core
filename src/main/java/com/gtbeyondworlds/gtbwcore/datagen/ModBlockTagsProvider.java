package com.gtbeyondworlds.gtbwcore.datagen;

import java.util.concurrent.CompletableFuture;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Block tags: everything is pickaxe-mined; ores additionally need stone tools and get {@code c:} ore tags. */
public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GtbwCore.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.COKE_OVEN_BRICKS.get(),
                ModBlocks.COKE_OVEN.get(),
                ModBlocks.BRICKED_BLAST_FURNACE_BRICKS.get(),
                ModBlocks.BRICKED_BLAST_FURNACE.get(),
                ModBlocks.TIN_ORE.get(),
                ModBlocks.DEEPSLATE_TIN_ORE.get());

        tag(BlockTags.NEEDS_STONE_TOOL).add(
                ModBlocks.TIN_ORE.get(),
                ModBlocks.DEEPSLATE_TIN_ORE.get());

        TagKey<Block> tinOres = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/tin"));
        tag(tinOres).add(ModBlocks.TIN_ORE.get(), ModBlocks.DEEPSLATE_TIN_ORE.get());
        tag(Tags.Blocks.ORES).addTag(tinOres);
    }
}
