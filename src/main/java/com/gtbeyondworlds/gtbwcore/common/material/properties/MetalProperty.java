package com.gtbeyondworlds.gtbwcore.common.material.properties;

import com.gtbeyondworlds.gtbwcore.common.material.BWMaterialBuilder;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MetalProperty extends BWMaterialProperty {

    @Override
    public void verifyProperties(BWMaterialBuilder builder) {
        if (!builder.containsProperty(BWPropertyKeys.INGOT_KEY)) {
            builder.addIngotProp();
        }

        if (!builder.containsProperty(BWPropertyKeys.DUST_KEY)) {
            builder.addDustProp();
        }
    }

    @Override
    public String getPropertyId() {
        return BWPropertyKeys.METAL_KEY;
    }

}
