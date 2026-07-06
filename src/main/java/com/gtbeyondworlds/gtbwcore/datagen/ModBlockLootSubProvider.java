package com.gtbeyondworlds.gtbwcore.datagen;

import java.util.Set;

import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

/** Every block drops itself; nothing needs silk touch or fortune yet. */
public class ModBlockLootSubProvider extends BlockLootSubProvider {

    public ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.COKE_OVEN_BRICKS.get());
        dropSelf(ModBlocks.COKE_OVEN.get());
        dropSelf(ModBlocks.BRICKED_BLAST_FURNACE_BRICKS.get());
        dropSelf(ModBlocks.BRICKED_BLAST_FURNACE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).toList();
    }
}
