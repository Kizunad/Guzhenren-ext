package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class JianDaoHurtProcReductionEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_PROC_COOLDOWN_TICKS = "proc_cooldown_ticks";
    private static final String META_HURT_MULTIPLIER = "hurt_multiplier";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final double DEFAULT_HURT_MULTIPLIER = 0.85;
    private static final double MIN_HURT_MULTIPLIER = 0.10;

    private final String usageId;
    private final String procCooldownKey;

    public JianDaoHurtProcReductionEffect(
        final String usageId,
        final String procCooldownKey
    ) {
        this.usageId = usageId;
        this.procCooldownKey = procCooldownKey;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim == null) {
            return damage;
        }
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        if (procCooldownKey != null && !procCooldownKey.isBlank()) {
            final int remain = GuEffectCooldownHelper.getRemainingTicks(
                victim,
                procCooldownKey
            );
            if (remain > 0) {
                return damage;
            }
        }

        final double chance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.JIAN_DAO
        ) * JianDaoBoostHelper.getJianXinMultiplier(victim);
        final double scaledChance = DaoHenEffectScalingHelper.scaleChance(
            chance,
            selfMultiplier
        );
        if (victim.getRandom().nextDouble() > scaledChance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, victim, usageInfo)) {
            return damage;
        }

        final int procCooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_PROC_COOLDOWN_TICKS, 0)
        );
        if (
            procCooldownTicks > 0
                && procCooldownKey != null
                && !procCooldownKey.isBlank()
        ) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                victim,
                procCooldownKey,
                victim.tickCount + procCooldownTicks
            );
        }

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HURT_MULTIPLIER,
                DEFAULT_HURT_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double scale = DaoHenEffectScalingHelper.clampMultiplier(
            selfMultiplier
        );
        final double scaled = UsageMetadataHelper.clamp(
            baseMultiplier / Math.max(1.0, scale),
            MIN_HURT_MULTIPLIER,
            1.0
        );
        return (float) Math.max(0.0, damage * scaled);
    }
}

