package com.gtbeyondworlds.gtbwcore.datagen;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;
import com.gtbeyondworlds.gtbwcore.registry.ModItems;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** English (en_us) names for everything the mod registers. */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, GtbwCore.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.gtbwcore.main", "GTBW Core");

        addItem(ModItems.TIN_INGOT, "Tin Ingot");
        addItem(ModItems.BRONZE_INGOT, "Bronze Ingot");
        addItem(ModItems.STEEL_INGOT, "Steel Ingot");
        addItem(ModItems.COPPER_DUST, "Copper Dust");
        addItem(ModItems.TIN_DUST, "Tin Dust");
        addItem(ModItems.BRONZE_DUST, "Bronze Dust");
        addItem(ModItems.STEEL_DUST, "Steel Dust");
        addItem(ModItems.RAW_TIN, "Raw Tin");

        addBlock(ModBlocks.COKE_OVEN_BRICKS, "Coke Oven Bricks");
        addBlock(ModBlocks.COKE_OVEN, "Coke Oven");
        addBlock(ModBlocks.BRICKED_BLAST_FURNACE_BRICKS, "Bricked Blast Furnace Bricks");
        addBlock(ModBlocks.BRICKED_BLAST_FURNACE, "Bricked Blast Furnace");
        addBlock(ModBlocks.TIN_ORE, "Tin Ore");
        addBlock(ModBlocks.DEEPSLATE_TIN_ORE, "Deepslate Tin Ore");

        add("gtbwcore.multiblock.formed", "%s is formed.");
        add("gtbwcore.multiblock.incomplete", "Multiblock incomplete: expected %s at (%s, %s, %s)");
    }
}
