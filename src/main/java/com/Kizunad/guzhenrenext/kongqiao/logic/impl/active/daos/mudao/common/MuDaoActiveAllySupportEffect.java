package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 木道主动：群体扶持（治疗/增益），强度随木道道痕变化。
 */
public class MuDaoActiveAllySupportEffect implements IGuEffect {
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

    private static final String META_RADIUS = "radius";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_HEAL_AMOUNT = "heal_amount";

    private static final double DEFAULT_RADIUS = 6.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 12;
    private static final double MAX_HEAL = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public MuDaoActiveAllySupportEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
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

        final double baseRadius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.MU_DAO
        );
        final double radius = DaoHenEffectScalingHelper.scaleValue(
            baseRadius,
            selfMultiplier
        );

        final double healBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        final double heal = Math.min(
            MAX_HEAL,
            DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier)
        );

        final AABB box = user.getBoundingBox().inflate(radius);
        for (LivingEntity ally : user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && isAlly(user, e)
        )) {
            if (heal > 0.0) {
                ally.heal((float) heal);
            }
            for (EffectSpec spec : effects) {
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
                if (duration <= 0) {
                    continue;
                }
                final int scaledDuration =
                    DaoHenEffectScalingHelper.scaleDurationTicks(
                        duration,
                        selfMultiplier
                    );
                if (scaledDuration <= 0) {
                    continue;
                }
                final int amplifier = Math.max(
                    0,
                    UsageMetadataHelper.getInt(
                        usageInfo,
                        spec.amplifierKey,
                        spec.defaultAmplifier
                    )
                );
                ally.addEffect(
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

    private static boolean isAlly(final LivingEntity user, final LivingEntity other) {
        if (other == user) {
            return true;
        }
        if (user instanceof Player a && other instanceof Player b) {
            return a.isAlliedTo(b);
        }
        return user.isAlliedTo(other);
    }
}
