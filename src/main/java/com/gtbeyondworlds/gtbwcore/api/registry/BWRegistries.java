package com.gtbeyondworlds.gtbwcore.api.registry;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.material.BWMaterials;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import com.gtbeyondworlds.gtbwcore.common.registry.BWRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(modid = GtbwCore.MOD_ID)
public class BWRegistries {
    private static final Set<Registry<?>> CUSTOM_REGISTRIES = new HashSet<>();
    private static boolean seenNewRegistryEvent = false;

    public static final BWRegistry<Item> ITEM_REGISTRY = fromBuiltinRegistry(Registries.ITEM);
    public static final BWRegistry<Block> BLOCK_REGISTRY = fromBuiltinRegistry(Registries.BLOCK);

    public static final BWRegistry<CreativeModeTab> CREATIVE_TAB_REGISTRY = fromBuiltinRegistry(Registries.CREATIVE_MODE_TAB);

    public static final BWRegistry<BWMaterial> MATERIAL_REGISTRY = newCustomRegistry("material");

    static {
        BWMaterials.init();
    }

    public static void init () {
        // static init
    }

    @SubscribeEvent
    private static void addEntries (RegisterEvent event) {
        ITEM_REGISTRY.addEntries(event);
        MATERIAL_REGISTRY.addEntries(event);
    }

    @SubscribeEvent
    private static void addCustomRegistries (NewRegistryEvent event) {
        seenNewRegistryEvent = true;

        GtbwCore.LOGGER.info("Adding custom registries.");

        for (Registry<?> registry : CUSTOM_REGISTRIES) {
            event.register(registry);
        }
    }

    private static <T> BWRegistry<T> newCustomRegistry (String registryName) {
        return newCustomRegistry(registryName, GtbwCore.MOD_ID, null);
    }

    private static <T> BWRegistry<T> newCustomRegistry (String registryName, final Consumer<RegistryBuilder<T>> builder) {
        return newCustomRegistry(registryName, GtbwCore.MOD_ID, builder);
    }

    private static <T> BWRegistry<T> newCustomRegistry (String registryName, String namespace) {
        return newCustomRegistry(registryName, namespace, null);
    }

    private static <T> BWRegistry<T> newCustomRegistry (String registryName, String namespace, @Nullable final Consumer<RegistryBuilder<T>> consumer) {
        ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(namespace, registryName);
        ResourceKey<Registry<T>> registryKey = ResourceKey.createRegistryKey(resource);

        BWRegistry<T> bwRegistry = new BWRegistry<>(namespace, registryKey);
        RegistryBuilder<T> builder = new RegistryBuilder<>(registryKey);

        if (BuiltInRegistries.REGISTRY.containsKey(resource))
            throw new IllegalStateException("Cannot create a registry that already exists - " + bwRegistry);

        if (seenNewRegistryEvent)
            throw new IllegalStateException("Cannot create a registry after NewRegistryEvent has fired");

        if (consumer != null)
            consumer.accept(builder);

        CUSTOM_REGISTRIES.add(builder.create());
        return bwRegistry;
    }

    private static <T> BWRegistry<T> fromBuiltinRegistry (ResourceKey<Registry<T>> key) {
        return fromBuiltinRegistry(key, GtbwCore.MOD_ID);
    }

    private static <T> BWRegistry<T> fromBuiltinRegistry (ResourceKey<Registry<T>> key, String namespace) {
        return new BWRegistry<>(namespace, key);
    }

}
