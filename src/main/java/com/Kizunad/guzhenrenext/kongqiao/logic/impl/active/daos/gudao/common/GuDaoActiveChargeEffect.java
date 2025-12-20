package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 骨道通用主动：突进冲撞（位移 + 前方命中伤害/控场）。
 * <p>
 * 注意：位移与命中判定都在同一 tick 内完成，设计目标是简单可靠，不做复杂的“移动中持续碰撞”。
 * </p>
 */
public class GuDaoActiveChargeEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_DASH_STRENGTH = "dash_strength";
    private static final String META_HIT_RADIUS = "hit_radius";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_MAGIC_DAMAGE = "magic_damage";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 240;
    private static final double DEFAULT_RANGE = 8.0;
    private static final double DEFAULT_DASH_STRENGTH = 1.2;
    private static final double DEFAULT_HIT_RADIUS = 1.0;

    private static final double FORWARD_EPSILON_SQR = 1.0E-6;

    private final String usageId;
    private final String nbtCooldownKey;

    public GuDaoActiveChargeEffect(final String usageId, final String nbtCooldownKey) {
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final Vec3 start = player.getEyePosition();
        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final Vec3 end = start.add(player.getViewVector(1.0F).scale(range));

        final double dashStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DASH_STRENGTH,
                DEFAULT_DASH_STRENGTH
            )
        );
        applyDash(player, dashStrength);

        final double hitRadius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HIT_RADIUS,
                DEFAULT_HIT_RADIUS
            )
        );

        final double physicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_PHYSICAL_DAMAGE, 0.0)
        );
        final double magicDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAGIC_DAMAGE, 0.0)
        );
        final int slowDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_SLOW_DURATION_TICKS, 0)
        );
        final int slowAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_SLOW_AMPLIFIER, 0)
        );

        final AABB searchBox = new AABB(start, end).inflate(hitRadius);
        final List<LivingEntity> victims = player.level().getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );
        for (LivingEntity victim : victims) {
            final double score = distanceSquaredToSegment(
                victim.getEyePosition(),
                start,
                end
            );
            if (score > (hitRadius * hitRadius)) {
                continue;
            }

            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                victim,
                DaoHenHelper.DaoType.GU_DAO
            );

            if (physicalDamage > 0.0) {
                final DamageSource source = buildPhysicalDamageSource(user);
                victim.hurt(source, (float) (physicalDamage * multiplier));
            } else if (magicDamage > 0.0) {
                victim.hurt(
                    user.damageSources().magic(),
                    (float) (magicDamage * multiplier)
                );
            }

            final int scaledSlowDuration = DaoHenEffectScalingHelper
                .scaleDurationTicks(slowDuration, multiplier);
            if (scaledSlowDuration > 0) {
                victim.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        scaledSlowDuration,
                        slowAmplifier,
                        true,
                        true
                    )
                );
            }
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
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

    private static void applyDash(
        final ServerPlayer player,
        final double forwardSpeed
    ) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0, look.z);
        if (forward.lengthSqr() > FORWARD_EPSILON_SQR) {
            forward = forward.normalize().scale(forwardSpeed);
        } else {
            forward = Vec3.ZERO;
        }

        final Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(
            motion.x + forward.x,
            motion.y,
            motion.z + forward.z
        );
        player.fallDistance = 0.0F;
        player.hasImpulse = true;
    }

    private static double distanceSquaredToSegment(
        final Vec3 point,
        final Vec3 start,
        final Vec3 end
    ) {
        final Vec3 ab = end.subtract(start);
        final Vec3 ap = point.subtract(start);
        final double abLen2 = ab.lengthSqr();
        if (abLen2 <= 0.0) {
            return ap.lengthSqr();
        }
        double t = ap.dot(ab) / abLen2;
        t = UsageMetadataHelper.clamp(t, 0.0, 1.0);
        final Vec3 projection = start.add(ab.scale(t));
        return point.subtract(projection).lengthSqr();
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}
