package com.gtbeyondworlds.gtbwcore.datagen;

import java.util.concurrent.CompletableFuture;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Block tags; every block so far is brickwork mined with a pickaxe. */
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
                ModBlocks.BRICKED_BLAST_FURNACE_PIECES.get(),
                ModBlocks.BRICKED_BLAST_FURNACE.get());
    }
}
