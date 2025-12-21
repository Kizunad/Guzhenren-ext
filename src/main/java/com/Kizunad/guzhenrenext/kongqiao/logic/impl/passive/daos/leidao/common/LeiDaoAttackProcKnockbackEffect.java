package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
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
 * 雷道被动：攻击触发（概率）- 击退震荡 + 可选弱化/附加伤害。
 * <p>
 * 用于“雷哮”类的冲击波手感：更偏功能/控场，伤害相对保守。
 * </p>
 */
public class LeiDaoAttackProcKnockbackEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_KNOCKBACK_Y = "knockback_y";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.10;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.35;
    private static final double MAX_KNOCKBACK_STRENGTH = 1.6;
    private static final double DEFAULT_KNOCKBACK_Y = 0.05;
    private static final double MAX_KNOCKBACK_Y = 0.45;
    private static final double MIN_DIR_LENGTH = 0.0001;

    private static final int DEFAULT_EFFECT_DURATION_TICKS = 40;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public LeiDaoAttackProcKnockbackEffect(
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
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(usageId)) {
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
            attacker,
            DaoHenHelper.DaoType.LEI_DAO
        );
        final double chance = DaoHenEffectScalingHelper.scaleChance(
            baseChance,
            selfMultiplier
        );
        if (attacker.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }

        final double baseStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_STRENGTH,
                DEFAULT_KNOCKBACK_STRENGTH
            )
        );
        final double strength = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseStrength, selfMultiplier),
            0.0,
            MAX_KNOCKBACK_STRENGTH
        );

        final double baseKnockY = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_Y,
                DEFAULT_KNOCKBACK_Y
            )
        );
        final double knockY = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseKnockY, selfMultiplier),
            0.0,
            MAX_KNOCKBACK_Y
        );

        if (strength > 0.0 || knockY > 0.0) {
            final Vec3 delta = target.position().subtract(attacker.position());
            final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
            final double len = horizontal.length();
            if (len > MIN_DIR_LENGTH) {
                final Vec3 dir = horizontal.scale(1.0 / len);
                target.push(dir.x * strength, knockY, dir.z * strength);
                target.hasImpulse = true;
            }
        }

        if (debuff != null) {
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_EFFECT_DURATION_TICKS,
                    DEFAULT_EFFECT_DURATION_TICKS
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
                    META_EFFECT_AMPLIFIER,
                    DEFAULT_EFFECT_AMPLIFIER
                )
            );
            if (scaledDuration > 0) {
                target.addEffect(
                    new MobEffectInstance(debuff, scaledDuration, amplifier, true, true)
                );
            }
        }

        final double baseExtra = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        if (baseExtra > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.LEI_DAO
            );
            final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(attacker);
            target.hurt(source, (float) (baseExtra * multiplier));
        }

        return damage;
    }
}
