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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/**
 * 光道通用被动：持续性维持（多资源）+ 受击触发（减伤/耀目反制）。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - proc_cooldown_ticks（触发冷却）<br>
 * - projectile_only（true 表示仅对投射物伤害生效）<br>
 * - damage_reduction_ratio（基础减伤比例，最终会乘以光道道痕倍率并做上限裁剪）<br>
 * - max_reduction_ratio（可选，减伤比例上限）<br>
 * - blind_duration_ticks / blind_amplifier（可为 0，攻击者存在时施加）<br>
 * </p>
 */
public class GuangDaoSustainedHurtProcEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";

    private static final String META_PROC_COOLDOWN_TICKS = "proc_cooldown_ticks";
    private static final String META_PROJECTILE_ONLY = "projectile_only";
    private static final String META_DAMAGE_REDUCTION_RATIO = "damage_reduction_ratio";
    private static final String META_MAX_REDUCTION_RATIO = "max_reduction_ratio";
    private static final String META_BLIND_DURATION_TICKS = "blind_duration_ticks";
    private static final String META_BLIND_AMPLIFIER = "blind_amplifier";

    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_REDUCTION_RATIO = 0.0;
    private static final double DEFAULT_MAX_REDUCTION_RATIO = 0.75;

    private static final int DEFAULT_EFFECT_DURATION_TICKS = 0;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 5 * 60;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String procCooldownKey;

    public GuangDaoSustainedHurtProcEffect(
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
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim == null || source == null) {
            return damage;
        }
        if (victim.level().isClientSide()) {
            return damage;
        }
        if (!isActive(victim)) {
            return damage;
        }

        if (UsageMetadataHelper.getBoolean(usageInfo, META_PROJECTILE_ONLY, false)) {
            final Entity direct = source.getDirectEntity();
            if (!(direct instanceof Projectile)) {
                return damage;
            }
        }

        if (procCooldownKey != null && !procCooldownKey.isBlank()) {
            final int remain = GuEffectCooldownHelper.getRemainingTicks(
                victim,
                procCooldownKey
            );
            if (remain > 0) {
                return damage;
            }
        }

        final double baseReduction = UsageMetadataHelper.getDouble(
            usageInfo,
            META_DAMAGE_REDUCTION_RATIO,
            DEFAULT_REDUCTION_RATIO
        );
        final double maxReduction = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_MAX_REDUCTION_RATIO,
                DEFAULT_MAX_REDUCTION_RATIO
            )
        );

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.GUANG_DAO
        );
        final double scaledReduction = UsageMetadataHelper.clamp(
            baseReduction * multiplier,
            0.0,
            maxReduction
        );

        final LivingEntity attacker = source.getEntity() instanceof LivingEntity living
            ? living
            : null;
        if (attacker != null && attacker.isAlive() && !attacker.isAlliedTo(victim)) {
            final int blindDuration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_BLIND_DURATION_TICKS,
                    DEFAULT_EFFECT_DURATION_TICKS
                )
            );
            if (blindDuration > 0) {
                final int amplifier = Math.max(
                    0,
                    UsageMetadataHelper.getInt(
                        usageInfo,
                        META_BLIND_AMPLIFIER,
                        DEFAULT_EFFECT_AMPLIFIER
                    )
                );
                final double debuffMultiplier = DaoHenCalculator.calculateMultiplier(
                    victim,
                    attacker,
                    DaoHenHelper.DaoType.GUANG_DAO
                );
                attacker.addEffect(
                    new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        scaleDuration(blindDuration, debuffMultiplier),
                        amplifier,
                        true,
                        true
                    )
                );
            }
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
                victim,
                procCooldownKey,
                victim.tickCount + cooldownTicks
            );
        }

        return (float) (damage * (1.0 - scaledReduction));
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
