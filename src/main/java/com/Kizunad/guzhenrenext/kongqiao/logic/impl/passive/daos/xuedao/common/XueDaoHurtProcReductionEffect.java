package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血道被动：受击触发减伤（带概率与触发冷却）。
 * <p>
 * 说明：减伤属于“非伤害类强度”，使用道痕倍率裁剪，避免堆叠后出现近乎免伤的极端情况。
 * </p>
 */
public class XueDaoHurtProcReductionEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_PROC_COOLDOWN_TICKS = "proc_cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_SELF_HEAL_AMOUNT = "self_heal_amount";

    private static final double DEFAULT_PROC_CHANCE = 0.10;
    private static final int DEFAULT_PROC_COOLDOWN_TICKS = 80;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.85;

    private static final double MIN_DAMAGE_MULTIPLIER = 0.10;
    private static final double MAX_SELF_HEAL = 100.0;

    private final String usageId;
    private final String procCooldownKey;

    public XueDaoHurtProcReductionEffect(
        final String usageId,
        final String procCooldownKey
    ) {
        this.usageId = usageId;
        this.procCooldownKey = procCooldownKey;
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
        if (victim == null || usageInfo == null) {
            return damage;
        }
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            victim,
            procCooldownKey
        );
        if (remain > 0) {
            return damage;
        }

        final double baseChance = UsageMetadataHelper.getDouble(
            usageInfo,
            META_PROC_CHANCE,
            DEFAULT_PROC_CHANCE
        );
        final double chance = UsageMetadataHelper.clamp(baseChance, 0.0, 1.0);
        if (victim.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(
            victim instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null,
            victim,
            usageInfo
        )) {
            return damage;
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_PROC_COOLDOWN_TICKS,
                DEFAULT_PROC_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                victim,
                procCooldownKey,
                victim.tickCount + cooldownTicks
            );
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(victim, DaoHenHelper.DaoType.XUE_DAO)
        );

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            ),
            MIN_DAMAGE_MULTIPLIER,
            1.0
        );
        final double scaledMultiplier = scaleReductionMultiplier(
            baseMultiplier,
            selfMultiplier
        );

        applySelfHeal(victim, usageInfo, selfMultiplier);

        return (float) (damage * scaledMultiplier);
    }

    private static void applySelfHeal(
        final LivingEntity victim,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_HEAL_AMOUNT, 0.0)
        );
        if (baseHeal <= 0.0) {
            return;
        }
        final double heal = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseHeal, selfMultiplier),
            0.0,
            MAX_SELF_HEAL
        );
        if (heal > 0.0) {
            victim.heal((float) heal);
        }
    }

    private static double scaleReductionMultiplier(
        final double baseMultiplier,
        final double selfMultiplier
    ) {
        final double m = DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
        final double reduced = 1.0 - (1.0 - baseMultiplier) * m;
        return UsageMetadataHelper.clamp(reduced, MIN_DAMAGE_MULTIPLIER, 1.0);
    }
}

