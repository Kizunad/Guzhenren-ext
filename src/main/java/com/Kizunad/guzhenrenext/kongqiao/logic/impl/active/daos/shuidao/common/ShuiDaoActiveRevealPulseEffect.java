package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 水道主动：水光照映 - 显形/标记周围敌对目标（发光），用于追踪与侦察。
 */
public class ShuiDaoActiveRevealPulseEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_RADIUS = "radius";
    public static final String META_GLOW_DURATION_TICKS = "glow_duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 200;
    private static final double DEFAULT_RADIUS = 12.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double MAX_RADIUS = 48.0;

    private static final int DEFAULT_GLOW_DURATION_TICKS = 100;

    private final String usageId;
    private final String nbtCooldownKey;

    public ShuiDaoActiveRevealPulseEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, nbtCooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            MIN_RADIUS,
            MAX_RADIUS
        );

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHUI_DAO
        );

        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_GLOW_DURATION_TICKS,
                DEFAULT_GLOW_DURATION_TICKS
            )
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            selfMultiplier
        );
        if (duration > 0) {
            final AABB box = user.getBoundingBox().inflate(radius);
            for (LivingEntity entity : user.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e != user && !e.isAlliedTo(user)
            )) {
                entity.addEffect(
                    new MobEffectInstance(MobEffects.GLOWING, duration, 0, true, true)
                );
            }
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
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
}

