package com.gtbeyondworlds.gtbwcore.datagen;

import java.util.concurrent.CompletableFuture;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Common (`c:`) item tags so other mods can find our materials: each material
 * gets its own `c:dusts/x` / `c:ingots/x` tag, folded into the parent tag.
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, GtbwCore.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        materialTag(Tags.Items.DUSTS, "dusts/copper", ModItems.COPPER_DUST.get());
        materialTag(Tags.Items.DUSTS, "dusts/tin", ModItems.TIN_DUST.get());
        materialTag(Tags.Items.DUSTS, "dusts/bronze", ModItems.BRONZE_DUST.get());
        materialTag(Tags.Items.DUSTS, "dusts/steel", ModItems.STEEL_DUST.get());

        materialTag(Tags.Items.INGOTS, "ingots/tin", ModItems.TIN_INGOT.get());
        materialTag(Tags.Items.INGOTS, "ingots/bronze", ModItems.BRONZE_INGOT.get());
        materialTag(Tags.Items.INGOTS, "ingots/steel", ModItems.STEEL_INGOT.get());

        materialTag(Tags.Items.RAW_MATERIALS, "raw_materials/tin", ModItems.RAW_TIN.get());

        copy(BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/tin")),
                ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/tin")));
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
    }

    private void materialTag(TagKey<Item> parent, String path, Item item) {
        TagKey<Item> child = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
        tag(child).add(item);
        tag(parent).addTag(child);
    }
}
