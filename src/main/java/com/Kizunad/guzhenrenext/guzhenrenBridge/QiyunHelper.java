package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 气运数据操作工具类。
 */
public final class QiyunHelper {

    private QiyunHelper() {}

    public static double getAmount(final LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).qiyun;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double getMaxAmount(final LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        try {
            return getVariables(entity).qiyun_shangxian;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 修改气运，按上限进行截断（允许为负）。
     * @return 修改后的气运值
     */
    public static double modify(final LivingEntity entity, final double amount) {
        if (entity == null) {
            return 0.0;
        }
        try {
            final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
            final double original = vars.qiyun;
            final double max = vars.qiyun_shangxian;
            final double raw = original + amount;
            final double newValue = max > 0.0 ? Math.min(raw, max) : raw;
            if (Double.compare(original, newValue) != 0) {
                vars.qiyun = newValue;
                PlayerVariablesSyncHelper.markSyncDirty(vars);
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
