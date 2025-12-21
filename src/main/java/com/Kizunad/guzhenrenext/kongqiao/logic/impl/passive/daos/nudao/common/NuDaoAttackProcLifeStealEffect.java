package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 奴道被动：攻击触发（概率）“饮血”。
 * <p>
 * 触发后按本次攻击伤害的一定比例为自己回复生命，并可选附带一个负面效果。
 * </p>
 */
public class NuDaoAttackProcLifeStealEffect implements IGuEffect {
    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_LIFESTEAL_RATIO = "lifesteal_ratio";
    private static final String META_MAX_HEAL = "max_heal";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.12;
    private static final double DEFAULT_LIFESTEAL_RATIO = 0.0;
    private static final double DEFAULT_MAX_HEAL = 0.0;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 40;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private static final double MAX_RATIO = 0.50;
    private static final double MAX_HEAL_FALLBACK = 200.0;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public NuDaoAttackProcLifeStealEffect(
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
            DaoHenHelper.DaoType.NU_DAO
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

        final double ratio = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_LIFESTEAL_RATIO,
                DEFAULT_LIFESTEAL_RATIO
            ),
            0.0,
            MAX_RATIO
        );
        if (ratio <= 0.0) {
            return damage;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.NU_DAO
        );
        final double healBase = Math.max(0.0, damage) * ratio;
        final double maxHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAX_HEAL, DEFAULT_MAX_HEAL)
        );
        final double cap = maxHeal > 0.0 ? maxHeal : MAX_HEAL_FALLBACK;
        final double heal = Math.min(
            cap,
            DaoHenEffectScalingHelper.scaleValue(healBase, multiplier)
        );
        if (heal > 0.0) {
            attacker.heal((float) heal);
        }

        applyDebuff(target, usageInfo, multiplier);
        return damage;
    }

    private void applyDebuff(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (debuff == null) {
            return;
        }
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        if (duration <= 0) {
            return;
        }
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            duration,
            multiplier
        );
        if (scaledDuration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );
        target.addEffect(new MobEffectInstance(debuff, scaledDuration, amplifier, true, true));
    }
}
