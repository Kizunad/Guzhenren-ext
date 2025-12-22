package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao;

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
 * 雷道主动杀招：电弧爆。
 * <p>
 * 以自身为中心爆发电弧，对范围内多个敌对生灵造成普通伤害并施加迟滞。
 * </p>
 */
public class DianHuBurstShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_dian_hu_burst";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LEI_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_MAX_TARGETS = "max_targets";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 3.5;
    private static final double MIN_RADIUS = 0.0;
    private static final double MAX_RADIUS = 32.0;
    private static final int DEFAULT_MAX_TARGETS = 5;
    private static final int MAX_MAX_TARGETS = 36;
    private static final double DEFAULT_DAMAGE = 80.0;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 140;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 10000.0;

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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            COOLDOWN_KEY
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
        );
        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = ShazhaoMetadataHelper.clamp(
            baseRadius * selfMultiplier,
            MIN_RADIUS,
            MAX_RADIUS
        );
        if (radius <= MIN_RADIUS) {
            return false;
        }

        final int maxTargets = Math.min(
            MAX_MAX_TARGETS,
            Math.max(
                0,
                ShazhaoMetadataHelper.getInt(
                    data,
                    META_MAX_TARGETS,
                    DEFAULT_MAX_TARGETS
                )
            )
        );
        if (maxTargets <= 0) {
            return false;
        }

        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, DEFAULT_DAMAGE)
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

        final AABB box = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> enemies = player.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player) && player.hasLineOfSight(e)
        );
        if (enemies.isEmpty()) {
            return false;
        }

        enemies.sort(Comparator.comparingDouble(player::distanceToSqr));
        int hit = 0;
        for (LivingEntity enemy : enemies) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                player,
                enemy,
                DAO_TYPE
            );
            final double damage = ShazhaoMetadataHelper.clamp(
                baseDamage * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (damage > MIN_VALUE) {
                enemy.hurt(
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                    (float) damage
                );
            }

            if (baseSlowTicks > 0) {
                final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
                    baseSlowTicks,
                    multiplier
                );
                if (slowTicks > 0) {
                    enemy.addEffect(
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

            hit++;
            if (hit >= maxTargets) {
                break;
            }
        }

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
        return true;
    }
}
