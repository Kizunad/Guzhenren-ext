package com.Kizunad.guzhenrenext.xianqiao.runtime;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import java.util.Objects;
import net.minecraft.core.BlockPos;

/**
 * 仙窍运行时边界解释服务。
 * <p>
 * 该服务只做一件事：基于 chunk 真值边界与方块坐标，推导运行时分区模型。
 * 不写回、不扩展、不重算存储边界，确保 runtime 语义与持久化真源解耦。
 * </p>
 */
public final class ApertureRuntimeBoundaryService {

    private static final int CHUNK_BLOCK_SIZE = 16;

    private static final int CHUNK_MAX_OFFSET = CHUNK_BLOCK_SIZE - 1;

    private static final int ZERO = 0;

    private ApertureRuntimeBoundaryService() {
    }

    /**
     * 基于 ApertureInfo 与 BlockPos 解析运行时分区。
     */
    public static ChaosZoneModel resolveZone(ApertureInfo info, BlockPos blockPos) {
        Objects.requireNonNull(info, "info");
        Objects.requireNonNull(blockPos, "blockPos");
        long outsideDistanceSquared = ApertureBoundaryService.getOutsideDistanceSquared(info, blockPos);
        return ChaosZoneModel.fromOutsideDistanceSquared(outsideDistanceSquared);
    }

    /**
     * 基于 ApertureInfo 与方块坐标解析运行时分区。
     */
    public static ChaosZoneModel resolveZone(ApertureInfo info, int blockX, int blockZ) {
        Objects.requireNonNull(info, "info");
        return resolveZone(
            info.minChunkX(),
            info.maxChunkX(),
            info.minChunkZ(),
            info.maxChunkZ(),
            blockX,
            blockZ
        );
    }

    /**
     * 基于 chunk 真值边界与方块坐标解析运行时分区（纯几何入口，便于单测）。
     */
    public static ChaosZoneModel resolveZone(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        int blockX,
        int blockZ
    ) {
        long outsideDistanceSquared = getOutsideDistanceSquared(
            minChunkX,
            maxChunkX,
            minChunkZ,
            maxChunkZ,
            blockX,
            blockZ
        );
        return ChaosZoneModel.fromOutsideDistanceSquared(outsideDistanceSquared);
    }

    /**
     * 计算目标方块到 chunk 真值边界矩形的越界距离平方。
     */
    public static long getOutsideDistanceSquared(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        int blockX,
        int blockZ
    ) {
        int normalizedMinChunkX = Math.min(minChunkX, maxChunkX);
        int normalizedMaxChunkX = Math.max(minChunkX, maxChunkX);
        int normalizedMinChunkZ = Math.min(minChunkZ, maxChunkZ);
        int normalizedMaxChunkZ = Math.max(minChunkZ, maxChunkZ);

        long minBlockX = chunkToMinBlock(normalizedMinChunkX);
        long maxBlockX = chunkToMaxBlock(normalizedMaxChunkX);
        long minBlockZ = chunkToMinBlock(normalizedMinChunkZ);
        long maxBlockZ = chunkToMaxBlock(normalizedMaxChunkZ);

        long deltaX = computeAxisOutsideDistance(blockX, minBlockX, maxBlockX);
        long deltaZ = computeAxisOutsideDistance(blockZ, minBlockZ, maxBlockZ);
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    private static long chunkToMinBlock(int chunkCoord) {
        return (long) chunkCoord * CHUNK_BLOCK_SIZE;
    }

    private static long chunkToMaxBlock(int chunkCoord) {
        return chunkToMinBlock(chunkCoord) + CHUNK_MAX_OFFSET;
    }

    private static long computeAxisOutsideDistance(long value, long minInclusive, long maxInclusive) {
        if (value < minInclusive) {
            return minInclusive - value;
        }
        if (value > maxInclusive) {
            return value - maxInclusive;
        }
        return ZERO;
    }
}
