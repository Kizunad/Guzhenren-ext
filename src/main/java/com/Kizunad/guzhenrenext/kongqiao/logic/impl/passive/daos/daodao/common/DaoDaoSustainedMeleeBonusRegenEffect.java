package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道被动：持续维持（扣费）后获得近战附加伤害，并可提供资源恢复（满足“至少一种被动恢复资源”的约束）。
 */
public final class DaoDaoSustainedMeleeBonusRegenEffect
    extends AbstractDaoDaoSustainedEffect {

    public static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    public static final String META_REGEN_ZHENYUAN = "regen_zhenyuan";
    public static final String META_REGEN_JINGLI = "regen_jingli";
    public static final String META_REGEN_HUNPO = "regen_hunpo";
    public static final String META_REGEN_NIANTOU = "regen_niantou";

    public DaoDaoSustainedMeleeBonusRegenEffect(final String usageId) {
        super(usageId, DaoHenHelper.DaoType.DAO_DAO);
    }

    @Override
    protected void onSustainSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double daoHenMultiplier
    ) {
        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            daoHenMultiplier
        );

        final double zhenyuanRegen = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_REGEN_ZHENYUAN, 0.0)
        );
        if (zhenyuanRegen > 0.0) {
            ZhenYuanHelper.modify(user, zhenyuanRegen * multiplier);
        }

        final double jingliRegen = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_REGEN_JINGLI, 0.0)
        );
        if (jingliRegen > 0.0) {
            JingLiHelper.modify(user, jingliRegen * multiplier);
        }

        final double hunpoRegen = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_REGEN_HUNPO, 0.0)
        );
        if (hunpoRegen > 0.0) {
            HunPoHelper.modify(user, hunpoRegen * multiplier);
        }

        final double niantouRegen = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_REGEN_NIANTOU, 0.0)
        );
        if (niantouRegen > 0.0) {
            NianTouHelper.modify(user, niantouRegen * multiplier);
        }
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
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker == null || target == null || usageInfo == null) {
            return damage;
        }
        if (!isActive(attacker)) {
            return damage;
        }

        final double extraBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_PHYSICAL_DAMAGE,
                0.0
            )
        );
        if (extraBase <= 0.0) {
            return damage;
        }

        final double multiplier = Math.max(
            0.0,
            DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.DAO_DAO
            )
        );
        final double extra = extraBase * multiplier;
        return (float) (damage + extra);
    }
}
