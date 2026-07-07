package com.gtbeyondworlds.gtbwcore.datagen;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockControllerBlock;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Blockstates and block models, including the block items' models. */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, GtbwCore.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.COKE_OVEN_BRICKS.get(), cubeAll(ModBlocks.COKE_OVEN_BRICKS.get()));
        simpleBlockWithItem(ModBlocks.BRICKED_BLAST_FURNACE_BRICKS.get(),
                cubeAll(ModBlocks.BRICKED_BLAST_FURNACE_BRICKS.get()));

        controllerBlock(ModBlocks.COKE_OVEN.get(), "coke_oven", "coke_oven_bricks");
        controllerBlock(ModBlocks.BRICKED_BLAST_FURNACE.get(), "bricked_blast_furnace",
                "bricked_blast_furnace_bricks");

        simpleBlockWithItem(ModBlocks.TIN_ORE.get(), cubeAll(ModBlocks.TIN_ORE.get()));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_TIN_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_TIN_ORE.get()));
    }

    /**
     * A horizontally-rotated controller: brick texture on all sides, a front
     * texture with an opening that lights up while the multiblock is formed.
     */
    private void controllerBlock(Block block, String name, String sideTexture) {
        ModelFile off = models().orientable(name,
                modLoc("block/" + sideTexture), modLoc("block/" + name + "_front"), modLoc("block/" + sideTexture));
        ModelFile on = models().orientable(name + "_on",
                modLoc("block/" + sideTexture), modLoc("block/" + name + "_front_on"), modLoc("block/" + sideTexture));
        horizontalBlock(block, state -> state.getValue(MultiblockControllerBlock.FORMED) ? on : off);
        simpleBlockItem(block, off);
    }
}
