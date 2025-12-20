package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 三转七香酒虫：主动【七香醉风】。
 * <p>
 * 设计目标：机动 + 小范围控场，偏跑图与追击；以真元消耗与冷却限制滥用。</p>
 */
public class QiXiangJiuChongFragrantDashEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:qi_xiang_jiu_chong_active_fragrant_dash";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "QiXiangJiuChongFragrantDashCooldownUntilTick";

    private static final String META_DASH_STRENGTH = "dash_strength";
    private static final String META_RADIUS = "radius";
    private static final String META_EFFECT_SECONDS = "effect_seconds";
    private static final String META_ACTIVATE_ZHENYUAN_BASE_COST =
        "activate_zhenyuan_base_cost";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_DASH_STRENGTH = 1.2;
    private static final double DEFAULT_RADIUS = 5.0;
    private static final int DEFAULT_EFFECT_SECONDS = 6;
    private static final double DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST = 360.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 360;

    private static final double FORWARD_EPSILON_SQR = 1.0E-6;

    @Override
    public String getUsageId() {
        return USAGE_ID;
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
            NBT_COOLDOWN_UNTIL_TICK
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("七香醉风冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double baseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ACTIVATE_ZHENYUAN_BASE_COST,
                DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(user, cost)) {
            player.displayClientMessage(Component.literal("真元不足。"), true);
            return false;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
        }

        final double dashStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DASH_STRENGTH,
                DEFAULT_DASH_STRENGTH
            )
        );
        applyDash(player, dashStrength);

        final double radius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final int effectSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_SECONDS,
                DEFAULT_EFFECT_SECONDS
            )
        );
        if (radius > 0.0 && effectSeconds > 0) {
            final int duration = effectSeconds * 20;
            final List<LivingEntity> targets = findTargets(user, radius);
            for (LivingEntity target : targets) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        duration,
                        0,
                        true,
                        true
                    )
                );
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        duration,
                        0,
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
                NBT_COOLDOWN_UNTIL_TICK,
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

    private static List<LivingEntity> findTargets(
        final LivingEntity user,
        final double radius
    ) {
        final AABB area = user.getBoundingBox().inflate(radius);
        return user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && e instanceof Monster
        );
    }
}

