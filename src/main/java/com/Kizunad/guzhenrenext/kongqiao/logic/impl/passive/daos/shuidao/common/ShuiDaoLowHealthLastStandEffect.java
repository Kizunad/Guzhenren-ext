package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common;

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
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 水道高转被动：背水一战（低血量触发）- 大幅减伤 + 可选短暂自 buff。
 * <p>
 * 该被动强调“命悬一线时的爆发性保命”，因此触发条件为血量比例低于阈值。
 * </p>
 *
 * <p>metadata:</p>
 * <ul>
 *   <li>health_threshold_ratio（触发阈值，0~1）</li>
 *   <li>cooldown_ticks</li>
 *   <li>damage_multiplier（触发时受到伤害乘算系数，0~1）</li>
 *   <li>buff_duration_ticks / buff_amplifier（可选）</li>
 *   <li>（消耗）niantou_cost / jingli_cost / hunpo_cost / zhenyuan_base_cost</li>
 * </ul>
 */
public class ShuiDaoLowHealthLastStandEffect implements IGuEffect {

    private static final String META_HEALTH_THRESHOLD_RATIO = "health_threshold_ratio";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";

    private static final double DEFAULT_HEALTH_THRESHOLD_RATIO = 0.30;
    private static final int DEFAULT_COOLDOWN_TICKS = 240;

    private static final double MIN_DAMAGE_MULTIPLIER = 0.10;
    private static final double MAX_REDUCTION = 0.90;

    private static final int DEFAULT_BUFF_DURATION_TICKS = 80;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> buff;

    public ShuiDaoLowHealthLastStandEffect(
        final String usageId,
        final String nbtCooldownKey,
        final Holder<MobEffect> buff
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.buff = buff;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(victim, nbtCooldownKey);
        if (remain > 0) {
            return damage;
        }

        final double threshold = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEALTH_THRESHOLD_RATIO,
                DEFAULT_HEALTH_THRESHOLD_RATIO
            ),
            0.0,
            1.0
        );
        final double maxHealth = Math.max(1.0, victim.getMaxHealth());
        final double ratio = victim.getHealth() / maxHealth;
        if (ratio > threshold) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, victim, usageInfo)) {
            return damage;
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                victim,
                nbtCooldownKey,
                victim.tickCount + cooldownTicks
            );
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.SHUI_DAO
        );

        if (buff != null) {
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_BUFF_DURATION_TICKS,
                    DEFAULT_BUFF_DURATION_TICKS
                )
            );
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                selfMultiplier
            );
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_BUFF_AMPLIFIER,
                    DEFAULT_BUFF_AMPLIFIER
                )
            );
            if (scaledDuration > 0) {
                victim.addEffect(
                    new MobEffectInstance(buff, scaledDuration, amplifier, true, true)
                );
            }
        }

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE_MULTIPLIER, 1.0),
            MIN_DAMAGE_MULTIPLIER,
            1.0
        );
        final double baseReduction = UsageMetadataHelper.clamp(
            1.0 - baseMultiplier,
            0.0,
            MAX_REDUCTION
        );
        final double scaledReduction = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseReduction, selfMultiplier),
            0.0,
            MAX_REDUCTION
        );
        final double finalMultiplier = UsageMetadataHelper.clamp(
            1.0 - scaledReduction,
            MIN_DAMAGE_MULTIPLIER,
            1.0
        );

        return (float) (damage * finalMultiplier);
    }
}

