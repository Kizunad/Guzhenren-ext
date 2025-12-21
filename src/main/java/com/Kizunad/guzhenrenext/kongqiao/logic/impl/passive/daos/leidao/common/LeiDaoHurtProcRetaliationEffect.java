package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 雷道被动：受击触发（概率）- 反制电击（普通伤害）+ 施加减益。
 * <p>
 * 反制伤害同样走普通伤害来源，受护甲影响。
 * </p>
 */
public class LeiDaoHurtProcRetaliationEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final String META_RETALIATION_DAMAGE = "retaliation_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final int DEFAULT_COOLDOWN_TICKS = 120;

    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public LeiDaoHurtProcRetaliationEffect(
        final String usageId,
        final String nbtCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.debuff = debuff;
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

        final Entity sourceEntity = source == null ? null : source.getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker) || !attacker.isAlive()) {
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
            DaoHenHelper.DaoType.LEI_DAO
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
                attacker.addEffect(
                    new MobEffectInstance(debuff, scaledDuration, amplifier, true, true)
                );
            }
        }

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RETALIATION_DAMAGE, 0.0)
        );
        if (baseDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                victim,
                attacker,
                DaoHenHelper.DaoType.LEI_DAO
            );
            attacker.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(victim),
                (float) (baseDamage * multiplier)
            );
        }

        return damage;
    }
}
