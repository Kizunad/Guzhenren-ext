package com.Kizunad.guzhenrenext.xianqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

/**
 * 仙窍边界判定统一服务。
 * <p>
 * 该服务以 {@link ApertureInfo} 中的 min/max chunk 为唯一真源，
 * 对外提供方块与区块两个维度的统一边界判定入口，避免调用方重复手写半径距离公式。
 * </p>
 */
public final class ApertureBoundaryService {

    /** 一个区块对应的方块边长。 */
    private static final int CHUNK_BLOCK_SIZE = 16;

    /** 区块内最大相对坐标偏移（0~15）。 */
    private static final int CHUNK_MAX_OFFSET = CHUNK_BLOCK_SIZE - 1;

    private ApertureBoundaryService() {
    }

    /**
     * 判定目标区块是否位于仙窍边界内（包含边界）。
     *
     * @param info 仙窍信息
     * @param chunkX 目标区块 X
     * @param chunkZ 目标区块 Z
     * @return 位于边界内返回 true，否则返回 false
     */
    public static boolean containsChunk(ApertureInfo info, int chunkX, int chunkZ) {
        return chunkX >= info.minChunkX()
            && chunkX <= info.maxChunkX()
            && chunkZ >= info.minChunkZ()
            && chunkZ <= info.maxChunkZ();
    }

    /**
     * 判定目标方块是否位于仙窍边界内（包含边界）。
     * <p>
     * 实现先将方块坐标映射到区块，再复用区块级统一入口，
     * 保证所有调用方遵循同一套边界真源语义。
     * </p>
     *
     * @param info 仙窍信息
     * @param blockPos 目标方块坐标
     * @return 位于边界内返回 true，否则返回 false
     */
    public static boolean containsBlock(ApertureInfo info, BlockPos blockPos) {
        int chunkX = SectionPos.blockToSectionCoord(blockPos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(blockPos.getZ());
        return containsChunk(info, chunkX, chunkZ);
    }

    /**
     * 按“方块缓冲”扩展后判定区块是否命中。
     * <p>
     * 该方法用于需要边界缓冲区的调用方：
     * 先将缓冲方块数向上折算为区块扩展量，再在扩展后的边界上做区块匹配。
     * </p>
     *
     * @param info 仙窍信息
     * @param chunkX 目标区块 X
     * @param chunkZ 目标区块 Z
     * @param bufferBlocks 方块缓冲值（可为 0）
     * @return 命中扩展边界返回 true，否则返回 false
     */
    public static boolean containsChunkWithBlockBuffer(
        ApertureInfo info,
        int chunkX,
        int chunkZ,
        int bufferBlocks
    ) {
        int normalizedBuffer = Math.max(0, bufferBlocks);
        int chunkBuffer = Math.floorDiv(normalizedBuffer + CHUNK_MAX_OFFSET, CHUNK_BLOCK_SIZE);
        return chunkX >= info.minChunkX() - chunkBuffer
            && chunkX <= info.maxChunkX() + chunkBuffer
            && chunkZ >= info.minChunkZ() - chunkBuffer
            && chunkZ <= info.maxChunkZ() + chunkBuffer;
    }

    /**
     * 计算目标方块到仙窍边界（XZ 平面矩形边界）的“越界距离平方”。
     * <p>
     * 结果语义：
     * 1) 在边界内（含边界）返回 0；
     * 2) 在边界外返回到最近边界点的欧氏距离平方。
     * </p>
     *
     * @param info 仙窍信息
     * @param blockPos 目标方块坐标
     * @return 越界距离平方
     */
    public static long getOutsideDistanceSquared(ApertureInfo info, BlockPos blockPos) {
        int minBlockX = SectionPos.sectionToBlockCoord(info.minChunkX());
        int maxBlockX = SectionPos.sectionToBlockCoord(info.maxChunkX()) + CHUNK_MAX_OFFSET;
        int minBlockZ = SectionPos.sectionToBlockCoord(info.minChunkZ());
        int maxBlockZ = SectionPos.sectionToBlockCoord(info.maxChunkZ()) + CHUNK_MAX_OFFSET;

        long deltaX = computeAxisOutsideDistance(blockPos.getX(), minBlockX, maxBlockX);
        long deltaZ = computeAxisOutsideDistance(blockPos.getZ(), minBlockZ, maxBlockZ);
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    /**
     * 计算单轴上点到闭区间边界的越界距离。
     */
    private static long computeAxisOutsideDistance(int value, int minInclusive, int maxInclusive) {
        if (value < minInclusive) {
            return (long) minInclusive - value;
        }
        if (value > maxInclusive) {
            return (long) value - maxInclusive;
        }
        return 0L;
    }
}
