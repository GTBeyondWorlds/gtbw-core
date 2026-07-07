package com.gtbeyondworlds.gtbwcore.common.material.properties;

import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterialBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class IngotProperty extends BWItemProperty {
    public IngotProperty() {
        super(new ItemLike[1]);
    }

    @Override
    public void verifyProperties(BWMaterialBuilder builder) {
        if (!builder.containsProperty(BWPropertyKeys.DUST_KEY)) {
            builder.addDustProp();
        }
    }

    @Override
    public void onMaterialRegistered(BWMaterial material) {
        registerSimpleItem(material, "ingot");
    }

    @Override
    public String getPropertyId() {
        return BWPropertyKeys.INGOT_KEY;
    }

    public ItemLike getIngotItem () {
        return this.getItem(0);
    }
}
