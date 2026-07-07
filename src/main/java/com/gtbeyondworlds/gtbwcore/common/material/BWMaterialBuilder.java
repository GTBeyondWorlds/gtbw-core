package com.gtbeyondworlds.gtbwcore.common.material;

import com.gtbeyondworlds.gtbwcore.common.material.properties.BWMaterialProperty;
import com.gtbeyondworlds.gtbwcore.common.material.properties.DustProperty;
import com.gtbeyondworlds.gtbwcore.common.material.properties.IngotProperty;
import com.gtbeyondworlds.gtbwcore.common.material.properties.MetalProperty;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@MethodsReturnNonnullByDefault
public class BWMaterialBuilder {
    private final Map<String, BWMaterialProperty> props = new HashMap<>();
    private final BWMaterialInfo info;

    @Getter @Setter
    private String materialId;

    public BWMaterialBuilder(String materialId) {
        this.info = new BWMaterialInfo(materialId);
        this.materialId = materialId;
    }

    public static BWMaterialBuilder create(String materialId) {
        return new BWMaterialBuilder(materialId);
    }

    public BWMaterialBuilder setTintColor(int tintColor) {
        this.info.setTintColor(tintColor);
        return this;
    }

    public BWMaterialBuilder addIngotProp () {
        return this.addProperty(new IngotProperty());
    }

    public BWMaterialBuilder addDustProp() {
        return this.addProperty(new DustProperty());
    }

    public BWMaterialBuilder addMetalProp() {
        return this.addProperty(new MetalProperty());
    }

    public BWMaterialBuilder addProperty(BWMaterialProperty prop) {
        this.props.put(prop.getPropertyId(), prop);
        prop.verifyProperties(this);

        return this;
    }

    public boolean containsProperty(String propKey) {
        return this.props.containsKey(propKey);
    }

    public BWMaterial build() {
        return new BWMaterial(this.materialId, this.info, this.props);
    }
}
