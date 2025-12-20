package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 真元数据操作工具类。
 */
public final class ZhenYuanHelper {

    private ZhenYuanHelper() {}
    private static final double MIN_ZHUANSHU = 1.0;
    private static final double POWER_MULTIPLIER = 4.0;
    private static final double DENOM_MULTIPLIER = 3.0;
    private static final double DENOM_DIVISOR = 96.0;

    /**
     * 获取当前真元。
     */
    public static double getAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).zhenyuan;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取最大真元。
     */
    public static double getMaxAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).zuida_zhenyuan;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改真元数值。
     * @param amount 变化量 (正数为加，负数为减)
     * @return 修改后的真元值
     */
    public static double modify(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            var vars = getVariables(entity);
            double original = vars.zhenyuan;
            double max = vars.zuida_zhenyuan;

            // 确保真元在 0 到 最大值 之间
            double newValue = Math.max(0, Math.min(max, original + amount));

            if (Double.compare(original, newValue) != 0) {
                vars.zhenyuan = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 检查真元是否足够。
     */
    public static boolean hasEnough(LivingEntity entity, double cost) {
        return getAmount(entity) >= cost;
    }

    /**
     * 获取当前真元百分比（0-1）。
     */
    public static float getPercentage(LivingEntity entity) {
        double max = getMaxAmount(entity);
        if (max <= 0) {
            return 0.0f;
        }
        return (float) (getAmount(entity) / max);
    }

    /**
     * 计算蛊虫消耗真元（遵循模组标准公式）。
     * Formula: Base / ((2^(Stage + Rank*4) * Rank * 3) / 96)
     * @param entity 使用者
     * @param baseCost 蛊虫的基础消耗值
     * @return 实际消耗的真元量
     */
    public static double calculateGuCost(LivingEntity entity, double baseCost) {
        if (entity == null) {
            return baseCost;
        }
        try {
            var vars = getVariables(entity);
            double zhuanshu = vars.zhuanshu;
            double jieduan = vars.jieduan;

            // 防止除以零：如果转数小于1，视为1（或者极低效率）
            if (zhuanshu < 1) {
                zhuanshu = MIN_ZHUANSHU;
            }

            double power = Math.pow(2, jieduan + zhuanshu * POWER_MULTIPLIER);
            double denominator =
                (power * zhuanshu * DENOM_MULTIPLIER) / DENOM_DIVISOR;

            if (denominator <= 0) {
                return baseCost; // 异常保护
            }

            return baseCost / denominator;
        } catch (Exception e) {
            return baseCost;
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(
        LivingEntity entity
    ) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
