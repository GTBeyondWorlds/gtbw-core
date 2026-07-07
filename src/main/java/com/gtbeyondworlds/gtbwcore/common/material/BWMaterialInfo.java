package com.gtbeyondworlds.gtbwcore.common.material;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;

public class BWMaterialInfo {
    @Getter @Setter
    private int tintColor = -1;

    @Getter
    private final Component name;

    protected BWMaterialInfo (String materialId) {
        this.name = Component.translatable("material." + GtbwCore.MOD_ID + "." + materialId);
    }
}
