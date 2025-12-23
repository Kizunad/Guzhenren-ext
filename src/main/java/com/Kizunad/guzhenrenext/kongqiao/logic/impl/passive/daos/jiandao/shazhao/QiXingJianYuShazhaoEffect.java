package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 剑道主动杀招：七星剑雨。
 * <p>
 * 七星剑匣吐出剑雨，覆盖一域：对周围多个敌对目标造成普通伤害并施加短暂迟滞。
 * </p>
 */
public class QiXingJianYuShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_qi_xing_jian_yu";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_MAX_TARGETS = "max_targets";
    private static final String META_DAMAGE = "damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 6.0;
    private static final double MIN_RADIUS = 1.0;

    private static final int DEFAULT_MAX_TARGETS = 7;
    private static final int MIN_MAX_TARGETS = 1;
    private static final int MAX_MAX_TARGETS = 24;

    private static final double DEFAULT_DAMAGE = 800.0;
    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 20000.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 900;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(player, COOLDOWN_KEY);
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
        );
        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * selfMultiplier;

        final List<LivingEntity> targets = findTargets(player, radius);
        if (targets.isEmpty()) {
            player.displayClientMessage(Component.literal("附近没有可命中的敌人。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final int maxTargets = clampInt(
            ShazhaoMetadataHelper.getInt(data, META_MAX_TARGETS, DEFAULT_MAX_TARGETS),
            MIN_MAX_TARGETS,
            MAX_MAX_TARGETS
        );
        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_DURATION_TICKS,
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int slowAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, DEFAULT_SLOW_AMPLIFIER)
        );

        int hits = 0;
        for (LivingEntity target : targets) {
            if (hits >= maxTargets) {
                break;
            }
            if (target == null) {
                continue;
            }
            hits += 1;
            strikeTarget(player, target, baseDamage, baseSlowTicks, slowAmplifier);
        }

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return hits > 0;
    }

    private static int clampInt(final int value, final int min, final int max) {
        return Math.min(Math.max(value, min), max);
    }

    private static List<LivingEntity> findTargets(
        final ServerPlayer player,
        final double radius
    ) {
        final AABB area = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        return targets;
    }

    private static void strikeTarget(
        final ServerPlayer player,
        final LivingEntity target,
        final double baseDamage,
        final int baseSlowTicks,
        final int slowAmplifier
    ) {
        final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);
        final double damage = ShazhaoMetadataHelper.clamp(
            baseDamage * Math.max(MIN_VALUE, multiplier),
            MIN_VALUE,
            MAX_DAMAGE_PER_TARGET
        );
        if (damage > MIN_VALUE) {
            target.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                (float) damage
            );
        }

        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseSlowTicks,
            multiplier
        );
        if (slowTicks > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowTicks,
                    slowAmplifier,
                    true,
                    true
                )
            );
        }
    }
}
