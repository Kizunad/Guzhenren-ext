package com.Kizunad.guzhenrenext.bastion.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 基地自动建造服务。
 * <p>
 * 负责根据蓝图将必需方块放置到指定位置，可选方块由玩家手动补全。
 * </p>
 */
public final class BastionBuildingService {

    private BastionBuildingService() {
    }

    /**
     * 在指定位置放置蓝图结构。
     *
     * @param level 世界
     * @param center 中心位置
     * @param blueprint 蓝图
     * @return 放置的方块数量
     */
    public static int placeBlueprint(Level level, BlockPos center, BastionBlueprint blueprint) {
        int placed = 0;
        for (BastionBlueprint.BlockEntry entry : blueprint.entries()) {
            if (!entry.required()) {
                continue;
            }

            BlockPos targetPos = center.offset(entry.x(), entry.y(), entry.z());
            level.setBlock(targetPos, entry.block().defaultBlockState(), PlacementConstants.BLOCK_UPDATE_FLAGS);
            placed++;
        }
        return placed;
    }

    /**
     * 检查是否可以放置蓝图（目标位置是否为空气或可替换）。
     *
     * @param level 世界
     * @param center 中心位置
     * @param blueprint 蓝图
     * @return 是否可放置
     */
    public static boolean canPlaceBlueprint(Level level, BlockPos center, BastionBlueprint blueprint) {
        for (BastionBlueprint.BlockEntry entry : blueprint.entries()) {
            if (!entry.required()) {
                continue;
            }

            BlockPos targetPos = center.offset(entry.x(), entry.y(), entry.z());
            BlockState current = level.getBlockState(targetPos);
            if (!current.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 放置相关常量，避免 MagicNumber。
     */
    private static final class PlacementConstants {
        private static final int BLOCK_UPDATE_FLAGS = Block.UPDATE_ALL;

        private PlacementConstants() {
        }
    }
}
