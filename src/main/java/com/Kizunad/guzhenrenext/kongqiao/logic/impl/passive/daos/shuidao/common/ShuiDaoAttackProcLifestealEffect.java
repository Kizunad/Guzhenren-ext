package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common;

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
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 水道被动：攻击触发（概率）- 吸血/回春 + 可选减益 + 可选附加普通伤害。
 * <p>
 * 用于“血水”等偏邪门的水道蛊虫：以恢复为主，伤害保持在普通伤害范畴内。
 * </p>
 *
 * <p>metadata:</p>
 * <ul>
 *   <li>proc_chance</li>
 *   <li>heal_amount（触发时治疗生命）</li>
 *   <li>effect_duration_ticks / effect_amplifier（可选减益）</li>
 *   <li>extra_physical_damage（可选附伤）</li>
 *   <li>（消耗）niantou_cost / jingli_cost / hunpo_cost / zhenyuan_base_cost</li>
 * </ul>
 */
public class ShuiDaoAttackProcLifestealEffect implements IGuEffect {

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";

    private static final double DEFAULT_PROC_CHANCE = 0.10;
    private static final double MAX_HEAL = 100.0;

    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private final String usageId;
    private final List<Holder<MobEffect>> debuffs;

    public ShuiDaoAttackProcLifestealEffect(
        final String usageId,
        final List<Holder<MobEffect>> debuffs
    ) {
        this.usageId = usageId;
        this.debuffs = debuffs == null ? List.of() : List.copyOf(debuffs);
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
            UsageMetadataHelper.getDouble(usageInfo, META_PROC_CHANCE, DEFAULT_PROC_CHANCE),
            0.0,
            1.0
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            attacker,
            DaoHenHelper.DaoType.SHUI_DAO
        );
        final double chance = DaoHenEffectScalingHelper.scaleChance(baseChance, selfMultiplier);
        if (attacker.getRandom().nextDouble() > chance) {
            return damage;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
            return damage;
        }

        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (baseHeal > 0.0) {
            final double heal = Math.min(
                MAX_HEAL,
                DaoHenEffectScalingHelper.scaleValue(baseHeal, selfMultiplier)
            );
            if (heal > 0.0) {
                attacker.heal((float) heal);
            }
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
        if (scaledDuration > 0) {
            for (Holder<MobEffect> effect : debuffs) {
                if (effect == null) {
                    continue;
                }
                target.addEffect(
                    new MobEffectInstance(effect, scaledDuration, amplifier, true, true)
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
                DaoHenHelper.DaoType.SHUI_DAO
            );
            final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(attacker);
            target.hurt(source, (float) (baseExtra * multiplier));
        }

        return damage;
    }
}

