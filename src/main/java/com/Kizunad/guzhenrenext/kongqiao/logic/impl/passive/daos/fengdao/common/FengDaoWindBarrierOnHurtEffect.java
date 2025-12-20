package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 风道通用被动：受伤触发风障（概率）。
 * <p>
 * 触发后可：
 * 1) 按比例减伤（受风道道痕自倍率影响，但有下限保护）；<br>
 * 2) 推开伤害来源实体（如果可用）；<br>
 * 3) 给予自身短暂移速提升。<br>
 * </p>
 */
public class FengDaoWindBarrierOnHurtEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_PUSH_STRENGTH = "push_strength";
    private static final String META_SPEED_DURATION_TICKS = "speed_duration_ticks";
    private static final String META_SPEED_AMPLIFIER = "speed_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.18;
    private static final int DEFAULT_COOLDOWN_TICKS = 120;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.85;
    private static final double DEFAULT_PUSH_STRENGTH = 0.35;
    private static final int DEFAULT_SPEED_DURATION_TICKS = 60;
    private static final int DEFAULT_SPEED_AMPLIFIER = 0;

    private static final double MIN_DAMAGE_MULTIPLIER = 0.35;
    private static final double DIRECTION_EPSILON_SQR = 1.0E-6;
    private static final int MAX_EFFECT_DURATION_TICKS = 20 * 30;

    private final String usageId;
    private final String nbtCooldownKey;

    public FengDaoWindBarrierOnHurtEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            victim,
            nbtCooldownKey
        );
        if (remain > 0) {
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

        applyPushBack(victim, source, usageInfo);
        final double fengDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.FENG_DAO
        );
        applySpeedBuff(victim, usageInfo, fengDaoMultiplier);

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double scaledMultiplier = UsageMetadataHelper.clamp(
            1.0 - (1.0 - baseMultiplier) * fengDaoMultiplier,
            MIN_DAMAGE_MULTIPLIER,
            1.0
        );
        return (float) (damage * scaledMultiplier);
    }

    private static void applyPushBack(
        final LivingEntity victim,
        final DamageSource source,
        final NianTouData.Usage usageInfo
    ) {
        final double pushStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PUSH_STRENGTH,
                DEFAULT_PUSH_STRENGTH
            )
        );
        if (pushStrength <= 0.0) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        final Vec3 dir = attacker.position().subtract(victim.position());
        if (dir.lengthSqr() <= DIRECTION_EPSILON_SQR) {
            return;
        }
        attacker.knockback(pushStrength, dir.x, dir.z);
    }

    private static void applySpeedBuff(
        final LivingEntity victim,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final Holder<MobEffect> speed = MobEffects.MOVEMENT_SPEED;
        if (speed == null) {
            return;
        }
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SPEED_DURATION_TICKS,
                DEFAULT_SPEED_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SPEED_AMPLIFIER,
                DEFAULT_SPEED_AMPLIFIER
            )
        );
        final int scaledDuration = (int) Math.min(
            MAX_EFFECT_DURATION_TICKS,
            Math.round(duration * multiplier)
        );
        if (scaledDuration > 0) {
            victim.addEffect(
                new MobEffectInstance(
                    speed,
                    scaledDuration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }
}
