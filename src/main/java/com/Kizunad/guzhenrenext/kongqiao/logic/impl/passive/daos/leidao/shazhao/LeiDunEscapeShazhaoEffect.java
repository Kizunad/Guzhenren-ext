package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/**
 * 雷道主动杀招：雷遁。
 * <p>
 * 小幅位移并获得短时速度/抗性，偏向保命与调整站位。
 * </p>
 */
public class LeiDunEscapeShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_lei_dun_escape";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LEI_DAO;

    private static final String META_DASH_STRENGTH = "dash_strength";
    private static final String META_SPEED_DURATION_TICKS = "speed_duration_ticks";
    private static final String META_SPEED_AMPLIFIER = "speed_amplifier";
    private static final String META_RESIST_DURATION_TICKS = "resist_duration_ticks";
    private static final String META_RESIST_AMPLIFIER = "resist_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_DASH_STRENGTH = 1.2;
    private static final double MAX_DASH_STRENGTH = 3.0;
    private static final int DEFAULT_SPEED_DURATION_TICKS = 80;
    private static final int DEFAULT_SPEED_AMPLIFIER = 0;
    private static final int DEFAULT_RESIST_DURATION_TICKS = 60;
    private static final int DEFAULT_RESIST_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 240;

    private static final double MIN_VALUE = 0.0;

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

        final double baseStrength = ShazhaoMetadataHelper.clamp(
            ShazhaoMetadataHelper.getDouble(data, META_DASH_STRENGTH, DEFAULT_DASH_STRENGTH),
            MIN_VALUE,
            MAX_DASH_STRENGTH
        );
        final double strength = baseStrength * selfMultiplier;
        final Vec3 dir = player.getViewVector(1.0F).normalize();
        player.setDeltaMovement(player.getDeltaMovement().add(dir.scale(strength)));
        player.hurtMarked = true;

        final int baseSpeedTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SPEED_DURATION_TICKS,
                DEFAULT_SPEED_DURATION_TICKS
            )
        );
        final int speedTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseSpeedTicks,
            selfMultiplier
        );
        final int speedAmp = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SPEED_AMPLIFIER, DEFAULT_SPEED_AMPLIFIER)
        );
        if (speedTicks > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    speedTicks,
                    speedAmp,
                    true,
                    true
                )
            );
        }

        final int baseResistTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESIST_DURATION_TICKS,
                DEFAULT_RESIST_DURATION_TICKS
            )
        );
        final int resistTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseResistTicks,
            selfMultiplier
        );
        final int resistAmp = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESIST_AMPLIFIER,
                DEFAULT_RESIST_AMPLIFIER
            )
        );
        if (resistTicks > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    resistTicks,
                    resistAmp,
                    true,
                    true
                )
            );
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

