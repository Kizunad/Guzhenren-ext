package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

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
 * 冰雪道被动杀招：金风送霜。
 * <p>
 * 每秒维持扣费，对周围敌对单位施加寒霜（迟滞/挖掘疲劳/冻结），并造成少量普通伤害。
 * </p>
 */
public class JinFengSongShuangShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bing_xue_dao_jin_feng_song_shuang";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE_PER_SECOND = "damage_per_second";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_FATIGUE_DURATION_TICKS = "fatigue_duration_ticks";
    private static final String META_FATIGUE_AMPLIFIER = "fatigue_amplifier";
    private static final String META_FREEZE_TICKS_PER_SECOND = "freeze_ticks_per_second";
    private static final String META_MAX_FROZEN_TICKS = "max_frozen_ticks";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_RADIUS = 6.5;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE_PER_SECOND = 260.0;
    private static final double MAX_DAMAGE_PER_TARGET = 12000.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_FATIGUE_DURATION_TICKS = 60;
    private static final int DEFAULT_FATIGUE_AMPLIFIER = 0;
    private static final int DEFAULT_FREEZE_TICKS_PER_SECOND = 80;
    private static final int DEFAULT_MAX_FROZEN_TICKS = 520;

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null || user.level().isClientSide()) {
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
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE_PER_SECOND, DEFAULT_DAMAGE_PER_SECOND)
        );
        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_DURATION_TICKS, DEFAULT_SLOW_DURATION_TICKS)
        );
        final int slowAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, DEFAULT_SLOW_AMPLIFIER)
        );
        final int baseFatigueTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_FATIGUE_DURATION_TICKS,
                DEFAULT_FATIGUE_DURATION_TICKS
            )
        );
        final int fatigueAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_FATIGUE_AMPLIFIER, DEFAULT_FATIGUE_AMPLIFIER)
        );
        final int baseFreezeTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_FREEZE_TICKS_PER_SECOND,
                DEFAULT_FREEZE_TICKS_PER_SECOND
            )
        );
        final int maxFrozenTicks = Math.max(
            1,
            ShazhaoMetadataHelper.getInt(data, META_MAX_FROZEN_TICKS, DEFAULT_MAX_FROZEN_TICKS)
        );

        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseSlowTicks, selfMultiplier);
        final int fatigueTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseFatigueTicks,
            selfMultiplier
        );
        final int freezeTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseFreezeTicks, selfMultiplier);

        final AABB area = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity target : targets) {
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
            if (fatigueTicks > 0) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.DIG_SLOWDOWN,
                        fatigueTicks,
                        fatigueAmplifier,
                        true,
                        true
                    )
                );
            }
            if (freezeTicks > 0) {
                BingXueDaoShazhaoEffectHelper.addFreezeTicks(target, freezeTicks, maxFrozenTicks);
            }

            if (baseDamage <= MIN_VALUE) {
                continue;
            }
            final double multiplier = DaoHenCalculator.calculateMultiplier(user, target, DAO_TYPE);
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

