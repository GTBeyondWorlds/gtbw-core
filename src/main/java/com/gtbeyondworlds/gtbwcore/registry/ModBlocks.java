package com.gtbeyondworlds.gtbwcore.registry;

import java.util.function.Function;

import com.gtbeyondworlds.gtbwcore.GtbwCore;
import com.gtbeyondworlds.gtbwcore.api.multiblock.MultiblockPartBlock;
import com.gtbeyondworlds.gtbwcore.content.machine.blastfurnace.BrickedBlastFurnaceBlock;
import com.gtbeyondworlds.gtbwcore.content.machine.cokeoven.CokeOvenBlock;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * All blocks registered by GTBW Core.
 *
 * <p>Blocks that exist in item form go through {@link #registerWithItem}, which
 * also creates the {@link net.minecraft.world.item.BlockItem} — one line per block.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GtbwCore.MODID);

    // Structure blocks; parts ping nearby controllers so structures form and
    // un-form automatically when they are placed or broken.
    public static final DeferredBlock<MultiblockPartBlock> COKE_OVEN_BRICKS =
            registerWithItem("coke_oven_bricks", MultiblockPartBlock::new, structureProperties(MapColor.TERRACOTTA_BROWN));
    public static final DeferredBlock<MultiblockPartBlock> BRICKED_BLAST_FURNACE_BRICKS =
            registerWithItem("bricked_blast_furnace_bricks", MultiblockPartBlock::new, structureProperties(MapColor.TERRACOTTA_ORANGE));

    // Multiblock controllers
    public static final DeferredBlock<CokeOvenBlock> COKE_OVEN =
            registerWithItem("coke_oven", CokeOvenBlock::new, structureProperties(MapColor.TERRACOTTA_BROWN));
    public static final DeferredBlock<BrickedBlastFurnaceBlock> BRICKED_BLAST_FURNACE =
            registerWithItem("bricked_blast_furnace", BrickedBlastFurnaceBlock::new, structureProperties(MapColor.TERRACOTTA_ORANGE));

    // Ores. Placed exclusively by the vein worldgen (api.worldgen.vein);
    // the deepslate form is chosen by the rock each ore block replaces.
    public static final DeferredBlock<DropExperienceBlock> TIN_ORE =
            registerWithItem("tin_ore", props -> new DropExperienceBlock(ConstantInt.of(0), props),
                    oreProperties(3.0f, MapColor.STONE, SoundType.STONE));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TIN_ORE =
            registerWithItem("deepslate_tin_ore", props -> new DropExperienceBlock(ConstantInt.of(0), props),
                    oreProperties(4.5f, MapColor.DEEPSLATE, SoundType.DEEPSLATE));

    private ModBlocks() {}

    private static BlockBehaviour.Properties structureProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(2.0f, 8.0f)
                .sound(SoundType.NETHER_BRICKS)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties oreProperties(float hardness, MapColor color, SoundType sound) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(hardness, 3.0f)
                .sound(sound)
                .requiresCorrectToolForDrops();
    }

    private static <T extends Block> DeferredBlock<T> registerWithItem(
            String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, factory, properties);
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
