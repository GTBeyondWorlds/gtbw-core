package com.gtbeyondworlds.gtbwcore.common.material.properties;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import com.gtbeyondworlds.gtbwcore.common.material.item.MaterialItem;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public abstract class BWItemProperty extends BWMaterialProperty {
    @Getter
    private final ItemLike[] items;

    protected BWItemProperty(final ItemLike[] items) {
        this.items = items;
    }

    protected void registerSimpleItem (BWMaterial material, String itemName) {
        registerSimpleItem(material, itemName, 0);
    }

    protected void registerSimpleItem (BWMaterial material, String itemName, int itemIdx) {
        String materialId = material.getMaterialId();

        BWRegistries.ITEM_REGISTRY.registerDeferred(materialId + "_" + itemName, () -> {
            Item item = new MaterialItem(material, "item." + GtbwCore.MOD_ID + "." + itemName);
            items[itemIdx] = item;

            return item;
        });
    }

    public ItemLike getItem() {
        return this.getItem(0);
    }

    public ItemLike getItem(int itemIdx) {
        return this.items[itemIdx];
    }

}
