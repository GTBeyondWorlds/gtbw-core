package com.gtbeyondworlds.gtbwcore.datagen;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModItems;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Models for plain items; block item models live in {@link ModBlockStateProvider}. */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, GtbwCore.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.TIN_INGOT.get());
        basicItem(ModItems.BRONZE_INGOT.get());
        basicItem(ModItems.STEEL_INGOT.get());
        basicItem(ModItems.COPPER_DUST.get());
        basicItem(ModItems.TIN_DUST.get());
        basicItem(ModItems.BRONZE_DUST.get());
        basicItem(ModItems.STEEL_DUST.get());
    }
}
