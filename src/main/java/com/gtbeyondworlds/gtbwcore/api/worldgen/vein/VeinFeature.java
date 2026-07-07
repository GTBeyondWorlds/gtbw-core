package com.gtbeyondworlds.gtbwcore.api.worldgen.vein;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Carves the current chunk's slice of every vein that reaches it. Runs once
 * per chunk (empty placement list); all coordination happens through
 * {@link VeinMath}'s determinism — this feature never writes outside its own
 * chunk, which keeps it trivially inside the worldgen write limits.
 */
public class VeinFeature extends Feature<NoneFeatureConfiguration> {

    public VeinFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        List<VeinDefinition> definitions = VeinRegistry.all();
        List<VeinType> types = VeinRegistry.types();
        if (definitions.isEmpty()) {
            return false;
        }
        WorldGenLevel level = context.level();
        long worldSeed = level.getSeed();
        ChunkPos chunk = new ChunkPos(context.origin());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        int regionX = VeinMath.regionFromChunk(chunk.x);
        int regionZ = VeinMath.regionFromChunk(chunk.z);
        boolean placedAny = false;

        // A vein's max reach (96) is less than a region (384), so only veins
        // of this region and its 8 neighbors can touch this chunk.
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int rx = regionX - 1; rx <= regionX + 1; rx++) {
            for (int rz = regionZ - 1; rz <= regionZ + 1; rz++) {
                int index = VeinMath.selectTypeIndex(worldSeed, rx, rz, types);
                if (index < 0) {
                    continue;
                }
                VeinInstance vein = VeinMath.instance(worldSeed, rx, rz, types.get(index));
                int reach = VeinMath.maxReachXZ(vein);
                if (vein.centerX() + reach < minX || vein.centerX() - reach > minX + 15
                        || vein.centerZ() + reach < minZ || vein.centerZ() - reach > minZ + 15) {
                    continue;
                }
                placedAny |= carve(level, definitions.get(index), vein, minX, minZ, pos);
            }
        }
        return placedAny;
    }

    /** Places this chunk's blocks of one vein; returns whether any were placed. */
    private static boolean carve(WorldGenLevel level, VeinDefinition definition, VeinInstance vein,
            int minX, int minZ, BlockPos.MutableBlockPos pos) {
        int reach = VeinMath.maxReachXZ(vein);
        int vReach = VeinMath.maxReachY(vein);
        int xLo = Math.max(minX, vein.centerX() - reach);
        int xHi = Math.min(minX + 15, vein.centerX() + reach);
        int zLo = Math.max(minZ, vein.centerZ() - reach);
        int zHi = Math.min(minZ + 15, vein.centerZ() + reach);
        int yLo = Math.max(vein.centerY() - vReach, level.getMinBuildHeight());
        int yHi = Math.min(vein.centerY() + vReach, level.getMaxBuildHeight() - 1);
        boolean placed = false;
        for (int x = xLo; x <= xHi; x++) {
            for (int z = zLo; z <= zHi; z++) {
                for (int y = yLo; y <= yHi; y++) {
                    int ore = VeinMath.rollOre(vein, x, y, z);
                    if (ore < 0) {
                        continue; // outside the lens or rolled rock — no block read needed
                    }
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    VeinDefinition.OreBlocks blocks = definition.ore(ore);
                    if (state.is(BlockTags.STONE_ORE_REPLACEABLES)) {
                        level.setBlock(pos, blocks.stone().get().defaultBlockState(), 2);
                        placed = true;
                    } else if (state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
                        level.setBlock(pos, blocks.deepslate().get().defaultBlockState(), 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
