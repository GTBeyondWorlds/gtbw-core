package com.gtbeyondworlds.gtbwcore.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.registry.ModFeatures;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Worldgen JSON: the vein feature's configured/placed pair, the biome
 * modifier adding it to every overworld biome, and the modifier removing
 * vanilla scattered generation for ores that veins now cover (copper, iron,
 * coal — see the design record; uncovered ores keep vanilla gen).
 */
public class ModWorldgenProvider extends DatapackBuiltinEntriesProvider {

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_VEINS_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, id("ore_veins"));
    public static final ResourceKey<PlacedFeature> ORE_VEINS_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("ore_veins"));
    public static final ResourceKey<BiomeModifier> ADD_ORE_VEINS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_ore_veins"));
    public static final ResourceKey<BiomeModifier> REMOVE_REPLACED_VANILLA_ORES =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("remove_replaced_vanilla_ores"));

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, context -> context.register(ORE_VEINS_CONFIGURED,
                    new ConfiguredFeature<>(ModFeatures.ORE_VEINS.get(), NoneFeatureConfiguration.INSTANCE)))
            .add(Registries.PLACED_FEATURE, context -> context.register(ORE_VEINS_PLACED,
                    // No placement modifiers: the feature runs exactly once per
                    // chunk and does its own region math.
                    new PlacedFeature(
                            context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ORE_VEINS_CONFIGURED),
                            List.of())))
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                var biomes = context.lookup(Registries.BIOME);
                var placed = context.lookup(Registries.PLACED_FEATURE);
                context.register(ADD_ORE_VEINS, new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placed.getOrThrow(ORE_VEINS_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));
                context.register(REMOVE_REPLACED_VANILLA_ORES, new BiomeModifiers.RemoveFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(
                                placed.getOrThrow(OrePlacements.ORE_COPPER),
                                placed.getOrThrow(OrePlacements.ORE_COPPER_LARGE),
                                placed.getOrThrow(OrePlacements.ORE_IRON_UPPER),
                                placed.getOrThrow(OrePlacements.ORE_IRON_MIDDLE),
                                placed.getOrThrow(OrePlacements.ORE_IRON_SMALL),
                                placed.getOrThrow(OrePlacements.ORE_COAL_UPPER),
                                placed.getOrThrow(OrePlacements.ORE_COAL_LOWER)),
                        Set.of(GenerationStep.Decoration.UNDERGROUND_ORES)));
            });

    public ModWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(GtbwCore.MODID));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GtbwCore.MODID, path);
    }
}
