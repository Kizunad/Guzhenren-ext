package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 雷道主动：雷翼飞跃 - 短时间机动爆发（冲刺 + 缓降/短暂腾空）。
 * <p>
 * 该技能不造成伤害，定位为机动/脱离/追击工具。
 * </p>
 */
public class LeiDaoActiveWingLeapEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_DASH_STRENGTH = "dash_strength";
    public static final String META_LEVITATION_DURATION_TICKS = "levitation_duration_ticks";
    public static final String META_SPEED_DURATION_TICKS = "speed_duration_ticks";
    public static final String META_SPEED_AMPLIFIER = "speed_amplifier";
    public static final String META_SLOW_FALL_DURATION_TICKS = "slow_fall_duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 280;

    private static final double DEFAULT_DASH_STRENGTH = 0.7;
    private static final double MAX_DASH_STRENGTH = 2.2;

    private static final int DEFAULT_LEVITATION_DURATION_TICKS = 20;
    private static final int DEFAULT_SPEED_DURATION_TICKS = 80;
    private static final int DEFAULT_SPEED_AMPLIFIER = 1;
    private static final int DEFAULT_SLOW_FALL_DURATION_TICKS = 120;

    private final String usageId;
    private final String nbtCooldownKey;

    public LeiDaoActiveWingLeapEffect(final String usageId, final String nbtCooldownKey) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, nbtCooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LEI_DAO
        );

        final double dashStrength = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(
                Math.max(
                    0.0,
                    UsageMetadataHelper.getDouble(
                        usageInfo,
                        META_DASH_STRENGTH,
                        DEFAULT_DASH_STRENGTH
                    )
                ),
                selfMultiplier
            ),
            0.0,
            MAX_DASH_STRENGTH
        );
        if (dashStrength > 0.0) {
            final Vec3 dir = player.getViewVector(1.0F);
            player.push(dir.x * dashStrength, 0.0, dir.z * dashStrength);
            player.hasImpulse = true;
        }

        final int levDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_LEVITATION_DURATION_TICKS,
                    DEFAULT_LEVITATION_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        if (levDuration > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.LEVITATION,
                    levDuration,
                    0,
                    true,
                    true
                )
            );
        }

        final int speedDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_SPEED_DURATION_TICKS,
                    DEFAULT_SPEED_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        final int speedAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SPEED_AMPLIFIER,
                DEFAULT_SPEED_AMPLIFIER
            )
        );
        if (speedDuration > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    speedDuration,
                    speedAmplifier,
                    true,
                    true
                )
            );
        }

        final int slowFallDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_SLOW_FALL_DURATION_TICKS,
                    DEFAULT_SLOW_FALL_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        if (slowFallDuration > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.SLOW_FALLING,
                    slowFallDuration,
                    0,
                    true,
                    true
                )
            );
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }
}

