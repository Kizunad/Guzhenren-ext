package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
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
 * 雷道主动杀招：连环惊雷。
 * <p>
 * 视线锁定单体，造成普通伤害后在附近进行少量“跳电”追加打击，并施加短暂迟滞。
 * </p>
 */
public class LianHuanJingLeiShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_lian_huan_jing_lei";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LEI_DAO;

    private static final String META_RANGE = "range";
    private static final String META_CHAIN_RADIUS = "chain_radius";
    private static final String META_CHAIN_COUNT = "chain_count";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_CHAIN_DAMAGE_RATIO = "chain_damage_ratio";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 18.0;
    private static final double MIN_RANGE = 1.0;
    private static final double DEFAULT_CHAIN_RADIUS = 6.0;
    private static final double MIN_RADIUS = 0.0;
    private static final double MAX_RADIUS = 32.0;
    private static final int DEFAULT_CHAIN_COUNT = 3;
    private static final int MAX_CHAIN_COUNT = 12;
    private static final double DEFAULT_DAMAGE = 1200.0;
    private static final double DEFAULT_CHAIN_RATIO = 0.45;
    private static final double MAX_CHAIN_RATIO = 1.0;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 80;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 10000.0;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);
    private static final String MSG_NO_TARGET = "未找到可锁定目标。";

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

        if (isOnCooldown(player)) {
            return false;
        }

        final double selfMultiplier = resolveSelfMultiplier(player);
        final LivingEntity target = findPrimaryTarget(player, data, selfMultiplier);
        if (target == null) {
            player.displayClientMessage(Component.literal(MSG_NO_TARGET), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final Params params = readParams(data, selfMultiplier);
        strike(player, target, params);
        chainStrike(player, target, params);
        applyCooldown(player, data);
        return true;
    }

    private static boolean isOnCooldown(final ServerPlayer player) {
        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            COOLDOWN_KEY
        );
        if (remain <= 0) {
            return false;
        }
        player.displayClientMessage(
            Component.literal("冷却中，剩余 " + remain + "t"),
            true
        );
        return true;
    }

    private static double resolveSelfMultiplier(final ServerPlayer player) {
        return DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
        );
    }

    private static LivingEntity findPrimaryTarget(
        final ServerPlayer player,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final double baseRange = Math.max(
            MIN_RANGE,
            ShazhaoMetadataHelper.getDouble(data, META_RANGE, DEFAULT_RANGE)
        );
        final double range = baseRange * Math.max(MIN_VALUE, selfMultiplier);
        return LineOfSightTargetHelper.findTarget(player, range);
    }

    private record Params(
        double baseDamage,
        double chainRatio,
        double chainRadius,
        int chainCount,
        int baseSlowTicks,
        int slowAmplifier
    ) {}

    private static Params readParams(final ShazhaoData data, final double selfMultiplier) {
        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, DEFAULT_DAMAGE)
        );
        final double chainRatio = ShazhaoMetadataHelper.clamp(
            ShazhaoMetadataHelper.getDouble(
                data,
                META_CHAIN_DAMAGE_RATIO,
                DEFAULT_CHAIN_RATIO
            ),
            MIN_VALUE,
            MAX_CHAIN_RATIO
        );

        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_CHAIN_RADIUS,
                DEFAULT_CHAIN_RADIUS
            )
        );
        final double chainRadius = ShazhaoMetadataHelper.clamp(
            baseRadius * Math.max(MIN_VALUE, selfMultiplier),
            MIN_RADIUS,
            MAX_RADIUS
        );

        final int chainCount = Math.min(
            MAX_CHAIN_COUNT,
            Math.max(
                0,
                ShazhaoMetadataHelper.getInt(
                    data,
                    META_CHAIN_COUNT,
                    DEFAULT_CHAIN_COUNT
                )
            )
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

        return new Params(
            baseDamage,
            chainRatio,
            chainRadius,
            chainCount,
            baseSlowTicks,
            slowAmplifier
        );
    }

    private static void strike(
        final ServerPlayer player,
        final LivingEntity target,
        final Params params
    ) {
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DAO_TYPE
        );
        final double damage = ShazhaoMetadataHelper.clamp(
            params.baseDamage * Math.max(MIN_VALUE, multiplier),
            MIN_VALUE,
            MAX_DAMAGE_PER_TARGET
        );
        if (damage > MIN_VALUE) {
            target.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                (float) damage
            );
        }
        applySlow(target, params, multiplier);
    }

    private static void chainStrike(
        final ServerPlayer player,
        final LivingEntity primaryTarget,
        final Params params
    ) {
        if (params.chainRadius <= MIN_RADIUS || params.chainCount <= 0) {
            return;
        }

        final AABB box = primaryTarget.getBoundingBox().inflate(params.chainRadius);
        final List<LivingEntity> candidates = player.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e ->
                e.isAlive()
                    && e != player
                    && e != primaryTarget
                    && !e.isAlliedTo(player)
                    && player.hasLineOfSight(e)
        );
        if (candidates.isEmpty()) {
            return;
        }

        candidates.sort(Comparator.comparingDouble(primaryTarget::distanceToSqr));
        int chained = 0;
        for (LivingEntity enemy : candidates) {
            strikeChainTarget(player, enemy, params);
            chained++;
            if (chained >= params.chainCount) {
                break;
            }
        }
    }

    private static void strikeChainTarget(
        final ServerPlayer player,
        final LivingEntity target,
        final Params params
    ) {
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DAO_TYPE
        );
        final double damage = ShazhaoMetadataHelper.clamp(
            params.baseDamage
                * params.chainRatio
                * Math.max(MIN_VALUE, multiplier),
            MIN_VALUE,
            MAX_DAMAGE_PER_TARGET
        );
        if (damage > MIN_VALUE) {
            target.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                (float) damage
            );
        }
        applySlow(target, params, multiplier);
    }

    private static void applySlow(
        final LivingEntity target,
        final Params params,
        final double multiplier
    ) {
        if (params.baseSlowTicks <= 0) {
            return;
        }
        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            params.baseSlowTicks,
            multiplier
        );
        if (slowTicks <= 0) {
            return;
        }
        target.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                slowTicks,
                params.slowAmplifier,
                true,
                true
            )
        );
    }

    private static void applyCooldown(final ServerPlayer player, final ShazhaoData data) {
        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }
    }
}
