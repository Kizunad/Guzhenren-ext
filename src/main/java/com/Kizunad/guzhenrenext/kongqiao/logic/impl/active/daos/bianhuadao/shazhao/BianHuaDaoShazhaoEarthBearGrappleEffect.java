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
import net.minecraft.world.phys.Vec3;

/**
 * 变化道主动杀招【地熊擒杀】：锁定单体，以熊爪擒拽并重击其身。
 * <p>
 * 说明：擒拽与迟缓属于非伤害类效果，使用 {@link DaoHenEffectScalingHelper} 对倍率裁剪；
 * 伤害使用普通伤害源，避免法术穿甲过强。
 * </p>
 */
public class BianHuaDaoShazhaoEarthBearGrappleEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bian_hua_earth_bear_grapple";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_RANGE = "range";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_PULL_STRENGTH = "pull_strength";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final double DEFAULT_RANGE = 16.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 11;

    private static final double MIN_RANGE = 1.0;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double PULL_UPWARD_PUSH = 0.08;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_bian_hua_earth_bear_grapple_cd_until";

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

        final DamageSource source =
            PhysicalDamageSourceHelper.buildPhysicalDamageSource(player);

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
            target.hurt(source, (float) (baseDamage * multiplier));
        }

        applyPull(player, target, data, multiplier);
        applySlow(target, data, multiplier);
        applyCooldown(player, data);
        return true;
    }

    private static void applyPull(
        final LivingEntity user,
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double baseStrength = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PULL_STRENGTH, 0.0)
        );
        if (baseStrength <= 0.0) {
            return;
        }
        final double strength = DaoHenEffectScalingHelper.scaleValue(
            baseStrength,
            multiplier
        );
        if (strength <= 0.0) {
            return;
        }

        final Vec3 delta = user.position().subtract(target.position());
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, PULL_UPWARD_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }

    private static void applySlow(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int durationTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_DURATION_TICKS, 0)
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
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, 0)
        );
        target.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                scaledDuration,
                amplifier,
                true,
                true
            )
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

