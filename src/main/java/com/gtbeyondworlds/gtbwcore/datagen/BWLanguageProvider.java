package com.gtbeyondworlds.gtbwcore.datagen;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.material.BWMaterials;
import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BWLanguageProvider extends LanguageProvider {
    public BWLanguageProvider(PackOutput output) {
        super(output, GtbwCore.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.addItemTranslations();

        this.addMaterialTranslations();
    }

    private void addItemTranslations() {
        this.addItem("dust", "%s Dust");
        this.addItem("dust_small", "Small Pile of %s Dust");
        this.addItem("dust_tiny", "Tiny Pile of %s Dust");

        this.addItem("ingot", "%s Ingot");
    }

    private void addMaterialTranslations() {
        this.addMaterial(BWMaterials.TIN, "Tin");
        this.addMaterial(BWMaterials.BRONZE, "Bronze");
    }

    private void addMaterial(BWMaterial material, String name) {
        this.addEntry("material", material.getMaterialId(), name);
    }

    private void addItem(String itemId, String name) {
        this.addEntry("item", itemId, name);
    }

    private void addEntry(String prefix, String suffix, String name) {
        this.add(prefix + "." + GtbwCore.MOD_ID + "." + suffix, name);
    }
}
