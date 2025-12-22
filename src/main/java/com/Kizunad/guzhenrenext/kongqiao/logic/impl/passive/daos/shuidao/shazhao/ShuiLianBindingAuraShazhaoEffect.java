package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 水道被动杀招：水链缚敌。
 * <p>
 * 每秒维持扣费，对周围敌对单位施加迟滞，并造成少量普通伤害（受道痕增伤算法影响）。
 * </p>
 */
public class ShuiLianBindingAuraShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_shui_lian_binding_aura";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.SHUI_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE_PER_SECOND = "damage_per_second";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final double DEFAULT_RADIUS = 5.0;
    private static final double DEFAULT_DAMAGE_PER_SECOND = 4.0;
    private static final double MIN_RADIUS = 1.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 200.0;

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
        final double radius = baseRadius * Math.max(MIN_VALUE, selfMultiplier);

        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_DAMAGE_PER_SECOND,
                DEFAULT_DAMAGE_PER_SECOND
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
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_AMPLIFIER,
                DEFAULT_SLOW_AMPLIFIER
            )
        );

        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseSlowTicks,
            selfMultiplier
        );

        final AABB area = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity target : targets) {
            if (slowTicks > 0) {
                target.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowAmplifier, true, true)
                );
            }
            if (baseDamage <= MIN_VALUE) {
                continue;
            }
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DAO_TYPE
            );
            final double finalDamage = ShazhaoMetadataHelper.clamp(
                baseDamage * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (finalDamage > MIN_VALUE) {
                target.hurt(
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(user),
                    (float) finalDamage
                );
            }
        }
    }
}

