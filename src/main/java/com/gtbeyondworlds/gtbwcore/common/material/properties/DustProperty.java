package com.gtbeyondworlds.gtbwcore.common.material.properties;

import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DustProperty extends BWItemProperty {
    public DustProperty() {
        super(new ItemLike[3]);
    }

    @Override
    public void onMaterialRegistered(BWMaterial material) {
        registerSimpleItem(material, "dust", 0);
        registerSimpleItem(material, "dust_small", 1);
        registerSimpleItem(material, "dust_tiny", 2);
    }

    @Override
    public String getPropertyId() {
        return BWPropertyKeys.DUST_KEY;
    }

    public ItemLike getDustItem () {
        return this.getItem(0);
    }

    public ItemLike getSmallDustItem () {
        return this.getItem(1);
    }

    public ItemLike getTinyDustItem () {
        return this.getItem(2);
    }
}
