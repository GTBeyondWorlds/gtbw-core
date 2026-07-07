package com.gtbeyondworlds.gtbwcore.registry;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All worldgen features registered by GTBW Core. */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, GtbwCore.MODID);

    /** The per-chunk vein carver; configured/placed JSON comes from datagen. */
    public static final DeferredHolder<Feature<?>, VeinFeature> ORE_VEINS =
            FEATURES.register("ore_veins", VeinFeature::new);

    private ModFeatures() {}

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
