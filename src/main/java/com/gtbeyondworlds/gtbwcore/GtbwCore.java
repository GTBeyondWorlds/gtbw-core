package com.gtbeyondworlds.gtbwcore;

import com.gtbeyondworlds.gtbwcore.api.material.BWMaterials;
import com.gtbeyondworlds.gtbwcore.api.registry.BWRegistries;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(GtbwCore.MOD_ID)
public class GtbwCore {
    public static final String MOD_ID = "gtbwcore";

    public static final Logger LOGGER = LogUtils.getLogger();

    public GtbwCore(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("GTBW-Core setup");

        BWRegistries.init();
    }
}
