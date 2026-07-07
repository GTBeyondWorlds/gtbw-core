package com.gtbeyondworlds.gtbwcore.common.registry;

import com.google.common.collect.ImmutableSet;
import com.gtbeyondworlds.gtbwcore.GtbwCore;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BWRegistry<T> {
    private final Map<ResourceKey<T>, Supplier<T>> deferredEntries = new LinkedHashMap<>();
    private final Map<ResourceKey<T>, T> entries = new LinkedHashMap<>();

    private final ResourceKey<? extends Registry<T>> registryKey;

    private boolean hasRegistered = false;
    private final String namespace;

    public BWRegistry(String namespace, ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public BWRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        this(GtbwCore.MOD_ID, registryKey);
    }

    public T register(final String name, T obj) {
        return register(ResourceLocation.fromNamespaceAndPath(this.namespace, name), obj);
    }

    public T register(final ResourceLocation location, T obj) {
        return register(ResourceKey.create(this.registryKey, location), obj);
    }

    public T register(final ResourceKey<T> key, T obj) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(obj);

        if (deferredEntries.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate registration" + key);
        }

        return internalRegister(key, obj, false);
    }

    public void registerDeferred(final String name, Supplier<T> sup) {
        registerDeferred(ResourceLocation.fromNamespaceAndPath(this.namespace, name), sup);
    }

    public void registerDeferred(final ResourceLocation location, Supplier<T> sup) {
        registerDeferred(ResourceKey.create(this.registryKey, location), sup);
    }

    // deferred values are created only when the RegisterEvent fires
    public void registerDeferred(final ResourceKey<T> key, Supplier<T> sup) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(sup);

        if (entries.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate registration" + key);
        }

        if (deferredEntries.putIfAbsent(key, sup) != null) {
            throw new IllegalArgumentException("Duplicate registration " + key);
        }
    }

    @SuppressWarnings("unchecked")
    public void addEntries(RegisterEvent event) {
        if (!event.getRegistryKey().equals(this.registryKey)) {
            return;
        }

        Registry<T> registry = (Registry<T>) event.getRegistry();

        // add deferred to the normal entries
        for (var e : this.deferredEntries.entrySet()) {
            internalRegister(e.getKey(), e.getValue().get(), true);
        }

        // register the normal entries
        for (var e : this.entries.entrySet()) {
            GtbwCore.LOGGER.info("Registering {}", e.getKey());
            Registry.register(registry, e.getKey(), e.getValue());
        }

        this.deferredEntries.clear();
        this.hasRegistered = true;
    }

    private T internalRegister (final ResourceKey<T> key, T val, boolean isDeferred) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(val);

        if (this.hasRegistered) {
            throw new IllegalStateException("Cannot register new entries after RegisterEvent has been fired.");
        }

        if (this.entries.putIfAbsent(key, val) != null) {
            throw new IllegalArgumentException("Duplicate registration " + key);
        }

        onRegistered(key, val, isDeferred);
        return val;
    }

    @SuppressWarnings("unchecked")
    private void onRegistered (final ResourceKey<T> key, T obj, boolean isDeferred) {
        if (obj instanceof IBWRegistryObject<?> registryObject) {
            ((IBWRegistryObject<T>) registryObject).onRegistered(key, obj, isDeferred);
        }
    }

    public Set<Map.Entry<ResourceKey<T>, T>> getEntries() {
        if (!this.hasRegistered) {
            throw new IllegalStateException("Cannot get registries before RegisterEvent fires");
        }

        return ImmutableSet.copyOf(this.entries.entrySet());
    }

    public Set<T> getValues () {
        if (!this.hasRegistered) {
            throw new IllegalStateException("Cannot get registries before RegisterEvent fires");
        }

        return ImmutableSet.copyOf(this.entries.values());
    }

    public T getEntry(final String id) {
        return getEntry(ResourceLocation.fromNamespaceAndPath(this.namespace, id));
    }

    public T getEntry(final ResourceLocation location) {
        return getEntry(ResourceKey.create(this.registryKey, location));
    }

    public T getEntry(final ResourceKey<T> key) {
        if (!this.hasRegistered) {
            throw new IllegalStateException("Cannot get registries before RegisterEvent fires");
        }

        return this.entries.get(key);
    }
}
