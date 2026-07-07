package com.gtbeyondworlds.gtbwcore.common.registry;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface IBWRegistryObject<T> {
    // fires when this object gets registered on a BWRegistry
    void onRegistered(final ResourceKey<T> key, T obj, boolean isDeferred);
}
