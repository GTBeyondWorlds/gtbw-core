package com.gtbeyondworlds.gtbwcore.registry;

import com.gtbeyondworlds.gtbwcore.GtbwCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Creative mode tabs registered by GTBW Core. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GtbwCore.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.gtbwcore.main"))
                    .icon(() -> ModItems.STEEL_INGOT.get().getDefaultInstance())
                    // Iterate the deferred register instead of listing entries by hand so new
                    // content shows up automatically; display order is registration order.
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
