package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人实体交互桥接类。
 * <p>
 * 负责处理实体的身份识别、境界判断、空窍状态检查等只读操作。
 */
public final class EntityHelper {

    private EntityHelper() {}

    /**
     * 判断实体是否为蛊师（已开窍）。
     *
     * @param entity 目标实体
     * @return true 如果已开窍
     */
    public static boolean isGuMaster(LivingEntity entity) {
        if (entity == null) return false;
        try {
            var vars = getVariables(entity);
            // 根据源码，jieduan (阶段/转数) >= 1 或 kongqiao (空窍完整度) > 0 视为已开窍
            return vars.jieduan >= 1.0 || vars.kongqiao > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取蛊师转数（阶段）。
     *
     * @param entity 目标实体
     * @return 转数 (1-9)，如果不是蛊师或未开窍返回 0
     */
    public static int getGuMasterRank(LivingEntity entity) {
        if (entity == null) return 0;
        try {
            // 向下取整，jieduan 可能是 1.5 (一转中阶)
            return (int) getVariables(entity).jieduan;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取空窍完整度。
     *
     * @param entity 目标实体
     * @return 空窍值
     */
    public static double getApertureIntegrity(LivingEntity entity) {
        if (entity == null) return 0.0;
        try {
            return getVariables(entity).kongqiao;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 安全获取变量的内部辅助方法
     */
    private static GuzhenrenModVariables.PlayerVariables getVariables(
        LivingEntity entity
    ) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
