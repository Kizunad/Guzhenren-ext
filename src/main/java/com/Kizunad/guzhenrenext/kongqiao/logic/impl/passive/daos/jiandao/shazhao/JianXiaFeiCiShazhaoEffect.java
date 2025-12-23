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
 * 剑道主动杀招：剑匣飞刺。
 * <p>
 * 视线锁定单体，剑匣吐锋：造成少量普通伤害并以剑痕标记目标（发光）。
 * </p>
 */
public class JianXiaFeiCiShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jian_xia_fei_ci";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_RANGE = "range";
    private static final String META_DAMAGE = "damage";
    private static final String META_MARK_DURATION_TICKS = "mark_duration_ticks";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 12.0;
    private static final double MIN_RANGE = 1.0;

    private static final double DEFAULT_DAMAGE = 8.0;
    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE = 120.0;

    private static final int DEFAULT_MARK_DURATION_TICKS = 120;
    private static final int DEFAULT_COOLDOWN_TICKS = 80;

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

        final int baseMarkTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_MARK_DURATION_TICKS,
                DEFAULT_MARK_DURATION_TICKS
            )
        );
        final int markTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseMarkTicks,
            multiplier
        );
        if (markTicks > 0) {
            target.addEffect(
                new MobEffectInstance(MobEffects.GLOWING, markTicks, 0, true, true)
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

