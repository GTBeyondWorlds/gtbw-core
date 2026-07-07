package com.gtbeyondworlds.gtbwcore.client.material;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import com.gtbeyondworlds.gtbwcore.client.GtbwClient;
import com.gtbeyondworlds.gtbwcore.common.material.BWMaterial;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value=Dist.CLIENT, modid = GtbwCore.MOD_ID)
public class BWMaterialColorHandler {

    @SubscribeEvent
    public static void registerItemColors (RegisterColorHandlersEvent.Item event) {
        GtbwClient.LOGGER.info("Registering Material Colors");

        // TODO: REPLACE WITH BAKING INJECTION
        for (BWMaterial material : BWRegistries.MATERIAL_REGISTRY.getValues()) {
            Set<ItemLike> itemSet = material.getRegisteredItems();
            ItemLike[] itemArr = itemSet.toArray(ItemLike[]::new);

            event.register((itemStack, tintIdx) -> material.getTintColor(), itemArr);
        }
    }
}
