package com.gtbeyondworlds.gtbwcore.client;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.client.material.BWMaterialColorHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
@Mod(value = GtbwCore.MOD_ID, dist = Dist.CLIENT)
public class GtbwClient {
    public static final Logger LOGGER = LogUtils.getLogger();

    public GtbwClient(IEventBus bus) {
        bus.addListener(BWMaterialColorHandler::registerItemColors);
    }
}
