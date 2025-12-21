package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 力道被动：攻击触发（概率）附带负面效果，并可追加少量普通伤害。
 * <p>
 * 注：法术伤害默认不推荐使用，保留兼容字段但优先普通伤害。
 * </p>
 */
public class LiDaoAttackProcDebuffEffect implements IGuEffect {
    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 20 * 2;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_EXTRA_MAGIC_DAMAGE = 0.0;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public LiDaoAttackProcDebuffEffect(
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
            DaoHenHelper.DaoType.LI_DAO
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
        if (debuff != null && scaledDuration > 0) {
            target.addEffect(
                new MobEffectInstance(debuff, scaledDuration, amplifier, true, true)
            );
        }

        final double extraPhysicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_MAGIC_DAMAGE,
                DEFAULT_EXTRA_MAGIC_DAMAGE
            )
        );
        if (extraPhysicalDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.LI_DAO
            );
            final DamageSource source = buildPhysicalDamageSource(attacker);
            target.hurt(source, (float) (extraPhysicalDamage * multiplier));
        } else if (extraMagicDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.LI_DAO
            );
            final DamageSource source = attacker.damageSources().magic();
            target.hurt(source, (float) (extraMagicDamage * multiplier));
        }

        return damage;
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}

