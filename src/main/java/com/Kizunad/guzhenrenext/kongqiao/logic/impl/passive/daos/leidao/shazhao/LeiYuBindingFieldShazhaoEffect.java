package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 雷道被动杀招：雷域缚敌。
 * <p>
 * 每秒维持扣费，对周身一定范围内的敌对生灵造成普通伤害并施加迟滞。
 * </p>
 */
public class LeiYuBindingFieldShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_lei_yu_binding_field";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LEI_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_MAX_TARGETS = "max_targets";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final double DEFAULT_RADIUS = 5.0;
    private static final double MIN_RADIUS = 0.0;
    private static final int DEFAULT_MAX_TARGETS = 4;
    private static final int MAX_MAX_TARGETS = 24;
    private static final double DEFAULT_DAMAGE = 45.0;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 40;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 2000.0;

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }

        if (!ShazhaoCostHelper.tryConsumeSustain(user, data)) {
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DAO_TYPE)
        );

        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * selfMultiplier;
        if (radius <= MIN_RADIUS) {
            return;
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
            return;
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

        final AABB box = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> enemies = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e ->
                e.isAlive()
                    && e != user
                    && !e.isAlliedTo(user)
                    && user.hasLineOfSight(e)
        );
        if (enemies.isEmpty()) {
            return;
        }

        enemies.sort(Comparator.comparingDouble(user::distanceToSqr));
        int hit = 0;
        for (LivingEntity enemy : enemies) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
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
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(user),
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
    }
}
