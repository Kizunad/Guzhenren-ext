package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道通用被动：受伤触发冰障（概率）。
 * <p>
 * 触发后：按比例减伤（受冰雪道道痕自倍率影响，并有下限保护），并对攻击者施加寒意反噬（冻结/缓慢）。
 * </p>
 */
public class BingXueDaoFrostBarrierOnHurtEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_NIANTOU_COST = "niantou_cost";
    private static final String META_JINGLI_COST = "jingli_cost";
    private static final String META_HUNPO_COST = "hunpo_cost";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_RETALIATE_FREEZE_TICKS = "retaliate_freeze_ticks";
    private static final String META_RETALIATE_SLOW_DURATION_TICKS =
        "retaliate_slow_duration_ticks";
    private static final String META_RETALIATE_SLOW_AMPLIFIER =
        "retaliate_slow_amplifier";

    private static final double DEFAULT_PROC_CHANCE = 0.18;
    private static final int DEFAULT_COOLDOWN_TICKS = 120;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.85;
    private static final int DEFAULT_RETALIATE_FREEZE_TICKS = 80;
    private static final int DEFAULT_RETALIATE_SLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_RETALIATE_SLOW_AMPLIFIER = 0;

    private static final double MIN_DAMAGE_MULTIPLIER = 0.35;

    private final String usageId;
    private final String nbtCooldownKey;

    public BingXueDaoFrostBarrierOnHurtEffect(
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

        final double niantouCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST, 0.0)
        );
        if (niantouCost > 0.0 && NianTouHelper.getAmount(victim) < niantouCost) {
            return damage;
        }
        final double jingliCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        if (jingliCost > 0.0 && JingLiHelper.getAmount(victim) < jingliCost) {
            return damage;
        }
        final double hunpoCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(victim) < hunpoCost) {
            return damage;
        }
        if (niantouCost > 0.0) {
            NianTouHelper.modify(victim, -niantouCost);
        }
        if (jingliCost > 0.0) {
            JingLiHelper.modify(victim, -jingliCost);
        }
        if (hunpoCost > 0.0) {
            HunPoHelper.modify(victim, -hunpoCost);
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

        applyRetaliation(source, usageInfo);

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_MULTIPLIER,
                DEFAULT_DAMAGE_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double daoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.BING_XUE_DAO
        );
        final double scaledMultiplier = UsageMetadataHelper.clamp(
            1.0 - (1.0 - baseMultiplier) * daoMultiplier,
            MIN_DAMAGE_MULTIPLIER,
            1.0
        );
        return (float) (damage * scaledMultiplier);
    }

    private static void applyRetaliation(
        final DamageSource source,
        final NianTouData.Usage usageInfo
    ) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        final int freezeTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RETALIATE_FREEZE_TICKS,
                DEFAULT_RETALIATE_FREEZE_TICKS
            )
        );
        if (freezeTicks > 0) {
            attacker.setTicksFrozen(attacker.getTicksFrozen() + freezeTicks);
        }

        final Holder<MobEffect> slow = MobEffects.MOVEMENT_SLOWDOWN;
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RETALIATE_SLOW_DURATION_TICKS,
                DEFAULT_RETALIATE_SLOW_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RETALIATE_SLOW_AMPLIFIER,
                DEFAULT_RETALIATE_SLOW_AMPLIFIER
            )
        );
        if (slow != null && duration > 0) {
            attacker.addEffect(
                new MobEffectInstance(slow, duration, amplifier, true, true)
            );
        }
    }
}
