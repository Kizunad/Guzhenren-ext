package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

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
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 冰雪道主动杀招：霜剑回潮。
 * <p>
 * 视线锁定单体，以其为中心霜潮爆发：范围普通伤害 + 强迟滞 + 冻结（偏范围爆发）。
 * </p>
 */
public class ShuangJianHuiChaoShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bing_xue_dao_shuang_jian_hui_chao";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_RANGE = "range";
    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_FREEZE_TICKS = "freeze_ticks";
    private static final String META_MAX_FROZEN_TICKS = "max_frozen_ticks";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_RANGE = 20.0;
    private static final double MIN_RANGE = 1.0;
    private static final double DEFAULT_RADIUS = 6.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 6500.0;
    private static final double MAX_DAMAGE_PER_TARGET = 60000.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 220;
    private static final int DEFAULT_SLOW_AMPLIFIER = 1;
    private static final int DEFAULT_FREEZE_TICKS = 160;
    private static final int DEFAULT_MAX_FROZEN_TICKS = 1200;
    private static final int DEFAULT_COOLDOWN_TICKS = 2600;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null || player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(player, COOLDOWN_KEY);
        if (remain > 0) {
            player.displayClientMessage(Component.literal("冷却中，剩余 " + remain + "t"), true);
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE);
        final double baseRange = Math.max(
            MIN_RANGE,
            ShazhaoMetadataHelper.getDouble(data, META_RANGE, DEFAULT_RANGE)
        );
        final double range = baseRange * DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
        final LivingEntity anchor = LineOfSightTargetHelper.findTarget(player, range);
        if (anchor == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);

        final AABB area = anchor.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = anchor.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_DURATION_TICKS, DEFAULT_SLOW_DURATION_TICKS)
        );
        final int slowAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, DEFAULT_SLOW_AMPLIFIER)
        );
        final int baseFreezeTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_FREEZE_TICKS, DEFAULT_FREEZE_TICKS)
        );
        final int maxFrozenTicks = Math.max(
            1,
            ShazhaoMetadataHelper.getInt(data, META_MAX_FROZEN_TICKS, DEFAULT_MAX_FROZEN_TICKS)
        );

        for (LivingEntity target : targets) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);
            final double damage = ShazhaoMetadataHelper.clamp(
                baseDamage * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (damage > MIN_VALUE) {
                target.hurt(PhysicalDamageSourceHelper.buildPhysicalDamageSource(player), (float) damage);
            }

            final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseSlowTicks, multiplier);
            if (slowTicks > 0) {
                target.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowAmplifier, true, true)
                );
            }

            final int freezeTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseFreezeTicks, multiplier);
            if (freezeTicks > 0) {
                BingXueDaoShazhaoEffectHelper.addFreezeTicks(target, freezeTicks, maxFrozenTicks);
            }
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
        return true;
    }
}

