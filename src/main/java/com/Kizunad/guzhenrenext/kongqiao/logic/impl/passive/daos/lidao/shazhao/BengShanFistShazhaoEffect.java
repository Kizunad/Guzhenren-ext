package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao;

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
import net.minecraft.world.phys.Vec3;

/**
 * 力道主动杀招：崩山拳。
 * <p>
 * 视线锁定单体：造成普通伤害并击退，同时施加短暂迟滞与虚弱（偏控制/破势）。
 * </p>
 */
public class BengShanFistShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_li_dao_beng_shan_fist";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LI_DAO;

    private static final String META_RANGE = "range";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_WEAKNESS_DURATION_TICKS =
        "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 14.0;
    private static final double MIN_RANGE = 1.0;
    private static final double DEFAULT_DAMAGE = 260.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.55;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE = 8000.0;
    private static final double MAX_KNOCKBACK_STRENGTH = 1.6;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 80;
    private static final int DEFAULT_SLOW_AMPLIFIER = 0;
    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 80;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 420;

    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.08;

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

        final double baseKnockback = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_KNOCKBACK_STRENGTH,
                    DEFAULT_KNOCKBACK_STRENGTH
                )
            ),
            MIN_VALUE,
            MAX_KNOCKBACK_STRENGTH
        );
        final double knockback = baseKnockback * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
        applyKnockback(player.position(), target, knockback);

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
        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_DURATION_TICKS,
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseSlowTicks,
            multiplier
        );
        if (slowTicks > 0) {
            final int slowAmplifier = Math.max(
                0,
                ShazhaoMetadataHelper.getInt(
                    data,
                    META_SLOW_AMPLIFIER,
                    DEFAULT_SLOW_AMPLIFIER
                )
            );
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

        final int baseWeakTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_DURATION_TICKS,
                DEFAULT_WEAKNESS_DURATION_TICKS
            )
        );
        final int weakTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseWeakTicks,
            multiplier
        );
        if (weakTicks > 0) {
            final int weakAmplifier = Math.max(
                0,
                ShazhaoMetadataHelper.getInt(
                    data,
                    META_WEAKNESS_AMPLIFIER,
                    DEFAULT_WEAKNESS_AMPLIFIER
                )
            );
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    weakTicks,
                    weakAmplifier,
                    true,
                    true
                )
            );
        }
    }

    private static void applyKnockback(
        final Vec3 center,
        final LivingEntity target,
        final double strength
    ) {
        if (center == null || target == null || strength <= MIN_VALUE) {
            return;
        }
        final Vec3 delta = target.position().subtract(center);
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, VERTICAL_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }
}

