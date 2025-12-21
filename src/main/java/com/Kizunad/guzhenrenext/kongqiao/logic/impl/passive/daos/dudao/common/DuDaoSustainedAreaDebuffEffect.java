package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
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
import net.minecraft.world.phys.AABB;

/**
 * 毒道通用被动：持续维持（多资源）+ 范围削弱（可选每秒普通伤害）。
 * <p>
 * 典型用途：臭气/腐蚀领域等，以控场与削弱为主；伤害采用普通伤害，避免法术伤害穿甲过强。
 * </p>
 *
 * <p>metadata:</p>
 * <ul>
 *   <li>zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second</li>
 *   <li>radius</li>
 *   <li>effect_duration_ticks / effect_amplifier</li>
 *   <li>extra_physical_damage_per_second（可选）</li>
 * </ul>
 */
public class DuDaoSustainedAreaDebuffEffect implements IGuEffect {

    private static final String META_RADIUS = "radius";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_EXTRA_PHYSICAL_DAMAGE_PER_SECOND =
        "extra_physical_damage_per_second";

    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_RADIUS = 0.0;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 40;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private static final double MAX_RADIUS = 24.0;
    private static final double MAX_DAMAGE_PER_SECOND = 2000.0;

    private final String usageId;
    private final List<Holder<MobEffect>> debuffs;

    public DuDaoSustainedAreaDebuffEffect(
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
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.DU_DAO
        );
        final double clamped = DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);

        final double baseRadius = UsageMetadataHelper.clamp(
            Math.max(0.0, UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)),
            0.0,
            MAX_RADIUS
        );
        final double radius = UsageMetadataHelper.clamp(baseRadius * clamped, 0.0, MAX_RADIUS);

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(duration, clamped);
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );

        final double baseDamage = UsageMetadataHelper.clamp(
            Math.max(
                0.0,
                UsageMetadataHelper.getDouble(
                    usageInfo,
                    META_EXTRA_PHYSICAL_DAMAGE_PER_SECOND,
                    0.0
                )
            ),
            0.0,
            MAX_DAMAGE_PER_SECOND
        );
        final DamageSource source = baseDamage > 0.0
            ? PhysicalDamageSourceHelper.buildPhysicalDamageSource(user)
            : null;

        for (LivingEntity target : listTargets(user, radius)) {
            if (scaledDuration > 0 && !debuffs.isEmpty()) {
                for (Holder<MobEffect> effect : debuffs) {
                    if (effect == null) {
                        continue;
                    }
                    target.addEffect(
                        new MobEffectInstance(effect, scaledDuration, amplifier, true, true)
                    );
                }
            }
            if (baseDamage > 0.0 && source != null) {
                final double m = DaoHenCalculator.calculateMultiplier(
                    user,
                    target,
                    DaoHenHelper.DaoType.DU_DAO
                );
                target.hurt(source, (float) (baseDamage * m));
            }
        }

        setActive(user, true);
    }

    private static List<LivingEntity> listTargets(
        final LivingEntity user,
        final double radius
    ) {
        if (radius <= 0.0) {
            return List.of();
        }
        final AABB box = user.getBoundingBox().inflate(radius);
        return user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );
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

