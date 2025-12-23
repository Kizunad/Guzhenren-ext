package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

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

/**
 * 冰雪道主动杀招：冰锥寒盾。
 * <p>
 * 主动护身：获得吸收护盾，并刷新短暂抗性/火抗；数值随【冰雪】道痕动态变化。
 * </p>
 */
public class BingZhuiHanDunShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bing_xue_dao_bing_zhui_han_dun";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_SELF_ABSORPTION_ADD = "self_absorption_add";
    private static final String META_SELF_ABSORPTION_MAX = "self_absorption_max";
    private static final String META_RESISTANCE_DURATION_TICKS = "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER = "resistance_amplifier";
    private static final String META_FIRE_RESISTANCE_DURATION_TICKS = "fire_resistance_duration_ticks";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_SELF_ABSORPTION_ADD = 6.0;
    private static final double DEFAULT_SELF_ABSORPTION_MAX = 20.0;
    private static final double MAX_SELF_ABSORPTION_ADD = 40.0;
    private static final double MAX_SELF_ABSORPTION_MAX = 80.0;

    private static final int DEFAULT_RESISTANCE_DURATION_TICKS = 220;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;
    private static final int DEFAULT_FIRE_RESISTANCE_DURATION_TICKS = 220;
    private static final int DEFAULT_COOLDOWN_TICKS = 900;

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

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE)
        );

        final double addBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_ABSORPTION_ADD,
                DEFAULT_SELF_ABSORPTION_ADD
            )
        );
        final double addScaled = DaoHenEffectScalingHelper.scaleValue(addBase, selfMultiplier);
        final double add = ShazhaoMetadataHelper.clamp(addScaled, MIN_VALUE, MAX_SELF_ABSORPTION_ADD);

        final double maxBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_ABSORPTION_MAX,
                DEFAULT_SELF_ABSORPTION_MAX
            )
        );
        final double maxScaled = DaoHenEffectScalingHelper.scaleValue(maxBase, selfMultiplier);
        final double max = ShazhaoMetadataHelper.clamp(maxScaled, MIN_VALUE, MAX_SELF_ABSORPTION_MAX);

        if (add > MIN_VALUE && max > MIN_VALUE) {
            BingXueDaoShazhaoEffectHelper.addAbsorption(player, add, max);
        }

        refreshEffects(player, data, selfMultiplier);

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

    private static void refreshEffects(
        final ServerPlayer player,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int baseResTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESISTANCE_DURATION_TICKS,
                DEFAULT_RESISTANCE_DURATION_TICKS
            )
        );
        final int resTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseResTicks, multiplier);
        if (resTicks > 0) {
            final int amplifier = Math.max(
                0,
                ShazhaoMetadataHelper.getInt(
                    data,
                    META_RESISTANCE_AMPLIFIER,
                    DEFAULT_RESISTANCE_AMPLIFIER
                )
            );
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, resTicks, amplifier, true, true));
        }

        final int baseFireTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_FIRE_RESISTANCE_DURATION_TICKS,
                DEFAULT_FIRE_RESISTANCE_DURATION_TICKS
            )
        );
        final int fireTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseFireTicks, multiplier);
        if (fireTicks > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, fireTicks, 0, true, true));
        }
    }
}

