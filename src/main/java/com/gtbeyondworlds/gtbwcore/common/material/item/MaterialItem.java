package com.gtbeyondworlds.gtbwcore.common.material.item;

import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public class MaterialItem extends Item {
    @Getter
    private final BWMaterial material;
    private final String itemName;

    public MaterialItem(BWMaterial material, String name) {
        this(material, name, new Item.Properties());
    }

    public MaterialItem(BWMaterial material, String name, Item.Properties props) {
        super(props);

        this.material = material;
        this.itemName = name;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.itemName, this.material.getMaterialName());
    }
}
