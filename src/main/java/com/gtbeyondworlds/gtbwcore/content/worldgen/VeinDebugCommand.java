package com.gtbeyondworlds.gtbwcore.content.worldgen;

import java.util.List;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinInstance;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinMath;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinRegistry;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Op-only debug command reporting the vein of the region the player stands
 * in — the development-time stand-in for future prospecting tools. Messages
 * are plain literals on purpose: this is a dev tool, not player content.
 */
@EventBusSubscriber(modid = GtbwCore.MODID)
public final class VeinDebugCommand {

    private VeinDebugCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gtbwvein")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(VeinDebugCommand::report));
    }

    private static int report(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        List<VeinType> types = VeinRegistry.types();
        if (types.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No vein types are registered."));
            return 0;
        }
        long seed = level.getSeed();
        int regionX = VeinMath.regionFromBlock(player.getBlockX());
        int regionZ = VeinMath.regionFromBlock(player.getBlockZ());
        int index = VeinMath.selectTypeIndex(seed, regionX, regionZ, types);
        VeinInstance vein = VeinMath.instance(seed, regionX, regionZ, types.get(index));
        double distance = Math.hypot(player.getBlockX() - vein.centerX(),
                player.getBlockZ() - vein.centerZ());
        String note = level.dimension() == Level.OVERWORLD
                ? ""
                : " (veins only generate in the overworld)";
        String message = String.format(
                "Vein '%s' in region (%d, %d): center (%d, %d, %d), radius %d, thickness %d,"
                        + " richness %.0f%%, %.0f blocks away horizontally%s",
                vein.type().name(), regionX, regionZ,
                vein.centerX(), vein.centerY(), vein.centerZ(),
                vein.radius(), vein.thickness(), vein.richness() * 100.0, distance, note);
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }
}
