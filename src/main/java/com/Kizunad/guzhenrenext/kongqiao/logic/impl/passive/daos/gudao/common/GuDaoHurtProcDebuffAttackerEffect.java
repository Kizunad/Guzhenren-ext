package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 骨道通用被动：受伤时（概率）对攻击者施加 debuff，并可减免部分伤害。
 * <p>
 * - 资源消耗走 GuEffectCostHelper（含真元标准折算）。
 * - 持续/概率/减伤随骨道道痕动态变化（倍率做上限保护）。
 * </p>
 */
public class GuDaoHurtProcDebuffAttackerEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";

    private static final double DEFAULT_PROC_CHANCE = 0.20;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 1.0;

    private static final double MAX_DAMAGE_REDUCTION = 0.60;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public GuDaoHurtProcDebuffAttackerEffect(
        final String usageId,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.debuff = debuff;
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
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return damage;
        }
        if (attacker == victim) {
            return damage;
        }

        final double baseChance = UsageMetadataHelper.clamp(
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
            DaoHenHelper.DaoType.GU_DAO
        );
        final double chance = DaoHenEffectScalingHelper.scaleChance(
            baseChance,
            selfMultiplier
        );
        if (victim.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, victim, usageInfo)) {
            return damage;
        }

        final double vsMultiplier = DaoHenCalculator.calculateMultiplier(
            victim,
            attacker,
            DaoHenHelper.DaoType.GU_DAO
        );
        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            vsMultiplier
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );

        if (debuff != null && duration > 0) {
            attacker.addEffect(
                new MobEffectInstance(
                    debuff,
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
        }

        final double baseDamageMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double baseReduction = 1.0 - baseDamageMultiplier;
        final double scaledReduction = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseReduction, selfMultiplier),
            0.0,
            MAX_DAMAGE_REDUCTION
        );
        final double finalMultiplier = 1.0 - scaledReduction;
        return (float) (damage * finalMultiplier);
    }
}

