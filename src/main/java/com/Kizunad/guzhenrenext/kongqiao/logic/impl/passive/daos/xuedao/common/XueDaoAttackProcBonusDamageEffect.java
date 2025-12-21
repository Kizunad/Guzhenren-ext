package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血道被动：攻击触发“增伤 + 附伤 + 自愈/回资源”。
 * <p>
 * 设计目标：
 * <ul>
 *   <li>以普通伤害为主（受护甲影响），避免法术穿甲导致强度失控。</li>
 *   <li>增伤/附伤/自愈等强度随血道道痕动态缩放。</li>
 *   <li>触发受概率与触发冷却控制，资源消耗走标准折算。</li>
 * </ul>
 * </p>
 */
public class XueDaoAttackProcBonusDamageEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_PROC_COOLDOWN_TICKS = "proc_cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_BONUS_DAMAGE = "bonus_damage";

    private static final String META_SELF_HEAL_AMOUNT = "self_heal_amount";
    private static final String META_SELF_NIANTOU_GAIN = "self_niantou_gain";
    private static final String META_SELF_JINGLI_GAIN = "self_jingli_gain";
    private static final String META_SELF_HUNPO_GAIN = "self_hunpo_gain";
    private static final String META_SELF_ZHENYUAN_GAIN = "self_zhenyuan_gain";

    private static final String META_DEBUFF_DURATION_TICKS = "debuff_duration_ticks";
    private static final String META_DEBUFF_AMPLIFIER = "debuff_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.08;
    private static final int DEFAULT_PROC_COOLDOWN_TICKS = 60;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 1.0;

    private static final double MAX_SELF_HEAL = 100.0;

    private final String usageId;
    private final String procCooldownKey;
    private final Holder<MobEffect> debuff;

    public XueDaoAttackProcBonusDamageEffect(
        final String usageId,
        final String procCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.procCooldownKey = procCooldownKey;
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
        if (attacker == null || target == null || usageInfo == null) {
            return damage;
        }
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return damage;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            attacker,
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
        if (attacker.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(
            attacker instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null,
            attacker,
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
                attacker,
                procCooldownKey,
                attacker.tickCount + cooldownTicks
            );
        }

        final double dmgMultiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.XUE_DAO
        );
        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(attacker, DaoHenHelper.DaoType.XUE_DAO)
        );

        applyDebuff(target, usageInfo, selfMultiplier);
        applySelfGains(attacker, usageInfo, selfMultiplier);
        applyBonusDamage(attacker, target, usageInfo, dmgMultiplier);

        final double baseDamageMultiplier = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            )
        );
        final double scaledMultiplier = scaleDamageMultiplier(baseDamageMultiplier, dmgMultiplier);

        return (float) (damage * scaledMultiplier);
    }

    private void applyDebuff(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (debuff == null) {
            return;
        }
        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_DURATION_TICKS, 0)
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            selfMultiplier
        );
        if (duration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_AMPLIFIER, 0)
        );
        target.addEffect(new MobEffectInstance(debuff, duration, amplifier, true, true));
    }

    private static void applySelfGains(
        final LivingEntity attacker,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_HEAL_AMOUNT, 0.0)
        );
        if (baseHeal > 0.0) {
            final double heal = UsageMetadataHelper.clamp(
                DaoHenEffectScalingHelper.scaleValue(baseHeal, selfMultiplier),
                0.0,
                MAX_SELF_HEAL
            );
            if (heal > 0.0) {
                attacker.heal((float) heal);
            }
        }

        final double baseNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_NIANTOU_GAIN, 0.0)
        );
        if (baseNianTou > 0.0) {
            NianTouHelper.modify(
                attacker,
                DaoHenEffectScalingHelper.scaleValue(baseNianTou, selfMultiplier)
            );
        }

        final double baseJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_JINGLI_GAIN, 0.0)
        );
        if (baseJingli > 0.0) {
            JingLiHelper.modify(
                attacker,
                DaoHenEffectScalingHelper.scaleValue(baseJingli, selfMultiplier)
            );
        }

        final double baseHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_HUNPO_GAIN, 0.0)
        );
        if (baseHunpo > 0.0) {
            HunPoHelper.modify(
                attacker,
                DaoHenEffectScalingHelper.scaleValue(baseHunpo, selfMultiplier)
            );
        }

        final double baseZhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_SELF_ZHENYUAN_GAIN, 0.0)
        );
        if (baseZhenyuan > 0.0) {
            ZhenYuanHelper.modify(
                attacker,
                DaoHenEffectScalingHelper.scaleValue(baseZhenyuan, selfMultiplier)
            );
        }
    }

    private static void applyBonusDamage(
        final LivingEntity attacker,
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double dmgMultiplier
    ) {
        final double baseBonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_BONUS_DAMAGE, 0.0)
        );
        if (baseBonus <= 0.0) {
            return;
        }
        final double finalBonus = baseBonus * Math.max(0.0, dmgMultiplier);
        if (finalBonus <= 0.0) {
            return;
        }
        target.hurt(
            PhysicalDamageSourceHelper.buildPhysicalDamageSource(attacker),
            (float) finalBonus
        );
    }

    /**
     * 仅对“增伤倍率”做温和缩放：避免伤害倍率随道痕指数级膨胀导致一击过强。
     */
    private static double scaleDamageMultiplier(
        final double baseDamageMultiplier,
        final double dmgMultiplier
    ) {
        if (baseDamageMultiplier <= 0.0) {
            return 0.0;
        }
        if (baseDamageMultiplier <= 1.0) {
            return baseDamageMultiplier;
        }
        final double m = DaoHenEffectScalingHelper.clampMultiplier(dmgMultiplier);
        return 1.0 + (baseDamageMultiplier - 1.0) * m;
    }
}

