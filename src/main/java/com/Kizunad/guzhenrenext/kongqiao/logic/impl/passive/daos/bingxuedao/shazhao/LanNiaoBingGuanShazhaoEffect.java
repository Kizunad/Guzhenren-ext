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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冰雪道主动杀招：蓝鸟冰棺。
 * <p>
 * 视线锁定单体：普通伤害 + 强迟滞/挖掘疲劳/虚弱 + 冻结（偏强控单体）。
 * </p>
 */
public class LanNiaoBingGuanShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bing_xue_dao_lan_niao_bing_guan";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_RANGE = "range";
    private static final String META_DAMAGE = "damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_FATIGUE_DURATION_TICKS = "fatigue_duration_ticks";
    private static final String META_FATIGUE_AMPLIFIER = "fatigue_amplifier";
    private static final String META_WEAKNESS_DURATION_TICKS = "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";
    private static final String META_FREEZE_TICKS = "freeze_ticks";
    private static final String META_MAX_FROZEN_TICKS = "max_frozen_ticks";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_RANGE = 18.0;
    private static final double MIN_RANGE = 1.0;
    private static final double DEFAULT_DAMAGE = 900.0;
    private static final double MAX_DAMAGE = 16000.0;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 220;
    private static final int DEFAULT_SLOW_AMPLIFIER = 1;
    private static final int DEFAULT_FATIGUE_DURATION_TICKS = 220;
    private static final int DEFAULT_FATIGUE_AMPLIFIER = 1;
    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 180;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;
    private static final int DEFAULT_FREEZE_TICKS = 260;
    private static final int DEFAULT_MAX_FROZEN_TICKS = 900;
    private static final int DEFAULT_COOLDOWN_TICKS = 1600;

    private static final EffectSpec SLOW_EFFECT = new EffectSpec(
        MobEffects.MOVEMENT_SLOWDOWN,
        META_SLOW_DURATION_TICKS,
        DEFAULT_SLOW_DURATION_TICKS,
        META_SLOW_AMPLIFIER,
        DEFAULT_SLOW_AMPLIFIER
    );
    private static final EffectSpec FATIGUE_EFFECT = new EffectSpec(
        MobEffects.DIG_SLOWDOWN,
        META_FATIGUE_DURATION_TICKS,
        DEFAULT_FATIGUE_DURATION_TICKS,
        META_FATIGUE_AMPLIFIER,
        DEFAULT_FATIGUE_AMPLIFIER
    );
    private static final EffectSpec WEAKNESS_EFFECT = new EffectSpec(
        MobEffects.WEAKNESS,
        META_WEAKNESS_DURATION_TICKS,
        DEFAULT_WEAKNESS_DURATION_TICKS,
        META_WEAKNESS_AMPLIFIER,
        DEFAULT_WEAKNESS_AMPLIFIER
    );

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
            target.hurt(PhysicalDamageSourceHelper.buildPhysicalDamageSource(player), (float) damage);
        }

        applyDebuffs(target, data, multiplier);

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

    private static void applyDebuffs(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        applyEffect(target, data, SLOW_EFFECT, multiplier);
        applyEffect(target, data, FATIGUE_EFFECT, multiplier);
        applyEffect(target, data, WEAKNESS_EFFECT, multiplier);

        final int baseFreezeTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_FREEZE_TICKS, DEFAULT_FREEZE_TICKS)
        );
        final int freezeTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseFreezeTicks, multiplier);
        if (freezeTicks > 0) {
            final int maxFrozenTicks = Math.max(
                1,
                ShazhaoMetadataHelper.getInt(data, META_MAX_FROZEN_TICKS, DEFAULT_MAX_FROZEN_TICKS)
            );
            BingXueDaoShazhaoEffectHelper.addFreezeTicks(target, freezeTicks, maxFrozenTicks);
        }
    }

    private static void applyEffect(
        final LivingEntity target,
        final ShazhaoData data,
        final EffectSpec spec,
        final double multiplier
    ) {
        if (target == null || data == null || spec == null || spec.effect() == null) {
            return;
        }
        final int baseDuration = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, spec.durationKey(), spec.defaultDurationTicks())
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(baseDuration, multiplier);
        if (duration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, spec.amplifierKey(), spec.defaultAmplifier())
        );
        target.addEffect(new MobEffectInstance(spec.effect(), duration, amplifier, true, true));
    }

    private record EffectSpec(
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}
}
