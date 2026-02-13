package com.Kizunad.guzhenrenext.xianqiao.service;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

/**
 * 主世界地形粗采样工具。
 * <p>
 * 本工具仅复制方块状态（BlockState），不会复制 BlockEntity NBT、实体、计划刻与光照数据。
 * 其核心用途是在仙窍初始化等场景中，从主世界提取一块 16x16 的地表切片并投放到目标维度。
 * </p>
 */
public final class OverworldTerrainSampler {

    /** 单个采样块在 X/Z 方向的边长（一个 chunk）。 */
    private static final int SAMPLE_SIDE_LENGTH = 16;

    /** 采样最低层：地表下 4 格。 */
    private static final int SAMPLE_Y_BELOW_SURFACE = 4;

    /** 采样最高层：地表上 12 格。 */
    private static final int SAMPLE_Y_ABOVE_SURFACE = 12;

    /** biome 随机搜索最大尝试次数。 */
    private static final int MAX_BIOME_ATTEMPTS = 20;

    /** 随机搜索 chunk 坐标最小值（对应 -10000 方块）。 */
    private static final int MIN_RANDOM_CHUNK = -625;

    /** 随机搜索 chunk 坐标最大值（对应 +10000 方块）。 */
    private static final int MAX_RANDOM_CHUNK = 625;

    /** 方块更新标志：仅同步给客户端，不触发邻居更新。 */
    private static final int PLACE_UPDATE_FLAGS = Block.UPDATE_CLIENTS;

    /** chunk 边长常量，用于坐标对齐。 */
    private static final int CHUNK_SIZE = 16;

    private OverworldTerrainSampler() {
    }

    /**
     * 从主世界采样地形并放置到仙窍目标锚点。
     * <p>
     * 规则说明：
     * </p>
     * <ul>
     *     <li>若显式源锚点为空，则按 biome 进行随机 chunk 对齐搜索（最多 20 次）。</li>
     *     <li>地表高度使用 {@link Heightmap.Types#MOTION_BLOCKING_NO_LEAVES}。</li>
     *     <li>采样范围固定为 X/Z 16x16，Y 为地表下 4 到地表上 12，并对维度高度做 clamp。</li>
     *     <li>采样盒子最底层遇到流体或重力方块时，强制替换为石头。</li>
     *     <li>放置时仅写入 BlockState，更新标志使用 {@link Block#UPDATE_CLIENTS}。</li>
     * </ul>
     *
     * @param overworldLevel 主世界服务端世界
     * @param apertureLevel 仙窍目标服务端世界
     * @param targetAnchor 目标放置锚点（对应采样盒子最小角）
     * @param targetBiome 目标 biome（用于随机搜索源点时的中心点匹配）
     * @param explicitSourceAnchor 显式源锚点；为 null 时启用随机搜索
     * @param random 随机源
     * @return 成功返回 true；找不到可用源点或坐标越界时返回 false
     */
    public static boolean sampleAndPlace(
        ServerLevel overworldLevel,
        ServerLevel apertureLevel,
        BlockPos targetAnchor,
        Holder<Biome> targetBiome,
        @Nullable BlockPos explicitSourceAnchor,
        RandomSource random
    ) {
        @Nullable BlockPos sourceAnchor = explicitSourceAnchor;
        if (sourceAnchor == null) {
            sourceAnchor = findBiomeLocation(overworldLevel, targetBiome, random);
        }

        if (sourceAnchor == null) {
            return false;
        }

        int alignedSourceMinX = alignToChunkMin(sourceAnchor.getX());
        int alignedSourceMinZ = alignToChunkMin(sourceAnchor.getZ());
        int centerX = alignedSourceMinX + CHUNK_SIZE / 2;
        int centerZ = alignedSourceMinZ + CHUNK_SIZE / 2;
        int surfaceY = overworldLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;

        int sourceMinY = Math.max(overworldLevel.getMinBuildHeight(), surfaceY - SAMPLE_Y_BELOW_SURFACE);
        int sourceMaxY = Math.min(overworldLevel.getMaxBuildHeight() - 1, surfaceY + SAMPLE_Y_ABOVE_SURFACE);
        if (sourceMinY > sourceMaxY) {
            return false;
        }

        int sampleHeight = sourceMaxY - sourceMinY + 1;
        for (int xOffset = 0; xOffset < SAMPLE_SIDE_LENGTH; xOffset++) {
            for (int zOffset = 0; zOffset < SAMPLE_SIDE_LENGTH; zOffset++) {
                for (int yOffset = 0; yOffset < sampleHeight; yOffset++) {
                    int sourceX = alignedSourceMinX + xOffset;
                    int sourceY = sourceMinY + yOffset;
                    int sourceZ = alignedSourceMinZ + zOffset;
                    BlockPos sourcePos = new BlockPos(sourceX, sourceY, sourceZ);

                    BlockState sourceState = overworldLevel.getBlockState(sourcePos);
                    boolean isBottomLayer = sourceY == sourceMinY;
                    BlockState placeState = transformBottomLayerStateIfNeeded(sourceState, isBottomLayer);

                    BlockPos targetPos = targetAnchor.offset(xOffset, yOffset, zOffset);
                    if (
                        targetPos.getY() < apertureLevel.getMinBuildHeight()
                            || targetPos.getY() >= apertureLevel.getMaxBuildHeight()
                    ) {
                        return false;
                    }
                    apertureLevel.setBlock(targetPos, placeState, PLACE_UPDATE_FLAGS);
                }
            }
        }
        return true;
    }

