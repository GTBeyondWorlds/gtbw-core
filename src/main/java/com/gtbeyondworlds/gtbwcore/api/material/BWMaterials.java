package com.gtbeyondworlds.gtbwcore.api.material;

import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterialBuilder;

public class BWMaterials {
    public static BWMaterial TIN = registerMaterial(BWMaterialBuilder.create("tin")
            .setTintColor(0xfafeff).addDustProp().addIngotProp().build());

    public static BWMaterial BRONZE = registerMaterial(BWMaterialBuilder.create("bronze")
            .setTintColor(0xffc370).addDustProp().addIngotProp().build());

    public static void init () {
        // static init
    }

    private static BWMaterial registerMaterial (BWMaterial material) {
        return BWRegistries.MATERIAL_REGISTRY.register(material.getMaterialId(), material);
    }
}
