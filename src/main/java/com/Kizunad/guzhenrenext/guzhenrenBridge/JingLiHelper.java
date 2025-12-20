package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人精力交互桥接类。
 * <p>
 * 负责精力的读取、修改与同步。
 * </p>
 */
public final class JingLiHelper {

    private JingLiHelper() {
    }

    /**
     * 获取当前精力值。
     */
    public static double getAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).jingli;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取最大精力值。
     */
    public static double getMaxAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).zuida_jingli;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改精力值。
     *
     * @param entity 目标实体
     * @param amount 变化量（正数为增加，负数为减少）
     * @return 修改后的精力值
     */
    public static double modify(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            double original = vars.jingli;
            double max = vars.zuida_jingli;

            double newValue = Math.max(0, Math.min(max, original + amount));
            if (Double.compare(original, newValue) != 0) {
                vars.jingli = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(LivingEntity entity) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
