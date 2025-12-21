package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.SafeTeleportHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 木道主动：短距位移（随木道道痕缩放距离）并施加后效增益。
 */
public class MuDaoActiveBlinkEffect implements IGuEffect {
    public static class EffectSpec {
        private final Holder<MobEffect> effect;
        private final String durationKey;
        private final int defaultDurationTicks;
        private final String amplifierKey;
        private final int defaultAmplifier;

        public EffectSpec(
            final Holder<MobEffect> effect,
            final String durationKey,
            final int defaultDurationTicks,
            final String amplifierKey,
            final int defaultAmplifier
        ) {
            this.effect = effect;
            this.durationKey = durationKey;
            this.defaultDurationTicks = defaultDurationTicks;
            this.amplifierKey = amplifierKey;
            this.defaultAmplifier = defaultAmplifier;
        }
    }

    private static final String META_DISTANCE = "distance";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_DISTANCE = 8.0;
    private static final double MAX_DISTANCE = 24.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 8;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> afterEffects;

    public MuDaoActiveBlinkEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> afterEffects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.afterEffects = afterEffects == null ? List.of() : List.copyOf(afterEffects);
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
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double baseDistance = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DISTANCE,
                DEFAULT_DISTANCE
            )
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.MU_DAO
        );
        final double scaledDistance = DaoHenEffectScalingHelper.scaleValue(
            baseDistance,
            selfMultiplier
        );
        final double distance = UsageMetadataHelper.clamp(
            scaledDistance,
            1.0,
            MAX_DISTANCE
        );

        final Vec3 target = player.position().add(
            player.getViewVector(1.0F).scale(distance)
        );
        SafeTeleportHelper.teleportSafely(player, target);
        applyAfterEffects(player, usageInfo, selfMultiplier);

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
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (afterEffects == null || afterEffects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : afterEffects) {
            if (spec == null || spec.effect == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey,
                    spec.defaultDurationTicks
                )
            );
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                selfMultiplier
            );
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey,
                    spec.defaultAmplifier
                )
            );
            if (scaledDuration > 0) {
                player.addEffect(
                    new MobEffectInstance(
                        spec.effect,
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
