package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道被动：持续维持（扣费）后获得减伤，减伤随【刀】道道痕动态变化。
 */
public final class DaoDaoSustainedDamageReductionEffect
    extends AbstractDaoDaoSustainedEffect {

    public static final String META_DAMAGE_REDUCTION_RATIO =
        "damage_reduction_ratio";

    private static final double MAX_REDUCTION_RATIO = 0.6;

    public DaoDaoSustainedDamageReductionEffect(final String usageId) {
        super(usageId, DaoHenHelper.DaoType.DAO_DAO);
    }

    @Override
    protected void onSustainSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double daoHenMultiplier
    ) {
        // 仅维持激活态与扣费
    }

    @Override
    protected void onInactive(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        // 无需清理
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim == null || usageInfo == null) {
            return damage;
        }
        if (!isActive(victim)) {
            return damage;
        }

        final double baseRatio = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_REDUCTION_RATIO,
                0.0
            )
        );
        if (baseRatio <= 0.0) {
            return damage;
        }

        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(
                victim,
                DaoHenHelper.DaoType.DAO_DAO
            )
        );
        final double scaledRatio = Math.min(MAX_REDUCTION_RATIO, baseRatio * multiplier);
        final double ratio = Math.max(0.0, scaledRatio);
        return (float) (damage * (1.0 - ratio));
    }
}
