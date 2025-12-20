package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 念头容量与智道念头相关字段操作工具。
 */
public final class NianTouCapacityHelper {

    private NianTouCapacityHelper() {}

    /**
     * 获取念头容量（niantou_rongliang）。
     */
    public static double getCapacity(final LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).niantou_rongliang;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改念头容量（niantou_rongliang）。
     * @return 修改后的容量
     */
    public static double modifyCapacity(
        final LivingEntity entity,
        final double amount
    ) {
        if (entity == null) {
            return 0.0;
        }
        try {
            final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
            final double original = vars.niantou_rongliang;
            final double newValue = Math.max(0.0, original + amount);
            if (Double.compare(original, newValue) != 0) {
                vars.niantou_rongliang = newValue;
                vars.markSyncDirty();
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取智道念头（niantou_zhida）。
     */
    public static double getZhiDaoNianTou(final LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).niantou_zhida;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改智道念头（niantou_zhida），不做上限约束（由原模组逻辑处理）。
     */
    public static double modifyZhiDaoNianTou(
        final LivingEntity entity,
        final double amount
    ) {
        if (entity == null) {
            return 0.0;
        }
        try {
            final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
            final double original = vars.niantou_zhida;
            final double newValue = original + amount;
            if (Double.compare(original, newValue) != 0) {
                vars.niantou_zhida = newValue;
                vars.markSyncDirty();
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(
        final LivingEntity entity
    ) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}

