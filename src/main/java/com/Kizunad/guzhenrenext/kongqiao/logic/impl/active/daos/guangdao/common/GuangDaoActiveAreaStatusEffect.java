package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 光道通用主动：范围状态（自我/友方增益 + 敌方控制）。
 * <p>
 * 通过 metadata 配置：<br>
 * - cooldown_ticks<br>
 * - radius<br>
 * - ignore_walls（是否无视视线）<br>
 * - knockback_strength / knockback_vertical（可为 0，仅对敌方）<br>
 * - 标准一次性消耗（由 {@link GuEffectCostHelper#tryConsumeOnce} 读取）<br>
 * </p>
 */
public class GuangDaoActiveAreaStatusEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_IGNORE_WALLS = "ignore_walls";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_KNOCKBACK_VERTICAL = "knockback_vertical";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double DEFAULT_RADIUS = 0.0;
    private static final double DEFAULT_KNOCKBACK = 0.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final String cooldownKey;
    private final List<EffectSpec> selfEffects;
    private final List<EffectSpec> allyEffects;
    private final List<EffectSpec> enemyEffects;

    public GuangDaoActiveAreaStatusEffect(
        final String usageId,
        final String cooldownKey,
        final List<EffectSpec> selfEffects,
        final List<EffectSpec> allyEffects,
        final List<EffectSpec> enemyEffects
    ) {
        this.usageId = usageId;
        this.cooldownKey = cooldownKey;
        this.selfEffects = selfEffects == null ? List.of() : List.copyOf(selfEffects);
        this.allyEffects = allyEffects == null ? List.of() : List.copyOf(allyEffects);
        this.enemyEffects = enemyEffects == null ? List.of() : List.copyOf(enemyEffects);
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, cooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double baseRadius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final boolean ignoreWalls = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_IGNORE_WALLS,
            false
        );
        final double knockbackStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_STRENGTH,
                DEFAULT_KNOCKBACK
            )
        );
        final double knockbackVertical = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_VERTICAL,
                DEFAULT_KNOCKBACK
            )
        );

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.GUANG_DAO
        );
        final double radius = baseRadius * Math.max(0.0, selfMultiplier);
        if (radius > 0.0) {
            final AABB box = user.getBoundingBox().inflate(radius);
            final List<LivingEntity> targets = user.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e ->
                    e.isAlive()
                        && e != user
                        && (ignoreWalls || user.hasLineOfSight(e))
            );
            for (LivingEntity target : targets) {
                if (target.isAlliedTo(user)) {
                    applyEffects(user, target, allyEffects, usageInfo);
                    continue;
                }
                applyEffects(user, target, enemyEffects, usageInfo);
                if (knockbackStrength > 0.0) {
                    GuangDaoKnockbackHelper.applyKnockback(
                        user,
                        target,
                        knockbackStrength,
                        knockbackVertical
                    );
                }
            }
        }

        applyEffects(user, user, selfEffects, usageInfo);

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
                user,
                cooldownKey,
                user.tickCount + cooldownTicks
            );
        }
        return true;
    }

    private static void applyEffects(
        final LivingEntity caster,
        final LivingEntity target,
        final List<EffectSpec> specs,
        final NianTouData.Usage usageInfo
    ) {
        if (specs == null || specs.isEmpty()) {
            return;
        }
        final double multiplier = caster == target
            ? DaoHenCalculator.calculateSelfMultiplier(
                caster,
                DaoHenHelper.DaoType.GUANG_DAO
            )
            : DaoHenCalculator.calculateMultiplier(
                caster,
                target,
                DaoHenHelper.DaoType.GUANG_DAO
            );

        for (EffectSpec spec : specs) {
            final int baseDuration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            if (baseDuration <= 0) {
                continue;
            }
            final int duration = scaleDuration(baseDuration, multiplier);
            if (duration <= 0) {
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
            target.addEffect(
                new MobEffectInstance(
                    spec.effect(),
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }

    private static int scaleDuration(final int baseTicks, final double multiplier) {
        final double scaled = baseTicks * Math.max(0.0, multiplier);
        return (int) UsageMetadataHelper.clamp(
            Math.round(scaled),
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );
    }

    /**
     * 固定效果描述（duration/amplifier 由注册时指定，强度由光道道痕倍率动态放大 duration）。
     */
    public record EffectSpec(
        Holder<net.minecraft.world.effect.MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}
}
