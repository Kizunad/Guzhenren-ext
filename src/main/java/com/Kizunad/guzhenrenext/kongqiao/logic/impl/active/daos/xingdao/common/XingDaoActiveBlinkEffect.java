package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 星道通用主动：挪移。
 * <p>
 * - 资源消耗：真元折算 +（念头/精力/魂魄任选其一即可）。
 * - 位移距离与后续增益持续时间：随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoActiveBlinkEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_DISTANCE = "distance";

    private static final int DEFAULT_COOLDOWN_TICKS = 220;
    private static final double DEFAULT_DISTANCE = 6.0;
    private static final double MAX_DISTANCE = 32.0;

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

    public XingDaoActiveBlinkEffect(
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
            UsageMetadataHelper.getDouble(usageInfo, META_DISTANCE, DEFAULT_DISTANCE)
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.XING_DAO
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
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                selfMultiplier
            );
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
}
