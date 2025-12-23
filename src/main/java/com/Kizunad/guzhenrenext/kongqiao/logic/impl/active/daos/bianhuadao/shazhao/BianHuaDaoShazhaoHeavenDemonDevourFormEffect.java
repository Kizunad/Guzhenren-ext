package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 变化道主动杀招【天魔噬形】：锁定强敌，以天魔之形吞噬其势（高额普通伤害 + 凋零）。
 * <p>
 * 说明：伤害使用普通伤害源；凋零持续时间为控制类效果，按倍率裁剪，避免超长压制。
 * </p>
 */
public class BianHuaDaoShazhaoHeavenDemonDevourFormEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bian_hua_heaven_demon_devour_form";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_WITHER_DURATION_TICKS = "wither_duration_ticks";
    private static final String META_WITHER_AMPLIFIER = "wither_amplifier";

    private static final double DEFAULT_RANGE = 30.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 42;

    private static final double MIN_RANGE = 1.0;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_bian_hua_heaven_demon_devour_form_cd_until";

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
            MIN_RANGE,
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
            DAO_TYPE
        );
        final double baseDamage = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, 0.0)
        );
        if (baseDamage > 0.0) {
            final DamageSource source =
                PhysicalDamageSourceHelper.buildPhysicalDamageSource(player);
            target.hurt(source, (float) (baseDamage * multiplier));
        }

        applyWither(target, data, multiplier);
        applyCooldown(player, data);
        return true;
    }

    private static void applyWither(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int durationTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_WITHER_DURATION_TICKS, 0)
        );
        if (durationTicks <= 0) {
            return;
        }
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            durationTicks,
            multiplier
        );
        if (scaledDuration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_WITHER_AMPLIFIER, 0)
        );
        target.addEffect(
            new MobEffectInstance(MobEffects.WITHER, scaledDuration, amplifier, true, true)
        );
    }

    private static void applyCooldown(
        final ServerPlayer player,
        final ShazhaoData data
    ) {
        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks <= 0) {
            return;
        }
        GuEffectCooldownHelper.setCooldownUntilTick(
            player,
            NBT_COOLDOWN_KEY,
            player.tickCount + cooldownTicks
        );
    }
}

