package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common;

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
 * 人道通用被动：攻击触发（概率）对目标施加 debuff，并可附带额外伤害。
 * <p>
 * 约束：人道“法术伤害”在玩法上过强，因此该模板保留字段兼容，但建议仅在极少数用途中以保守数值启用。
 * </p>
 */
public class RenDaoAttackProcDebuffEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.15;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    /**
     * 人道法术伤害穿透护甲的收益过高，故对最终法术伤害设置保守上限。
     */
    private static final double MAX_FINAL_MAGIC_DAMAGE = 800.0;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public RenDaoAttackProcDebuffEffect(
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
            DaoHenHelper.DaoType.REN_DAO
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
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_MAGIC_DAMAGE, 0.0)
        );
        if (extraPhysicalDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.REN_DAO
            );
            final DamageSource source = buildPhysicalDamageSource(attacker);
            target.hurt(source, (float) (extraPhysicalDamage * multiplier));
        } else if (extraMagicDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.REN_DAO
            );
            final double finalDamage = Math.min(
                MAX_FINAL_MAGIC_DAMAGE,
                extraMagicDamage * Math.max(0.0, multiplier)
            );
            if (finalDamage > 0.0) {
                target.hurt(attacker.damageSources().magic(), (float) finalDamage);
            }
        }

        return damage;
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity attacker) {
        if (attacker instanceof Player player) {
            return attacker.damageSources().playerAttack(player);
        }
        return attacker.damageSources().mobAttack(attacker);
    }
}

