package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 火/炎道主动杀招【炽火突刺】：锁定单体的普通伤害打击（少量点燃）。
 * <p>
 * 目标选择：准星视线锁定生物；伤害使用普通伤害源，受护甲影响，避免法术伤害穿甲失控。
 * </p>
 */
public class HuoDaoShazhaoSparkThrustEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_huo_dao_spark_thrust";

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_BURN_SECONDS = "burn_seconds";

    private static final double DEFAULT_RANGE = 12.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 5;

    private static final int MAX_BURN_SECONDS = 10;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_huo_dao_spark_thrust_cd_until";

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null || player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            NBT_COOLDOWN_KEY
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double range = Math.max(
            1.0,
            ShazhaoMetadataHelper.getDouble(data, META_RANGE, DEFAULT_RANGE)
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            player,
            target,
            DaoHenHelper.DaoType.HUO_DAO
        );

        final double baseDamage = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, 0.0)
        );
        if (baseDamage > 0.0) {
            final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(
                player
            );
            target.hurt(source, (float) (baseDamage * multiplier));
        }

        final double burnBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_BURN_SECONDS, 0.0)
        );
        if (burnBase > 0.0) {
            final double burnSeconds = DaoHenEffectScalingHelper.scaleValue(
                burnBase,
                multiplier
            );
            final int seconds = (int) Math.min(
                MAX_BURN_SECONDS,
                Math.round(Math.max(0.0, burnSeconds))
            );
            if (seconds > 0) {
                target.igniteForSeconds(seconds);
            }
        }

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                NBT_COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return true;
    }
}

