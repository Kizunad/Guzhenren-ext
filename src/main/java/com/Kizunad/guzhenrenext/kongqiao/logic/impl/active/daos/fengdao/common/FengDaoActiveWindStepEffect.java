package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.SafeTeleportHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 风道通用主动：风步（短距挪移）。
 * <p>
 * 以玩家视线方向短距闪步到目标点附近的安全位置，距离会受风道道痕自倍率影响。
 * </p>
 */
public class FengDaoActiveWindStepEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DISTANCE = "distance";
    private static final String META_MAX_DISTANCE = "max_distance";

    private static final int DEFAULT_COOLDOWN_TICKS = 200;
    private static final double DEFAULT_DISTANCE = 6.0;
    private static final double DEFAULT_MAX_DISTANCE = 16.0;
    private static final int MAX_EFFECT_DURATION_TICKS = 20 * 30;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> afterEffects;

    public FengDaoActiveWindStepEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> afterEffects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.afterEffects = afterEffects;
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

        final double baseDistance = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DISTANCE, DEFAULT_DISTANCE)
        );
        final double maxDistance = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_MAX_DISTANCE,
                DEFAULT_MAX_DISTANCE
            )
        );
        final double fengDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.FENG_DAO
        );
        final double distance = Math.min(maxDistance, baseDistance * fengDaoMultiplier);

        final Vec3 target = player.position().add(
            player.getViewVector(1.0F).scale(distance)
        );
        SafeTeleportHelper.teleportSafely(player, target);
        applyAfterEffects(player, usageInfo);

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

    private void applyAfterEffects(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo
    ) {
        if (afterEffects == null || afterEffects.isEmpty()) {
            return;
        }
        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.FENG_DAO
        );
        for (EffectSpec spec : afterEffects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            final int scaledDuration = (int) Math.min(
                MAX_EFFECT_DURATION_TICKS,
                Math.round(duration * multiplier)
            );
            if (scaledDuration > 0) {
                player.addEffect(
                    new MobEffectInstance(
                        spec.effect(),
                        scaledDuration,
                        amplifier,
                        true,
                        true
                    )
                );
            }
        }
    }
}
