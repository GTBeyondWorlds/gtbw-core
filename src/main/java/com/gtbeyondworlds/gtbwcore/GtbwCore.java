package com.gtbeyondworlds.gtbwcore;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Main entrypoint for GTBW Core.
 *
 * <p>This is intentionally empty for now — it registers no content and only
 * confirms the mod loads. Features will be added later.
 */
@Mod(GtbwCore.MODID)
public class GtbwCore {
    /** The mod id. Must match the {@code mod_id} in {@code gradle.properties}. */
    public static final String MODID = "gtbwcore";

    /** Shared logger for the mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML recognizes parameter types like IEventBus and ModContainer and injects them automatically.
    public GtbwCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("GTBW Core loaded.");
    }
}
