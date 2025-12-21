package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
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
 * 星道通用主动：换位/占据。
 * <p>
 * - 资源消耗：真元折算 +（念头/精力/魂魄任选其一即可）。
 * - 范围与增益持续时间：随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoActiveSwapEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";

    private static final int DEFAULT_COOLDOWN_TICKS = 360;
    private static final double DEFAULT_RANGE = 12.0;
    private static final double MAX_RANGE = 24.0;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> selfEffects;
    private final List<EffectSpec> targetEffects;

    public XingDaoActiveSwapEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> selfEffects,
        final List<EffectSpec> targetEffects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.selfEffects = selfEffects;
        this.targetEffects = targetEffects;
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

        final double baseRange = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.XING_DAO
        );
        final double scaledRange = DaoHenEffectScalingHelper.scaleValue(
            baseRange,
            selfMultiplier
        );
        final double range = UsageMetadataHelper.clamp(scaledRange, 1.0, MAX_RANGE);

        final LivingEntity target = LineOfSightTargetHelper.findTarget(
            player,
            range
        );
        if (target == null) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("未找到可换位目标。"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final Vec3 userPos = player.position();
        final Vec3 targetPos = target.position();

        final Vec3 safeUserDest = SafeTeleportHelper.findSafeTeleportPos(
            player,
            targetPos
        );
        final Vec3 safeTargetDest = SafeTeleportHelper.findSafeTeleportPos(
            target,
            userPos
        );

        player.teleportTo(safeUserDest.x, safeUserDest.y, safeUserDest.z);
        player.resetFallDistance();
        target.teleportTo(safeTargetDest.x, safeTargetDest.y, safeTargetDest.z);
        target.resetFallDistance();

        applyBuffs(player, usageInfo, selfEffects, selfMultiplier);
        final double targetMultiplier = DaoHenCalculator.calculateMultiplier(
            user,
            target,
            DaoHenHelper.DaoType.XING_DAO
        );
        applyBuffs(target, usageInfo, targetEffects, targetMultiplier);

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

    private static void applyBuffs(
        final LivingEntity entity,
        final NianTouData.Usage usageInfo,
        final List<EffectSpec> effects,
        final double multiplier
    ) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : effects) {
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
                multiplier
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
                entity.addEffect(
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
