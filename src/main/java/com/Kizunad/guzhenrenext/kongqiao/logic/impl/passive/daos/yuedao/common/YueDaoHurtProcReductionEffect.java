package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.SafeTeleportHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 月道被动：受击触发（概率）减伤，并可附加自我增益与短距退避。
 */
public class YueDaoHurtProcReductionEffect implements IGuEffect {
    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_PROJECTILE_ONLY = "projectile_only";
    private static final String META_BLINK_DISTANCE = "blink_distance";
    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 8;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.85;
    private static final double MAX_REDUCTION = 0.60;
    private static final double DEFAULT_BLINK_DISTANCE = 0.0;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 20 * 4;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;

    private static final double MAX_BLINK_DISTANCE = 18.0;
    private static final double MIN_ATTACKER_DIRECTION_SQR = 1.0e-4;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> selfBuff;

    public YueDaoHurtProcReductionEffect(
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
            DaoHenHelper.DaoType.YUE_DAO
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

        final double baseBlinkDistance = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BLINK_DISTANCE,
                DEFAULT_BLINK_DISTANCE
            )
        );
        final double blinkDistance = DaoHenEffectScalingHelper.scaleValue(
            baseBlinkDistance,
            selfMultiplier
        );
        final double clampedBlink = UsageMetadataHelper.clamp(
            blinkDistance,
            0.0,
            MAX_BLINK_DISTANCE
        );
        if (clampedBlink > 0.0) {
            tryBlinkAway(victim, source, clampedBlink);
        }

        if (selfBuff != null) {
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

        return (float) (damage * multiplier);
    }

    private static void tryBlinkAway(
        final LivingEntity victim,
        final DamageSource source,
        final double distance
    ) {
        final Vec3 from = victim.position();
        Vec3 dir = victim.getViewVector(1.0F);
        final var attacker = source.getEntity();
        if (attacker instanceof LivingEntity entity) {
            final Vec3 delta = from.subtract(entity.position());
            if (delta.lengthSqr() > MIN_ATTACKER_DIRECTION_SQR) {
                dir = delta.normalize();
            }
        }
        final Vec3 target = from.add(dir.scale(distance));
        SafeTeleportHelper.teleportSafely(victim, target);
    }
}

