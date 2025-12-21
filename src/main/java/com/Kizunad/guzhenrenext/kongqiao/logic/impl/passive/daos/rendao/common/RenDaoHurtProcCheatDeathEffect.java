package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
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
 * 人道通用被动：濒死触发（概率）“保命”。
 * <p>
 * 典型用于表现“起死人”类特质：仅在本次伤害将导致死亡时触发，
 * 通过消耗资源与冷却门槛，抵消本次伤害并给予恢复/增益。
 * </p>
 * <p>
 * 通过 metadata 配置：
 * <ul>
 *   <li>proc_chance / cooldown_ticks</li>
 *   <li>survival_health（触发后至少保留的生命值）</li>
 *   <li>heal_amount / restore_jingli / restore_hunpo / restore_niantou</li>
 *   <li>可选：由注册时注入 EffectSpec，并从 metadata 读取 duration/amplifier</li>
 *   <li>一次性消耗：由 {@link GuEffectCostHelper#tryConsumeOnce} 读取</li>
 * </ul>
 * </p>
 */
public class RenDaoHurtProcCheatDeathEffect implements IGuEffect {

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private static final String META_PROC_CHANCE = "proc_chance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_SURVIVAL_HEALTH = "survival_health";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_RESTORE_JINGLI = "restore_jingli";
    private static final String META_RESTORE_HUNPO = "restore_hunpo";
    private static final String META_RESTORE_NIANTOU = "restore_niantou";

    private static final double DEFAULT_PROC_CHANCE = 1.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 60;
    private static final double DEFAULT_SURVIVAL_HEALTH = 1.0;

    private static final double MAX_HEAL = 200.0;
    private static final double MAX_RESTORE = 200.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public RenDaoHurtProcCheatDeathEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.effects = effects == null ? List.of() : List.copyOf(effects);
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

        if (!isFatal(victim, damage)) {
            return damage;
        }

        if (GuEffectCooldownHelper.isOnCooldown(victim, nbtCooldownKey)) {
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

        if (!GuEffectCostHelper.tryConsumeOnce(null, victim, usageInfo)) {
            return damage;
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.REN_DAO
        );
        applySurvival(victim, usageInfo);
        applyRecovery(victim, usageInfo, selfMultiplier);
        applyEffects(victim, usageInfo, selfMultiplier);

        return 0.0f;
    }

    private static boolean isFatal(final LivingEntity victim, final float damage) {
        if (victim == null) {
            return false;
        }
        if (damage <= 0.0f) {
            return false;
        }
        final double effectiveHealth =
            victim.getHealth() + victim.getAbsorptionAmount();
        return effectiveHealth - damage <= 0.0;
    }

    private static void applySurvival(
        final LivingEntity victim,
        final NianTouData.Usage usageInfo
    ) {
        final double survivalHealth = Math.max(
            0.1,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_SURVIVAL_HEALTH,
                DEFAULT_SURVIVAL_HEALTH
            )
        );
        victim.setHealth((float) Math.min(victim.getMaxHealth(), survivalHealth));
    }

    private static void applyRecovery(
        final LivingEntity victim,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double healBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (healBase > 0.0) {
            final double heal = Math.min(
                MAX_HEAL,
                DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier)
            );
            if (heal > 0.0) {
                victim.heal((float) heal);
            }
        }

        final double restoreJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_JINGLI, 0.0)
        );
        if (restoreJingli > 0.0) {
            final double amount = Math.min(
                MAX_RESTORE,
                DaoHenEffectScalingHelper.scaleValue(restoreJingli, selfMultiplier)
            );
            if (amount > 0.0) {
                JingLiHelper.modify(victim, amount);
            }
        }

        final double restoreHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_HUNPO, 0.0)
        );
        if (restoreHunpo > 0.0) {
            final double amount = Math.min(
                MAX_RESTORE,
                DaoHenEffectScalingHelper.scaleValue(restoreHunpo, selfMultiplier)
            );
            if (amount > 0.0) {
                HunPoHelper.modify(victim, amount);
            }
        }

        final double restoreNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_NIANTOU, 0.0)
        );
        if (restoreNianTou > 0.0) {
            final double amount = Math.min(
                MAX_RESTORE,
                DaoHenEffectScalingHelper.scaleValue(restoreNianTou, selfMultiplier)
            );
            if (amount > 0.0) {
                NianTouHelper.modify(victim, amount);
            }
        }
    }

    private void applyEffects(
        final LivingEntity victim,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (effects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : effects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            if (duration <= 0) {
                continue;
            }
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                selfMultiplier
            );
            if (scaledDuration <= 0) {
                continue;
            }
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            victim.addEffect(
                new MobEffectInstance(
                    spec.effect(),
                    scaledDuration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }
}

