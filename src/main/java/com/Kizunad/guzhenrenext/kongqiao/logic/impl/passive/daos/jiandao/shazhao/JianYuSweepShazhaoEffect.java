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
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 剑道主动杀招：剑域横斩。
 * <p>
 * 视线锁定单体，以其为中心横斩一域：范围普通伤害 + 击退 + 虚弱。
 * </p>
 */
public class JianYuSweepShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_jian_yu_sweep";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.JIAN_DAO;

    private static final String META_RANGE = "range";
    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_WEAKNESS_DURATION_TICKS =
        "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 18.0;
    private static final double MIN_RANGE = 1.0;
    private static final double DEFAULT_RADIUS = 4.2;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 2600.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.45;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 30000.0;
    private static final double MAX_KNOCKBACK_STRENGTH = 1.4;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.06;

    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 160;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 1600;

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
        final LivingEntity anchor = LineOfSightTargetHelper.findTarget(player, range);
        if (anchor == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);

        final AABB area = anchor.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = anchor.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
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

        final int baseWeakTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_DURATION_TICKS,
                DEFAULT_WEAKNESS_DURATION_TICKS
            )
        );
        final int weakAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_AMPLIFIER,
                DEFAULT_WEAKNESS_AMPLIFIER
            )
        );

        for (LivingEntity target : targets) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);

            final double damage = ShazhaoMetadataHelper.clamp(
                baseDamage * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (damage > MIN_VALUE) {
                target.hurt(
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                    (float) damage
                );
            }

            final int weakTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseWeakTicks, multiplier);
            if (weakTicks > 0) {
                target.addEffect(
                    new MobEffectInstance(MobEffects.WEAKNESS, weakTicks, weakAmplifier, true, true)
                );
            }

            final double scaledKnockback = baseKnockback * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
            applyKnockback(anchor.position(), target, scaledKnockback);
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

