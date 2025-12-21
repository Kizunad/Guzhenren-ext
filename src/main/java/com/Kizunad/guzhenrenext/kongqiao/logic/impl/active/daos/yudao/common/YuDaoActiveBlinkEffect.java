package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 宇道通用主动：挪移。
 * <p>
 * 以玩家视线方向短距闪烁到目标点附近的安全位置，可附带自增益。
 * </p>
 */
public class YuDaoActiveBlinkEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DISTANCE = "distance";

    private static final int DEFAULT_COOLDOWN_TICKS = 220;
    private static final double DEFAULT_DISTANCE = 6.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final List<EffectSpec> afterEffects;

    public YuDaoActiveBlinkEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey,
        final List<EffectSpec> afterEffects
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
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
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        final double distance = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DISTANCE, DEFAULT_DISTANCE)
        );
        final double multiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);
        final double scaledDistance = distance * Math.max(0.0, multiplier);

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final Vec3 target = player.position().add(
            player.getViewVector(1.0F).scale(scaledDistance)
        );
        SafeTeleportHelper.teleportSafely(player, target);
        applyAfterEffects(player, usageInfo, multiplier);

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
        final double multiplier
    ) {
        if (afterEffects == null || afterEffects.isEmpty()) {
            return;
        }
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
            final int scaledDuration = scaleDuration(duration, multiplier);
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
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

    private static int scaleDuration(final int baseDuration, final double multiplier) {
        if (baseDuration <= 0) {
            return 0;
        }
        final double scaled = baseDuration * Math.max(0.0, multiplier);
        if (scaled <= 0.0) {
            return 0;
        }
        return Math.min(Math.max(1, (int) Math.round(scaled)), MAX_DURATION_TICKS);
    }
}
