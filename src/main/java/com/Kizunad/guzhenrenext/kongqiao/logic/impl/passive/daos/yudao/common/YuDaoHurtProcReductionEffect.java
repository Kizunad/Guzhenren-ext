package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
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
 * 宇道通用被动：受伤触发（概率）减伤，并可附带“闪避挪移”或“受击借力”。
 */
public class YuDaoHurtProcReductionEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_BLINK_DISTANCE = "blink_distance";
    private static final String META_PROJECTILE_ONLY = "projectile_only";
    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.18;
    private static final int DEFAULT_COOLDOWN_TICKS = 120;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.75;
    private static final double DEFAULT_BLINK_DISTANCE = 0.0;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 80;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double MAX_REDUCTION = 0.85;
    private static final double MIN_DAMAGE_TAKEN_MULTIPLIER = 0.15;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> selfBuff;

    public YuDaoHurtProcReductionEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final Holder<MobEffect> selfBuff
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
        this.nbtCooldownKey = "GuzhenrenExtPassiveCd_" + usageId + "_hurt_proc";
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

        final double chance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
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

        final double selfMultiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(victim, daoType);

        final double baseTakenMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double baseReduction = 1.0 - baseTakenMultiplier;
        final double scaledReduction = UsageMetadataHelper.clamp(
            baseReduction * Math.max(0.0, selfMultiplier),
            0.0,
            MAX_REDUCTION
        );
        final double takenMultiplier = UsageMetadataHelper.clamp(
            1.0 - scaledReduction,
            MIN_DAMAGE_TAKEN_MULTIPLIER,
            1.0
        );

        final double blinkDistance = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BLINK_DISTANCE,
                DEFAULT_BLINK_DISTANCE
            )
        ) * Math.max(0.0, selfMultiplier);
        if (blinkDistance > 0.0) {
            tryBlinkAway(victim, source, blinkDistance);
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
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_BUFF_AMPLIFIER,
                    DEFAULT_BUFF_AMPLIFIER
                )
            );
            final int scaledDuration = Math.max(
                0,
                (int) Math.round(duration * Math.max(0.0, selfMultiplier))
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

        return (float) (damage * takenMultiplier);
    }

    private static void tryBlinkAway(
        final LivingEntity victim,
        final DamageSource source,
        final double distance
    ) {
        final Vec3 from = victim.position();
        Vec3 dir = null;
        if (source != null && source.getEntity() instanceof LivingEntity attacker) {
            final Vec3 delta = from.subtract(attacker.position());
            final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
            if (horizontal.lengthSqr() > MIN_HORIZONTAL_VECTOR_SQR) {
                dir = horizontal.normalize();
            }
        }
        if (dir == null) {
            final double angle = victim.getRandom().nextDouble() * Math.PI * 2.0;
            dir = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
        }
        final Vec3 target = from.add(dir.scale(distance));
        SafeTeleportHelper.teleportSafely(victim, target);
    }
}
