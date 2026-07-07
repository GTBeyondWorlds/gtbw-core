package com.gtbeyondworlds.gtbwcore.common.material;

import com.gtbeyondworlds.gtbwcore.common.material.properties.*;
import com.gtbeyondworlds.gtbwcore.common.registry.IBWRegistryObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import net.minecraft.world.level.ItemLike;

import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BWMaterial implements IBWRegistryObject<BWMaterial> {
    private final Map<String, BWMaterialProperty> properties;
    private final BWMaterialInfo info;

    @Getter
    private final String materialId;

    protected BWMaterial(final String id, final BWMaterialInfo info, final Map<String, BWMaterialProperty> props) {
        this.properties = props;
        this.materialId = id;
        this.info = info;
    }

    @Override
    public void onRegistered(final ResourceKey<BWMaterial> key, BWMaterial material, boolean isDeferred) {
        if (isDeferred) {
            throw new IllegalStateException("Materials mustn't be registered deferred");
        }

        for (BWMaterialProperty prop : this.properties.values()) {
            prop.onMaterialRegistered(material);
        }
    }

    public Set<ItemLike> getRegisteredItems() {
        Set<ItemLike> items = new HashSet<>();

        for (BWMaterialProperty prop : this.properties.values()) {
            if (prop instanceof BWItemProperty itemProp) {
                items.addAll(Arrays.asList(itemProp.getItems()));
            }
        }

        return items;
    }

    public @Nullable BWMaterialProperty getProperty(String propKey) {
        return this.properties.get(propKey);
    }

    public Component getMaterialName() {
        return this.info.getName();
    }

    public int getTintColor () {
        return this.info.getTintColor();
    }

    public @Nullable ItemLike getIngotItem() {
        BWMaterialProperty prop = this.getProperty(BWPropertyKeys.INGOT_KEY);
        return prop instanceof IngotProperty ingotProperty ? ingotProperty.getIngotItem() : null;
    }

    public @Nullable ItemLike getDustItem() {
        BWMaterialProperty prop = this.getProperty(BWPropertyKeys.DUST_KEY);
        return prop instanceof DustProperty dustProperty ? dustProperty.getDustItem() : null;
    }

    public @Nullable ItemLike getSmallDustITem() {
        BWMaterialProperty prop = this.getProperty(BWPropertyKeys.DUST_KEY);
        return prop instanceof DustProperty dustProperty ? dustProperty.getSmallDustItem() : null;
    }

    public @Nullable ItemLike getTinyDustItem() {
        BWMaterialProperty prop = this.getProperty(BWPropertyKeys.DUST_KEY);
        return prop instanceof DustProperty dustProperty ? dustProperty.getTinyDustItem() : null;
    }

    @Override
    public String toString() {
        return this.materialId;
    }
}
