package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 光道通用被动：持续性维持（多资源）+ 攻击触发（增伤/附加控制）。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - proc_cooldown_ticks（触发冷却）<br>
 * - damage_multiplier（乘区，最终为 damage * (1 + damage_multiplier * 道痕倍率)）<br>
 * - bonus_damage（加区，最终为 damage + bonus_damage * 道痕倍率）<br>
 * - burn_seconds（点燃秒数，可为 0）<br>
 * - glowing_duration_ticks（发光持续时间，可为 0）<br>
 * - weakness_duration_ticks / weakness_amplifier（可为 0）<br>
 * </p>
 */
public class GuangDaoSustainedAttackProcEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";

    private static final String META_PROC_COOLDOWN_TICKS = "proc_cooldown_ticks";
    private static final String META_DAMAGE_MULTIPLIER = "damage_multiplier";
    private static final String META_BONUS_DAMAGE = "bonus_damage";
    private static final String META_BURN_SECONDS = "burn_seconds";
    private static final String META_GLOWING_DURATION_TICKS = "glowing_duration_ticks";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_WEAKNESS_DURATION_TICKS = "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 0.0;
    private static final double DEFAULT_BONUS_DAMAGE = 0.0;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 0;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 5 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String procCooldownKey;

    public GuangDaoSustainedAttackProcEffect(
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
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            setActive(user, false);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST
            )
        );

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            setActive(user, false);
            return;
        }

        setActive(user, true);
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker == null || target == null) {
            return damage;
        }
        if (attacker.level().isClientSide()) {
            return damage;
        }
        if (!isActive(attacker)) {
            return damage;
        }
        if (procCooldownKey != null && !procCooldownKey.isBlank()) {
            final int remain = GuEffectCooldownHelper.getRemainingTicks(
                attacker,
                procCooldownKey
            );
            if (remain > 0) {
                return damage;
            }
        }

        final double daoMultiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.GUANG_DAO
        );

        final double damageMultiplier = UsageMetadataHelper.getDouble(
            usageInfo,
            META_DAMAGE_MULTIPLIER,
            DEFAULT_DAMAGE_MULTIPLIER
        );
        final double bonusDamage = UsageMetadataHelper.getDouble(
            usageInfo,
            META_BONUS_DAMAGE,
            DEFAULT_BONUS_DAMAGE
        );

        double next = damage;
        if (Double.compare(damageMultiplier, 0.0) != 0) {
            next = next * (1.0 + damageMultiplier * daoMultiplier);
        }
        if (Double.compare(bonusDamage, 0.0) != 0) {
            next = next + bonusDamage * daoMultiplier;
        }

        final int burnSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_BURN_SECONDS, 0)
        );
        if (burnSeconds > 0) {
            target.igniteForSeconds(burnSeconds);
        }

        final int glowingDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_GLOWING_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        if (glowingDuration > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.GLOWING,
                    scaleDuration(glowingDuration, daoMultiplier),
                    0,
                    true,
                    true
                )
            );
        }

        final int weaknessDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_WEAKNESS_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        if (weaknessDuration > 0) {
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_WEAKNESS_AMPLIFIER,
                    DEFAULT_EFFECT_AMPLIFIER
                )
            );
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    scaleDuration(weaknessDuration, daoMultiplier),
                    amplifier,
                    true,
                    true
                )
            );
        }

        final int slowDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SLOW_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        if (slowDuration > 0) {
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_SLOW_AMPLIFIER,
                    DEFAULT_EFFECT_AMPLIFIER
                )
            );
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    scaleDuration(slowDuration, daoMultiplier),
                    amplifier,
                    true,
                    true
                )
            );
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_PROC_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0 && procCooldownKey != null && !procCooldownKey.isBlank()) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                attacker,
                procCooldownKey,
                attacker.tickCount + cooldownTicks
            );
        }

        return (float) next;
    }

    private static int scaleDuration(final int baseTicks, final double multiplier) {
        final double scaled = baseTicks * Math.max(0.0, multiplier);
        return (int) UsageMetadataHelper.clamp(
            Math.round(scaled),
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );
    }

    private boolean isActive(final LivingEntity user) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        return actives != null && actives.isActive(usageId);
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives == null) {
            return;
        }
        if (active) {
            actives.add(usageId);
            return;
        }
        actives.remove(usageId);
    }
}
