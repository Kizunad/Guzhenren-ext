package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common;

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
 * 雷道主动：电眼一瞬 - 一次性脉冲显形（发光）并获得夜视。
 */
public class LeiDaoActiveRevealPulseEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_RADIUS = "radius";
    public static final String META_REVEAL_DURATION_TICKS = "reveal_duration_ticks";
    public static final String META_NIGHT_VISION_DURATION_TICKS = "night_vision_duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 300;
    private static final double DEFAULT_RADIUS = 16.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double MAX_RADIUS = 64.0;

    private static final int DEFAULT_REVEAL_DURATION_TICKS = 120;
    private static final int DEFAULT_NIGHT_VISION_DURATION_TICKS = 200;

    private final String usageId;
    private final String nbtCooldownKey;

    public LeiDaoActiveRevealPulseEffect(final String usageId, final String nbtCooldownKey) {
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LEI_DAO
        );

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            MIN_RADIUS,
            MAX_RADIUS
        );
        final AABB box = user.getBoundingBox().inflate(radius);

        final int revealDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_REVEAL_DURATION_TICKS,
                    DEFAULT_REVEAL_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        if (revealDuration > 0) {
            for (LivingEntity entity : user.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e != user && (e.isInvisible() || e.hasEffect(MobEffects.INVISIBILITY))
            )) {
                entity.addEffect(
                    new MobEffectInstance(MobEffects.GLOWING, revealDuration, 0, true, true)
                );
            }
        }

        final int nvDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_NIGHT_VISION_DURATION_TICKS,
                    DEFAULT_NIGHT_VISION_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        if (nvDuration > 0) {
            user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, nvDuration, 0, true, false));
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

