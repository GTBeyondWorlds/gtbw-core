package com.gtbeyondworlds.gtbwcore.api.item;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

public class BWCreativeModeTabs {
    public static final CreativeModeTab MATERIALS_TAB = registerTab("materials", CreativeModeTab.builder()
            .icon(Items.IRON_INGOT::getDefaultInstance).title(getTitle("materials"))
            .displayItems((params, output) ->
                    BWRegistries.MATERIAL_REGISTRY.getValues().forEach(material ->
                            material.getRegisteredItems().forEach(output::accept))).build());

    public static void init () {
        // static init
    }

    private static CreativeModeTab registerTab(String id, CreativeModeTab tab) {
        return BWRegistries.CREATIVE_TAB_REGISTRY.register(id, tab);
    }

    private static Component getTitle (String id) {
        return Component.translatable("itemGroup." + GtbwCore.MOD_ID + "." + id);
    }
}
