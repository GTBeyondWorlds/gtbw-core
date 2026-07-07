package com.gtbeyondworlds.gtbwcore.datagen;

import java.util.concurrent.CompletableFuture;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Recipes. Dusts smelt/blast into their ingots; steel dust deliberately has no
 * furnace recipe (it needs blasting), and copper dust yields the vanilla ingot.
 */
public class ModRecipeProvider extends RecipeProvider {

    private static final float SMELT_XP = 0.7f;
    private static final int SMELT_TICKS = 200;
    private static final int BLAST_TICKS = 100;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        dustSmelting(recipeOutput, ModItems.COPPER_DUST.get(), Items.COPPER_INGOT, "copper");
        dustSmelting(recipeOutput, ModItems.TIN_DUST.get(), ModItems.TIN_INGOT.get(), "tin");
        dustSmelting(recipeOutput, ModItems.BRONZE_DUST.get(), ModItems.BRONZE_INGOT.get(), "bronze");
        // Steel needs the heat of a blast furnace; no regular smelting on purpose.
        dustBlasting(recipeOutput, ModItems.STEEL_DUST.get(), ModItems.STEEL_INGOT.get(), "steel");

        // Raw tin smelts like vanilla raw ores; same numbers as the dusts.
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ModItems.RAW_TIN.get()), RecipeCategory.MISC,
                        ModItems.TIN_INGOT.get(), SMELT_XP, SMELT_TICKS)
                .unlockedBy("has_raw_tin", has(ModItems.RAW_TIN.get()))
                .save(recipeOutput, id("tin_ingot_from_smelting_raw_tin"));
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ModItems.RAW_TIN.get()), RecipeCategory.MISC,
                        ModItems.TIN_INGOT.get(), SMELT_XP, BLAST_TICKS)
                .unlockedBy("has_raw_tin", has(ModItems.RAW_TIN.get()))
                .save(recipeOutput, id("tin_ingot_from_blasting_raw_tin"));
    }

    /** Regular furnace + blast furnace for dusts that smelt at normal heat. */
    private void dustSmelting(RecipeOutput output, ItemLike dust, ItemLike ingot, String material) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(dust), RecipeCategory.MISC, ingot, SMELT_XP, SMELT_TICKS)
                .unlockedBy("has_" + material + "_dust", has(dust))
                .save(output, id(material + "_ingot_from_smelting_" + material + "_dust"));
        dustBlasting(output, dust, ingot, material);
    }

    private void dustBlasting(RecipeOutput output, ItemLike dust, ItemLike ingot, String material) {
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(dust), RecipeCategory.MISC, ingot, SMELT_XP, BLAST_TICKS)
                .unlockedBy("has_" + material + "_dust", has(dust))
                .save(output, id(material + "_ingot_from_blasting_" + material + "_dust"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GtbwCore.MODID, path);
    }
}
