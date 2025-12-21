package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 毒道主动：毒雾/腐蚀领域（覆盖地面）- 在目标位置生成短暂领域，触碰者被削弱。
 * <p>
 * 该技能以控场与削弱为主，不输出高频法术伤害；领域强度（持续）随毒道道痕变化。
 * </p>
 */
public class DuDaoActiveAreaFieldEffect implements IGuEffect {

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

    private static final int POISON_CLOUD_COLOR = 0x00AA00;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<Holder<MobEffect>> effects;

    public DuDaoActiveAreaFieldEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<Holder<MobEffect>> effects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.effects = effects == null ? List.of() : List.copyOf(effects);
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

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.DU_DAO)
        );

        final float radius = (float) UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_CLOUD_RADIUS, DEFAULT_CLOUD_RADIUS)
                * selfMultiplier,
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
        final int baseEffectDuration = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int effectDuration = Math.max(
            1,
            DaoHenEffectScalingHelper.scaleDurationTicks(baseEffectDuration, selfMultiplier)
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
        cloud.setParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, POISON_CLOUD_COLOR));
        for (Holder<MobEffect> effect : effects) {
            if (effect == null) {
                continue;
            }
            cloud.addEffect(
                new MobEffectInstance(effect, effectDuration, amplifier, true, true)
            );
        }
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
