package com.gtbeyondworlds.gtbwcore.content.worldgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinInstance;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinMath;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinRegistry;
import com.gtbeyondworlds.gtbwcore.api.worldgen.vein.VeinType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Op-only debug command for the vein of the region the player stands in —
 * the development-time stand-in for future prospecting tools. Bare
 * {@code /gtbwvein} reports the vein; {@code show} outlines its boundary with
 * invisible glowing shulkers (visible through terrain), {@code hide} removes
 * them. Markers auto-despawn after a minute and are tagged so {@code hide}
 * also cleans up any that survived a world save. Messages are plain literals
 * on purpose: this is a dev tool, not player content.
 */
@EventBusSubscriber(modid = GtbwCore.MODID)
public final class VeinDebugCommand {

    /** Entity command tag identifying our markers, for cleanup by tag. */
    private static final String MARKER_TAG = "gtbwvein_marker";

    /** Marker lifetime in ticks before auto-despawn. */
    private static final int MARKER_LIFETIME_TICKS = 20 * 60;

    /** Most boundary cells turned into markers per {@code show}. */
    private static final int MARKER_CAP = 600;

    /** Live markers awaiting expiry; server thread only. */
    private static final List<Marker> MARKERS = new ArrayList<>();

    private record Marker(ResourceKey<Level> dimension, UUID uuid, long expiryGameTime) {}

    private VeinDebugCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gtbwvein")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(VeinDebugCommand::report)
                .then(Commands.literal("show").executes(VeinDebugCommand::show))
                .then(Commands.literal("hide").executes(VeinDebugCommand::hide)));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (MARKERS.isEmpty() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        Iterator<Marker> iterator = MARKERS.iterator();
        while (iterator.hasNext()) {
            Marker marker = iterator.next();
            if (!marker.dimension().equals(level.dimension()) || now < marker.expiryGameTime()) {
                continue;
            }
            Entity entity = level.getEntity(marker.uuid());
            if (entity != null) {
                entity.discard();
            }
            iterator.remove();
        }
    }

    private static int report(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        List<VeinType> types = VeinRegistry.types();
        if (types.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No vein types are registered."));
            return 0;
        }
        VeinInstance vein = veinAt(level, player);
        int regionX = VeinMath.regionFromBlock(player.getBlockX());
        int regionZ = VeinMath.regionFromBlock(player.getBlockZ());
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

    private static int show(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        if (VeinRegistry.types().isEmpty()) {
            context.getSource().sendFailure(Component.literal("No vein types are registered."));
            return 0;
        }
        removeMarkers(level); // repeated /show replaces the outline instead of stacking it
        VeinInstance vein = veinAt(level, player);
        long expiry = level.getGameTime() + MARKER_LIFETIME_TICKS;
        int spawned = 0;
        int unloaded = 0;
        for (int[] cell : VeinMath.boundaryShell(vein, MARKER_CAP)) {
            BlockPos pos = new BlockPos(cell[0], cell[1], cell[2]);
            if (!level.isLoaded(pos)) {
                unloaded++;
                continue;
            }
            Shulker marker = EntityType.SHULKER.create(level);
            if (marker == null) {
                continue;
            }
            marker.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            marker.setNoAi(true);
            marker.setInvisible(true);
            marker.setGlowingTag(true);
            marker.setInvulnerable(true);
            marker.setSilent(true);
            marker.addTag(MARKER_TAG);
            level.addFreshEntity(marker);
            MARKERS.add(new Marker(level.dimension(), marker.getUUID(), expiry));
            spawned++;
        }
        String skipped = unloaded > 0
                ? String.format(" (%d cells skipped in unloaded chunks — move closer and rerun)", unloaded)
                : "";
        String message = String.format(
                "Outlined vein '%s' with %d glowing markers%s. Auto-clear in 60s, or /gtbwvein hide.",
                vein.type().name(), spawned, skipped);
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return spawned;
    }

    private static int hide(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        int removed = removeMarkers(level);
        String message = "Removed " + removed + " vein markers.";
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return removed;
    }

    /** Resolves the vein of the region the player stands in. */
    private static VeinInstance veinAt(ServerLevel level, ServerPlayer player) {
        List<VeinType> types = VeinRegistry.types();
        long seed = level.getSeed();
        int regionX = VeinMath.regionFromBlock(player.getBlockX());
        int regionZ = VeinMath.regionFromBlock(player.getBlockZ());
        int index = VeinMath.selectTypeIndex(seed, regionX, regionZ, types);
        return VeinMath.instance(seed, regionX, regionZ, types.get(index));
    }

    /** Discards every tagged marker in the level, including ones from saved worlds. */
    private static int removeMarkers(ServerLevel level) {
        // Snapshot first: discarding while iterating getAllEntities() mutates
        // the entity sections mid-iteration and the iterator yields nulls.
        List<Entity> found = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity != null && entity.getTags().contains(MARKER_TAG)) {
                found.add(entity);
            }
        }
        found.forEach(Entity::discard);
        MARKERS.removeIf(marker -> marker.dimension().equals(level.dimension()));
        return found.size();
    }
}
