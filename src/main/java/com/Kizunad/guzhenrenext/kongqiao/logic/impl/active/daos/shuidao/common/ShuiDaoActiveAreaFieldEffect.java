package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 水道主动：水域/泥沼领域（覆盖地面）- 在目标位置生成短暂领域，触碰者被减速与挖掘疲劳。
 * <p>
 * 该技能主要提供控场与逼位，不输出高额伤害，符合“水道偏卸力与牵制”的定位。
 * </p>
 */
public class ShuiDaoActiveAreaFieldEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_RANGE = "range";
    public static final String META_CLOUD_RADIUS = "cloud_radius";
    public static final String META_CLOUD_DURATION_TICKS = "cloud_duration_ticks";
    public static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    public static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 260;
    private static final double DEFAULT_RANGE = 14.0;
    private static final double MIN_RANGE = 1.0;
    private static final double MAX_RANGE = 64.0;

    private static final float DEFAULT_CLOUD_RADIUS = 3.0F;
    private static final float MIN_CLOUD_RADIUS = 0.5F;
    private static final float MAX_CLOUD_RADIUS = 12.0F;

    private static final int DEFAULT_CLOUD_DURATION_TICKS = 140;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 80;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;

    public ShuiDaoActiveAreaFieldEffect(
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

        final double range = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE),
            MIN_RANGE,
            MAX_RANGE
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("未找到可锁定目标。"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final float radius = (float) UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_CLOUD_RADIUS, DEFAULT_CLOUD_RADIUS),
            MIN_CLOUD_RADIUS,
            MAX_CLOUD_RADIUS
        );

        final int cloudDuration = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_CLOUD_DURATION_TICKS,
                DEFAULT_CLOUD_DURATION_TICKS
            )
        );
        final int effectDuration = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );

        final AreaEffectCloud cloud = new AreaEffectCloud(
            user.level(),
            target.getX(),
            target.getY(),
            target.getZ()
        );
        cloud.setOwner(user);
        cloud.setRadius(radius);
        cloud.setDuration(cloudDuration);
        cloud.setParticle(ParticleTypes.SPLASH);
        cloud.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                effectDuration,
                amplifier,
                true,
                true
            )
        );
        cloud.addEffect(
            new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                effectDuration,
                amplifier,
                true,
                true
            )
        );
        user.level().addFreshEntity(cloud);

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

