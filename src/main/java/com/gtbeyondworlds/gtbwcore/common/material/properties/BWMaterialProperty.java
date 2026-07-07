package com.gtbeyondworlds.gtbwcore.common.material.properties;

import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterialBuilder;
import lombok.Getter;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
public abstract class BWMaterialProperty {

    public BWMaterialProperty ( ) { }

    public abstract String getPropertyId();

    public void verifyProperties(BWMaterialBuilder builder) { }

    public void onMaterialRegistered(BWMaterial material) { }

    @Override
    public int hashCode() {
        return this.getPropertyId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BWMaterialProperty prop) {
            return this.getPropertyId().equals(prop.getPropertyId());
        }

        return false;
    }
}
