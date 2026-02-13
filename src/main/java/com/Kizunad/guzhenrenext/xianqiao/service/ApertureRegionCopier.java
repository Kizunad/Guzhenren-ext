package com.Kizunad.guzhenrenext.xianqiao.service;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 仙窍区域精确复制工具。
 * <p>
 * 该工具仅负责在同一 {@link ServerLevel} 内复制方块与方块实体数据（NBT），
 * 不涉及实体、计划刻、光照等世界运行时系统数据。
 * 设计目标是为“仙窍吞并/细采样”提供可复用且可预测的基础能力。
 * </p>
 */
public final class ApertureRegionCopier {

    /** 方块放置更新标志：仅同步客户端，不触发邻居更新。 */
    private static final int BLOCK_UPDATE_FLAGS = Block.UPDATE_CLIENTS;

    /** 合法体积边长最小值。 */
    private static final int MIN_SIZE = 1;

    private ApertureRegionCopier() {
    }

    /**
     * 将源区域按相对偏移复制到目标锚点。
     * <p>
     * 复制规则：
     * 1) 仅复制 {@link BlockState}；
     * 2) 若源方块存在 {@link BlockEntity}，则通过 {@code saveWithoutMetadata/loadWithComponents}
     *    完成 NBT 转移；
     * 3) 放置标志为 {@link Block#UPDATE_CLIENTS}；
     * 4) 不复制实体、ScheduledTick、Light。
     * </p>
     *
     * @param level 同一仙窍维度内的服务端世界
     * @param sourceMin 源区域最小角（允许与 sourceMax 颠倒，方法内部会归一化）
     * @param sourceMax 源区域最大角（允许与 sourceMin 颠倒，方法内部会归一化）
     * @param targetAnchor 目标区域锚点（对应归一化后源区域最小角）
     */
    public static void copyRegion(ServerLevel level, BlockPos sourceMin, BlockPos sourceMax, BlockPos targetAnchor) {
        BlockPos normalizedMin = new BlockPos(
            Math.min(sourceMin.getX(), sourceMax.getX()),
            Math.min(sourceMin.getY(), sourceMax.getY()),
            Math.min(sourceMin.getZ(), sourceMax.getZ())
        );
        BlockPos normalizedMax = new BlockPos(
            Math.max(sourceMin.getX(), sourceMax.getX()),
            Math.max(sourceMin.getY(), sourceMax.getY()),
            Math.max(sourceMin.getZ(), sourceMax.getZ())
        );
        HolderLookup.Provider registries = level.registryAccess();

        for (int x = normalizedMin.getX(); x <= normalizedMax.getX(); x++) {
            for (int y = normalizedMin.getY(); y <= normalizedMax.getY(); y++) {
                for (int z = normalizedMin.getZ(); z <= normalizedMax.getZ(); z++) {
                    BlockPos sourcePos = new BlockPos(x, y, z);
                    BlockPos offset = sourcePos.subtract(normalizedMin);
                    BlockPos targetPos = targetAnchor.offset(offset);
                    BlockState sourceState = level.getBlockState(sourcePos);

                    BlockEntity sourceBlockEntity = level.getBlockEntity(sourcePos);
                    CompoundTag savedTag = null;
                    if (sourceBlockEntity != null) {
                        savedTag = sourceBlockEntity.saveWithoutMetadata(registries);
                    }

                    level.removeBlockEntity(targetPos);
                    level.setBlock(targetPos, sourceState, BLOCK_UPDATE_FLAGS);

                    if (savedTag != null) {
                        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
                        if (targetBlockEntity != null) {
                            targetBlockEntity.loadWithComponents(savedTag, registries);
                            targetBlockEntity.setChanged();
                        }
                    }
                }
            }
        }
    }

    /**
     * 以中心点和体积尺寸为输入，复制立方体/长方体区域。
     * <p>
     * 尺寸为“边长”语义：
     * - 最小边长为 1；
     * - 当边长为偶数时，中心会向负方向多覆盖 1 格（通过 floor(size / 2) 计算）。
     * </p>
     *
     * @param level 仙窍维度服务端世界
     * @param sourceCenter 源区域中心点
     * @param sizeX X 方向边长（至少为 1）
     * @param sizeY Y 方向边长（至少为 1）
     * @param sizeZ Z 方向边长（至少为 1）
     * @param targetCenter 目标区域中心点
     * @throws IllegalArgumentException 任一边长小于 1 时抛出
     */
    public static void copyRegionSized(
        ServerLevel level,
        BlockPos sourceCenter,
        int sizeX,
        int sizeY,
        int sizeZ,
        BlockPos targetCenter
    ) {
        if (sizeX < MIN_SIZE || sizeY < MIN_SIZE || sizeZ < MIN_SIZE) {
            throw new IllegalArgumentException("copyRegionSized 的 sizeX/sizeY/sizeZ 必须大于等于 1。");
        }

        int sourceHalfX = sizeX / 2;
        int sourceHalfY = sizeY / 2;
        int sourceHalfZ = sizeZ / 2;
        BlockPos sourceMin = sourceCenter.offset(-sourceHalfX, -sourceHalfY, -sourceHalfZ);
        BlockPos sourceMax = sourceMin.offset(sizeX - 1, sizeY - 1, sizeZ - 1);

        int targetHalfX = sizeX / 2;
        int targetHalfY = sizeY / 2;
        int targetHalfZ = sizeZ / 2;
        BlockPos targetAnchor = targetCenter.offset(-targetHalfX, -targetHalfY, -targetHalfZ);

        copyRegion(level, sourceMin, sourceMax, targetAnchor);
    }
}
