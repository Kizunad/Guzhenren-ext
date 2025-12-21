package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.minecraft.world.entity.LivingEntity;

/**
 * 转数/阶段分层的统一折算：将“基础消耗”按玩家当前境界折算为“实际消耗”。
 * <p>
 * 约束：
 * <ul>
 *   <li>真元必须走 {@link ZhenYuanHelper#calculateGuCost(LivingEntity, double)}。</li>
 *   <li>念头/精力/魂魄等其他资源可复用同一折算分母（保证整体按转数分层）。</li>
 * </ul>
 * </p>
 */
public final class ZhuanCostHelper {

    private ZhuanCostHelper() {}

    /**
     * 将基础消耗按转数/阶段折算为实际消耗。
     */
    public static double scaleCost(final LivingEntity user, final double baseCost) {
        if (baseCost <= 0.0) {
            return 0.0;
        }
        final double denominator = ZhenYuanHelper.calculateGuCostDenominator(user);
        if (denominator <= 0.0) {
            return baseCost;
        }
        return baseCost / denominator;
    }

    /**
     * 将基础消耗（整数）折算并向上取整为整数消耗。
     */
    public static int scaleCostCeil(final LivingEntity user, final int baseCost) {
        if (baseCost <= 0) {
            return 0;
        }
        final double scaled = scaleCost(user, (double) baseCost);
        return (int) Math.ceil(Math.max(0.0, scaled));
    }

    /**
     * 将基础 Tick（整数）按转数/阶段折算并向上取整，至少为 1。
     */
    public static int scaleTicksCeil(final LivingEntity user, final int baseTicks) {
        if (baseTicks <= 1) {
            return 1;
        }
        final double scaled = scaleCost(user, (double) baseTicks);
        final int ticks = (int) Math.ceil(scaled);
        return Math.max(1, ticks);
    }
}

