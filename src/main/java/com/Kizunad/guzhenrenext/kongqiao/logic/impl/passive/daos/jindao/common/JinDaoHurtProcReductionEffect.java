package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 金道被动：受击触发（概率）减伤，并可附加自我增益/反震。
 */
public class JinDaoHurtProcReductionEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_PROJECTILE_ONLY = "projectile_only";
    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";
    private static final String META_RETALIATE_PHYSICAL_DAMAGE = "retaliate_physical_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 8;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.85;
    private static final double MAX_REDUCTION = 0.60;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 20 * 4;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> selfBuff;

    public JinDaoHurtProcReductionEffect(
        final String usageId,
        final String nbtCooldownKey,
        final Holder<MobEffect> selfBuff
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.selfBuff = selfBuff;
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

        final boolean projectileOnly = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_PROJECTILE_ONLY,
            false
        );
        if (projectileOnly && !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            return damage;
        }

        if (GuEffectCooldownHelper.isOnCooldown(victim, nbtCooldownKey)) {
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
            DaoHenHelper.DaoType.JIN_DAO
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

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                victim,
                nbtCooldownKey,
                victim.tickCount + cooldownTicks
            );
        }

        applySelfBuff(victim, usageInfo, selfMultiplier);
        retaliateIfPresent(victim, source, usageInfo, selfMultiplier);

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double baseReduction = 1.0 - baseMultiplier;
        final double scaledReduction = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseReduction, selfMultiplier),
            0.0,
            MAX_REDUCTION
        );
        final double multiplier = 1.0 - scaledReduction;

        return (float) (damage * multiplier);
    }

    private void applySelfBuff(
        final LivingEntity victim,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (selfBuff == null) {
            return;
        }
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
                new MobEffectInstance(
                    selfBuff,
                    scaledDuration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }

    private static void retaliateIfPresent(
        final LivingEntity victim,
        final DamageSource source,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_RETALIATE_PHYSICAL_DAMAGE,
                0.0
            )
        );
        if (baseDamage <= 0.0) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) {
            return;
        }

        final double amount = DaoHenEffectScalingHelper.scaleValue(baseDamage, selfMultiplier);
        if (amount <= 0.0) {
            return;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            victim,
            attacker,
            DaoHenHelper.DaoType.JIN_DAO
        );
        attacker.hurt(buildPhysicalDamageSource(victim), (float) (amount * multiplier));
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}

