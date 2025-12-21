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
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class JianDaoAttackProcBonusDamageEffect implements IGuEffect {

    public static final class EffectSpec {
        private final Holder<MobEffect> effect;
        private final String durationKey;
        private final int defaultDurationTicks;
        private final String amplifierKey;
        private final int defaultAmplifier;

        public EffectSpec(
            final Holder<MobEffect> effect,
            final String durationKey,
            final int defaultDurationTicks,
            final String amplifierKey,
            final int defaultAmplifier
        ) {
            this.effect = effect;
            this.durationKey = durationKey;
            this.defaultDurationTicks = defaultDurationTicks;
            this.amplifierKey = amplifierKey;
            this.defaultAmplifier = defaultAmplifier;
        }
    }

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_PROC_COOLDOWN_TICKS = "proc_cooldown_ticks";
    private static final String META_BONUS_DAMAGE = "bonus_damage";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 1.0;
    private static final double MIN_DAMAGE_MULTIPLIER = 1.0;

    private final String usageId;
    private final String procCooldownKey;
    private final List<EffectSpec> debuffs;

    public JianDaoAttackProcBonusDamageEffect(
        final String usageId,
        final String procCooldownKey,
        final List<EffectSpec> debuffs
    ) {
        this.usageId = usageId;
        this.procCooldownKey = procCooldownKey;
        this.debuffs = debuffs == null ? List.of() : List.copyOf(debuffs);
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker == null || target == null) {
            return damage;
        }
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        if (procCooldownKey != null && !procCooldownKey.isBlank()) {
            final int remain = GuEffectCooldownHelper.getRemainingTicks(
                attacker,
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
            attacker,
            DaoHenHelper.DaoType.JIAN_DAO
        ) * JianDaoBoostHelper.getJianXinMultiplier(attacker);
        final double scaledChance = DaoHenEffectScalingHelper.scaleChance(
            chance,
            selfMultiplier
        );
        if (attacker.getRandom().nextDouble() > scaledChance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
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
                attacker,
                procCooldownKey,
                attacker.tickCount + procCooldownTicks
            );
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.JIAN_DAO
        ) * JianDaoBoostHelper.getJianXinMultiplier(attacker);

        applyDebuffs(target, usageInfo, multiplier);

        final double baseBonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_BONUS_DAMAGE, 0.0)
        );
        final double bonus = baseBonus * Math.max(0.0, multiplier);

        final double rawDamageMultiplier = UsageMetadataHelper.getDouble(
            usageInfo,
            META_DAMAGE_MULTIPLIER,
            DEFAULT_DAMAGE_MULTIPLIER
        );
        final double safeDamageMultiplier = Math.max(
            MIN_DAMAGE_MULTIPLIER,
            rawDamageMultiplier
        );

        final double scaled = (damage * safeDamageMultiplier) + bonus;
        return (float) Math.max(0.0, scaled);
    }

    private void applyDebuffs(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (debuffs.isEmpty()) {
            return;
        }
        for (EffectSpec spec : debuffs) {
            if (spec == null || spec.effect == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey,
                    spec.defaultDurationTicks
                )
            );
            if (duration <= 0) {
                continue;
            }
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                multiplier
            );
            if (scaledDuration <= 0) {
                continue;
            }
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey,
                    spec.defaultAmplifier
                )
            );
            target.addEffect(
                new MobEffectInstance(spec.effect, scaledDuration, amplifier, true, true)
            );
        }
    }
}

