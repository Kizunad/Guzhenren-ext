package com.Kizunad.guzhenrenext.guzhenrenBridge;

import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人真元交互桥接类。
 * <p>
 * 负责真元的读取、修改与同步。
 */
public final class ZhenYuanHelper {

    private ZhenYuanHelper() {}

    /**
     * 获取当前真元值。
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
     * 获取最大真元值。
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
     * 获取真元百分比 (0.0 - 1.0)。
     */
    public static float getPercentage(LivingEntity entity) {
        double max = getMaxAmount(entity);
        if (max <= 0) {
            return 0.0f;
        }
        double current = getAmount(entity);
        return (float) Math.max(0.0, Math.min(1.0, current / max));
    }

    /**
     * 修改真元值。
     * <p>
     * <b>注意：</b> 此方法会触发数据同步（Dirty标记）。
     * 蛊真人模组本身会在 Tick 结束时统一发送数据包，因此同一 Tick 内多次调用是安全的。
     * <p>
     * <b>性能警告：</b> 如果你在 tick() 中每刻都调用此方法（例如每秒20次），
     * 会导致每秒发送20个网络包。对于高频持续性修改（如回复），请自行实现累积缓冲（每秒应用一次）。
     *
     * @param entity 目标实体
     * @param amount 变化量（正数为增加，负数为减少）
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

            double newValue = Math.max(0, Math.min(max, original + amount));

            if (Double.compare(original, newValue) != 0) {
                vars.zhenyuan = newValue;
                vars.markSyncDirty(); // 标记脏数据，触发同步
            }
            return newValue;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 设置真元值。
     */
    public static void set(LivingEntity entity, double amount) {
        if (entity == null) {
            return;
        }
        try {
            var vars = getVariables(entity);
            double max = vars.zuida_zhenyuan;
            double newValue = Math.max(0, Math.min(max, amount));

            if (Double.compare(vars.zhenyuan, newValue) != 0) {
                vars.zhenyuan = newValue;
                vars.markSyncDirty();
            }
        } catch (Exception ignored) {}
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(
        LivingEntity entity
    ) {
        return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
    }
}
