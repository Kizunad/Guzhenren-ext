package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 剑道主动杀招：剑痕穿刺。
 * <p>
 * 视线锁定单体，造成普通伤害并施加迟滞；同时给自身短暂提速，方便追击。
 * </p>
 */
public class JianHenPierceShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jian_hen_pierce";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_RANGE = "range";
    private static final String META_DAMAGE = "damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_SELF_SPEED_DURATION_TICKS =
        "self_speed_duration_ticks";
    private static final String META_SELF_SPEED_AMPLIFIER = "self_speed_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 17.0;
    private static final double MIN_RANGE = 1.0;
    private static final double DEFAULT_DAMAGE = 120.0;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE = 3000.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 120;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_SELF_SPEED_DURATION_TICKS = 100;
    private static final int DEFAULT_SELF_SPEED_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 320;

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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE);
        final double baseRange = Math.max(
            MIN_RANGE,
            ShazhaoMetadataHelper.getDouble(data, META_RANGE, DEFAULT_RANGE)
        );
        final double range = baseRange * DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);
        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final double damage = ShazhaoMetadataHelper.clamp(
            baseDamage * Math.max(MIN_VALUE, multiplier),
            MIN_VALUE,
            MAX_DAMAGE
        );
        if (damage > MIN_VALUE) {
            target.hurt(
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                (float) damage
            );
        }

        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_DURATION_TICKS, DEFAULT_SLOW_DURATION_TICKS)
        );
        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseSlowTicks, multiplier);
        final int slowAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, DEFAULT_SLOW_AMPLIFIER)
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

        final int baseSpeedTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SELF_SPEED_DURATION_TICKS,
                DEFAULT_SELF_SPEED_DURATION_TICKS
            )
        );
        final int speedTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseSpeedTicks, selfMultiplier);
        final int speedAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SELF_SPEED_AMPLIFIER,
                DEFAULT_SELF_SPEED_AMPLIFIER
            )
        );
        if (speedTicks > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    speedTicks,
                    speedAmplifier,
                    true,
                    true
                )
            );
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

