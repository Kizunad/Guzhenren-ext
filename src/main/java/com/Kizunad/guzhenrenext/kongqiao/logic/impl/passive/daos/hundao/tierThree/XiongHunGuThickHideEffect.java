package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 熊魂蛊：被动【皮糙肉厚】。
 * <p>
 * 核心：
 * 1) 减伤：基础 5% + 土道/变化道道痕带来的防御加成。
 * 2) 副作用：更快的饱食度消耗 + 每秒吞噬魂魄。
 * 3) 阈值：当魂魄低于一定比例时，“沉眠”不生效（避免把魂魄扣到 0 导致处死）。
 * </p>
 */
public class XiongHunGuThickHideEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:xionghungu_passive_thickhide";

    private static final double DEFAULT_BASE_REDUCTION = 0.05;
    private static final double DEFAULT_TU_DIVISOR = 500.0;
    private static final double DEFAULT_BIANHUA_DIVISOR = 200.0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 160.0;
    private static final double DEFAULT_MIN_SOUL_RATIO = 0.2;

    /**
     * 额外饱食度消耗（exhaustion）。
     * <p>
     * 原版规则里 exhaustion 累积到 4 才会扣 1 点饥饿值；这里给一个可配的“额外每秒消耗”，
     * 用来表达“消耗速度增加 20%”的体感副作用（无需侵入玩家原版消耗逻辑）。
     * </p>
     */
    private static final float DEFAULT_FOOD_EXHAUSTION_PER_SECOND = 0.2F;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        if (!isActive(user, usageInfo)) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        final double hunDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            ) * hunDaoMultiplier
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        KongqiaoAttachments.getActivePassives(user).add(USAGE_ID);

        if (user instanceof Player player) {
            float exhaustion = getMetaFloat(
                usageInfo,
                "food_exhaustion_per_second",
                DEFAULT_FOOD_EXHAUSTION_PER_SECOND
            );
            if (exhaustion > 0) {
                player.causeFoodExhaustion(exhaustion);
            }
        }
    }

    @Override
    public float onHurt(
        LivingEntity victim,
        DamageSource source,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }
        if (!KongqiaoAttachments.getActivePassives(victim).isActive(USAGE_ID)) {
            return damage;
        }

        if (!isActive(victim, usageInfo)) {
            return damage;
        }

        double baseReduction = getMetaDouble(
            usageInfo,
            "base_reduction_percent",
            DEFAULT_BASE_REDUCTION
        );
        double tuDivisor = getMetaDouble(
            usageInfo,
            "tu_divisor",
            DEFAULT_TU_DIVISOR
        );
        double bianhuaDivisor = getMetaDouble(
            usageInfo,
            "bianhua_divisor",
            DEFAULT_BIANHUA_DIVISOR
        );

        double tuDaoHen = DaoHenHelper.getDaoHen(
            victim,
            DaoHenHelper.DaoType.TU_DAO
        );
        double bianhuaDaoHen = DaoHenHelper.getDaoHen(
            victim,
            DaoHenHelper.DaoType.BIAN_HUA_DAO
        );

        // 最终伤害 = 原伤害 * (1 - base * hundao_multiplier - (tudao/500) - (bianhuadao/200))
        final double hunDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.HUN_DAO
        );
        double multiplier =
            1.0 -
            baseReduction * hunDaoMultiplier -
            safeDiv(tuDaoHen, tuDivisor) -
            safeDiv(bianhuaDaoHen, bianhuaDivisor);

        multiplier = clamp01NonNegative(multiplier);
        return (float) (damage * multiplier);
    }

    private static boolean isActive(
        LivingEntity user,
        NianTouData.Usage usageInfo
    ) {
        double minRatio = getMetaDouble(
            usageInfo,
            "min_soul_ratio",
            DEFAULT_MIN_SOUL_RATIO
        );
        double max = HunPoHelper.getMaxAmount(user);
        if (max <= 0) {
            return false;
        }
        double current = HunPoHelper.getAmount(user);
        return (current / max) >= minRatio;
    }

    private static double safeDiv(double value, double divisor) {
        if (divisor <= 0) {
            return 0.0;
        }
        return value / divisor;
    }

    private static double clamp01NonNegative(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static float getMetaFloat(
        NianTouData.Usage usage,
        String key,
        float defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Float.parseFloat(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