    /**
     * 在主世界内按 biome 匹配随机查找一个 chunk 对齐源锚点。
     *
     * @param overworldLevel 主世界
     * @param targetBiome 目标 biome
     * @param random 随机源
     * @return 找到时返回源锚点（chunk 最小角）；超过最大尝试次数返回 null
     */
    @Nullable
    private static BlockPos findBiomeLocation(
        ServerLevel overworldLevel,
        Holder<Biome> targetBiome,
        RandomSource random
    ) {
        for (int attempt = 0; attempt < MAX_BIOME_ATTEMPTS; attempt++) {
            int randomChunkX = random.nextIntBetweenInclusive(MIN_RANDOM_CHUNK, MAX_RANDOM_CHUNK);
            int randomChunkZ = random.nextIntBetweenInclusive(MIN_RANDOM_CHUNK, MAX_RANDOM_CHUNK);
            int chunkMinX = randomChunkX * CHUNK_SIZE;
            int chunkMinZ = randomChunkZ * CHUNK_SIZE;
            int centerX = chunkMinX + CHUNK_SIZE / 2;
            int centerZ = chunkMinZ + CHUNK_SIZE / 2;
            int centerY = overworldLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
            BlockPos centerPos = new BlockPos(centerX, centerY, centerZ);

            Holder<Biome> centerBiome = overworldLevel.getBiome(centerPos);
            if (isSameBiome(centerBiome, targetBiome)) {
                return new BlockPos(chunkMinX, centerY, chunkMinZ);
            }
        }
        return null;
    }

    /**
     * 将任意 X/Z 坐标对齐到所在 chunk 的最小角（16 边界）。
     *
     * @param value 原始坐标
     * @return 对齐后的 chunk 最小角坐标
     */
    private static int alignToChunkMin(int value) {
        return Math.floorDiv(value, CHUNK_SIZE) * CHUNK_SIZE;
    }

    /**
     * 对采样盒子底层方块执行替换规则。
     *
     * @param state 原始方块状态
     * @param isBottomLayer 是否处于采样盒子最底层
     * @return 处理后的方块状态
     */
    private static BlockState transformBottomLayerStateIfNeeded(BlockState state, boolean isBottomLayer) {
        if (!isBottomLayer) {
            return state;
        }

        boolean hasFluid = !state.getFluidState().isEmpty();
        boolean isFallingBlock = state.getBlock() instanceof FallingBlock;
        if (hasFluid || isFallingBlock) {
            return Blocks.STONE.defaultBlockState();
        }
        return state;
    }

    /**
     * 判断两个 biome holder 是否表示同一个 biome。
     * <p>
     * 优先比较资源键，若任一侧无键（例如直接 holder）则回退到 biome 实例比较。
     * </p>
     *
     * @param left 左侧 biome holder
     * @param right 右侧 biome holder
     * @return 同一 biome 返回 true，否则 false
     */
    private static boolean isSameBiome(Holder<Biome> left, Holder<Biome> right) {
        Optional<ResourceKey<Biome>> leftKey = left.unwrapKey();
        Optional<ResourceKey<Biome>> rightKey = right.unwrapKey();
        if (leftKey.isPresent() && rightKey.isPresent()) {
            return leftKey.get().equals(rightKey.get());
        }
        return left.value().equals(right.value());
    }
}
