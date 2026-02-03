package com.Kizunad.guzhenrenext.bastion.multiblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * 多方块结构检测服务。
 * <p>
 * 提供蓝图匹配、旋转匹配与批量匹配能力，不主动扫描世界。
 * </p>
 */
public final class BastionMultiblockService {

    /** 默认蓝图管理器实例。 */
    private static final BastionBlueprintManager DEFAULT_MANAGER = BastionBlueprintManager.createDefault();

    private BastionMultiblockService() {
    }

    /**
     * 匹配结果记录。
     *
     * @param matches 是否完全匹配
     * @param blueprintId 蓝图 ID
     * @param rotation 旋转角度（0/90/180/270）
     * @param matchedCount 已匹配必需方块数量
     * @param totalRequired 必需方块总数
     */
    public record MatchResult(boolean matches, String blueprintId, int rotation, int matchedCount,
                              int totalRequired) {
    }

    /**
     * 检测指定位置是否符合给定蓝图（不考虑旋转）。
     *
     * @param level 世界实例
     * @param center 结构中心位置
     * @param blueprint 蓝图定义
     * @return 匹配结果
     */
    public static MatchResult checkBlueprint(Level level, BlockPos center, BastionBlueprint blueprint) {
        int matched = 0;
        int required = 0;

        for (BastionBlueprint.BlockEntry entry : blueprint.entries()) {
            if (!entry.required()) {
                continue;
            }
            required++;

            BlockPos checkPos = center.offset(entry.x(), entry.y(), entry.z());
            Block worldBlock = level.getBlockState(checkPos).getBlock();

            if (worldBlock == entry.block()) {
                matched++;
            }
        }

        boolean matches = matched == required;
        return new MatchResult(matches, blueprint.id(), RotationConstants.ROTATION_0, matched, required);
    }

    /**
     * 在四个旋转方向上检测蓝图。
     *
     * @param level 世界实例
     * @param center 结构中心位置
     * @param blueprint 蓝图定义
     * @return 匹配结果（包含最佳匹配旋转），未匹配时 rotation 仍为最后尝试值
     */
    public static MatchResult checkBlueprintWithRotation(Level level, BlockPos center,
                                                         BastionBlueprint blueprint) {
        MatchResult bestResult = null;

        for (int rotation : RotationConstants.ROTATIONS) {
            int matched = 0;
            int required = 0;

            for (BastionBlueprint.BlockEntry entry : blueprint.entries()) {
                if (!entry.required()) {
                    continue;
                }
                required++;

                BlockPos offset = rotateOffset(entry.x(), entry.y(), entry.z(), rotation);
                BlockPos checkPos = center.offset(offset);
                Block worldBlock = level.getBlockState(checkPos).getBlock();

                if (worldBlock == entry.block()) {
                    matched++;
                }
            }

            boolean matches = matched == required;
            MatchResult current = new MatchResult(matches, blueprint.id(), rotation, matched, required);
            if (matches) {
                return current;
            }
            bestResult = current;
        }

        return bestResult == null ?
            new MatchResult(false, blueprint.id(), RotationConstants.ROTATION_0, 0, 0) : bestResult;
    }

    /**
     * 获取指定位置匹配的所有蓝图（考虑旋转）。
     *
     * @param level 世界实例
     * @param center 结构中心位置
     * @return 匹配结果列表（仅返回匹配成功的蓝图）
     */
    public static List<MatchResult> getMatchingBlueprints(Level level, BlockPos center) {
        List<MatchResult> results = new ArrayList<>();
        for (Map.Entry<String, BastionBlueprint> entry : DEFAULT_MANAGER.view().entrySet()) {
            MatchResult result = checkBlueprintWithRotation(level, center, entry.getValue());
            if (result.matches()) {
                results.add(result);
            }
        }
        return results;
    }

    /**
     * 按指定旋转角度旋转相对坐标。
     *
     * @param x 原始 x 偏移
     * @param y 原始 y 偏移
     * @param z 原始 z 偏移
     * @param rotation 旋转角度（仅支持 0/90/180/270）
     * @return 旋转后的偏移
     */
    private static BlockPos rotateOffset(int x, int y, int z, int rotation) {
        return switch (rotation) {
            case RotationConstants.ROTATION_0 -> new BlockPos(x, y, z);
            case RotationConstants.ROTATION_90 -> new BlockPos(-z, y, x);
            case RotationConstants.ROTATION_180 -> new BlockPos(-x, y, -z);
            case RotationConstants.ROTATION_270 -> new BlockPos(z, y, -x);
            default -> new BlockPos(x, y, z);
        };
    }

    /**
     * 旋转相关常量，避免 MagicNumber。
     */
    private static final class RotationConstants {
        private static final int ROTATION_0 = 0;
        private static final int ROTATION_90 = 90;
        private static final int ROTATION_180 = 180;
        private static final int ROTATION_270 = 270;
        private static final int[] ROTATIONS = {
            ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270
        };

        private RotationConstants() {
        }
    }
}
