package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道通用被动：攻击触发（概率）对目标施加减益，并可附带冻结与额外法术伤害。
 * <p>
 * 支持额外逻辑：若目标已受寒（冻结/缓慢），可提高本次伤害（不消耗）。
 * </p>
 */
public class BingXueDaoAttackProcDebuffEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_NIANTOU_COST = "niantou_cost";
    private static final String META_JINGLI_COST = "jingli_cost";
    private static final String META_HUNPO_COST = "hunpo_cost";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";
    private static final String META_FREEZE_TICKS = "freeze_ticks";
    private static final String META_DAMAGE_BONUS_VS_FROZEN = "damage_bonus_vs_frozen";

    private static final double DEFAULT_PROC_CHANCE = 0.15;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_EXTRA_MAGIC_DAMAGE = 0.0;
    private static final int DEFAULT_FREEZE_TICKS = 0;
    private static final double DEFAULT_DAMAGE_BONUS_VS_FROZEN = 0.0;

    private final String usageId;
    private final Holder<MobEffect> debuff;

    public BingXueDaoAttackProcDebuffEffect(
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

        float currentDamage = damage;
        currentDamage = applyFrozenBonus(attacker, target, usageInfo, currentDamage);

        final double chance = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROC_CHANCE,
                DEFAULT_PROC_CHANCE
            ),
            0.0,
            1.0
        );
        if (attacker.getRandom().nextDouble() > chance) {
            return currentDamage;
        }

        final double niantouCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST, 0.0)
        );
        if (niantouCost > 0.0 && NianTouHelper.getAmount(attacker) < niantouCost) {
            return currentDamage;
        }
        final double jingliCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        if (jingliCost > 0.0 && JingLiHelper.getAmount(attacker) < jingliCost) {
            return currentDamage;
        }
        final double hunpoCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(attacker) < hunpoCost) {
            return currentDamage;
        }
        if (niantouCost > 0.0) {
            NianTouHelper.modify(attacker, -niantouCost);
        }
        if (jingliCost > 0.0) {
            JingLiHelper.modify(attacker, -jingliCost);
        }
        if (hunpoCost > 0.0) {
            HunPoHelper.modify(attacker, -hunpoCost);
        }

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );
        if (debuff != null && duration > 0) {
            target.addEffect(
                new MobEffectInstance(debuff, duration, amplifier, true, true)
            );
        }

        final int freezeTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_FREEZE_TICKS,
                DEFAULT_FREEZE_TICKS
            )
        );
        if (freezeTicks > 0) {
            addFreezeTicks(target, freezeTicks);
        }

        final double extraMagicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_EXTRA_MAGIC_DAMAGE,
                DEFAULT_EXTRA_MAGIC_DAMAGE
            )
        );
        if (extraMagicDamage > 0.0) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.BING_XUE_DAO
            );
            target.hurt(
                attacker.damageSources().mobAttack(attacker),
                (float) (extraMagicDamage * multiplier)
            );
        }

        return currentDamage;
    }

    private static float applyFrozenBonus(
        final LivingEntity attacker,
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final float damage
    ) {
        final double bonus = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_BONUS_VS_FROZEN,
                DEFAULT_DAMAGE_BONUS_VS_FROZEN
            ),
            0.0,
            2.0
        );
        if (bonus <= 0.0) {
            return damage;
        }
        if (!isChilled(target)) {
            return damage;
        }
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.BING_XUE_DAO
        );
        final double finalMultiplier = UsageMetadataHelper.clamp(
            1.0 + bonus * multiplier,
            0.0,
            5.0
        );
        return (float) (damage * finalMultiplier);
    }

    private static boolean isChilled(final LivingEntity target) {
        if (target == null) {
            return false;
        }
        return target.getTicksFrozen() > 0 || target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    private static void addFreezeTicks(final LivingEntity target, final int ticks) {
        if (target == null || ticks <= 0) {
            return;
        }
        final int current = Math.max(0, target.getTicksFrozen());
        final int next = current + ticks;
        target.setTicksFrozen(next);
    }
}
