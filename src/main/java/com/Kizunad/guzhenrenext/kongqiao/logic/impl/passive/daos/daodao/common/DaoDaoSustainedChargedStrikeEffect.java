package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道被动：持续维持后，每秒为下一次攻击充能一次“断刀”附伤（一次性消耗）。
 * <p>
 * 设计目的：提供“不能连续催动”的节奏感，同时避免护甲穿透类伤害过强。
 * </p>
 */
public final class DaoDaoSustainedChargedStrikeEffect
    extends AbstractDaoDaoSustainedEffect {

    public static final String META_CHARGED_EXTRA_PHYSICAL_DAMAGE =
        "charged_extra_physical_damage";

    private static final String TAG_CHARGE = "GuzhenrenExt_DaoDaoCharge";
    private static final int MAX_CHARGE = 1;
    private static final int EMPTY_CHARGE = 0;

    public DaoDaoSustainedChargedStrikeEffect(final String usageId) {
        super(usageId, DaoHenHelper.DaoType.DAO_DAO);
    }

    @Override
    protected void onSustainSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double daoHenMultiplier
    ) {
        final CompoundTag data = user.getPersistentData();
        data.putInt(buildChargeKey(), MAX_CHARGE);
    }

    @Override
    protected void onInactive(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        user.getPersistentData().remove(buildChargeKey());
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

        final CompoundTag data = attacker.getPersistentData();
        final int charge = data.getInt(buildChargeKey());
        if (charge <= EMPTY_CHARGE) {
            return damage;
        }
        data.putInt(buildChargeKey(), EMPTY_CHARGE);

        final double extraBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_CHARGED_EXTRA_PHYSICAL_DAMAGE,
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
        return (float) (damage + extraBase * multiplier);
    }

    private String buildChargeKey() {
        return TAG_CHARGE + "_" + getUsageId();
    }
}
