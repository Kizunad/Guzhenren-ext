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
 * 水道被动：受击触发（概率）- 卸力减伤 + 可选自我增益（短暂护体/稳态）。
 * <p>
 * 减伤采用“减伤量随道痕放大，但有上限”的形式，避免极端道痕带来完全免伤。
 * </p>
 */
public class ShuiDaoHurtProcReductionEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";

    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.15;
    private static final int DEFAULT_COOLDOWN_TICKS = 160;

    private static final double MIN_DAMAGE_MULTIPLIER = 0.25;
    private static final double MAX_REDUCTION = 0.80;

    private static final int DEFAULT_BUFF_DURATION_TICKS = 80;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> buff;

    public ShuiDaoHurtProcReductionEffect(
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

        final double baseChance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_PROC_CHANCE, DEFAULT_PROC_CHANCE),
            0.0,
            1.0
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.SHUI_DAO
        );
        final double chance = DaoHenEffectScalingHelper.scaleChance(baseChance, selfMultiplier);
        if (victim.getRandom().nextDouble() > chance) {
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

