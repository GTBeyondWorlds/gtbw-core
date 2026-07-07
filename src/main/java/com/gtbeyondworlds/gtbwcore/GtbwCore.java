package com.gtbeyondworlds.gtbwcore;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.gtbeyondworlds.gtbwcore.registry.ModBlocks;
import com.gtbeyondworlds.gtbwcore.registry.ModCreativeTabs;
import com.gtbeyondworlds.gtbwcore.registry.ModFeatures;
import com.gtbeyondworlds.gtbwcore.registry.ModItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Main entrypoint for GTBW Core.
 *
 * <p>Registers the mod's content against the mod event bus. See
 * {@code docs/ARCHITECTURE.md} for the package layout and registration
 * conventions.
 */
@Mod(GtbwCore.MODID)
public class GtbwCore {
    /** The mod id. Must match the {@code mod_id} in {@code gradle.properties}. */
    public static final String MODID = "gtbwcore";

    /** Shared logger for the mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML recognizes parameter types like IEventBus and ModContainer and injects them automatically.
    public GtbwCore(IEventBus modEventBus, ModContainer modContainer) {
        // Blocks first: ModBlocks also registers the block items into ModItems.ITEMS.
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModFeatures.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("GTBW Core loaded.");
    }
}
