package com.Kizunad.guzhenrenext.kongqiao.logic.impl.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 通用主动：目标资源抽取。
 * <p>
 * 视线锁定目标，按配置抽取其资源（真元/念头/精力/魂魄），并将抽取量按比例转化给施术者。
 * </p>
 */
public class DaoActiveTargetResourceDrainEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_TRANSFER_RATIO = "transfer_ratio";

    private static final String META_NIANTOU_DRAIN = "niantou_drain";
    private static final String META_JINGLI_DRAIN = "jingli_drain";
    private static final String META_HUNPO_DRAIN = "hunpo_drain";
    private static final String META_ZHENYUAN_DRAIN = "zhenyuan_drain";

    private static final String META_DEBUFF_DURATION_TICKS = "debuff_duration_ticks";
    private static final String META_DEBUFF_AMPLIFIER = "debuff_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final double DEFAULT_RANGE = 14.0;
    private static final double DEFAULT_TRANSFER_RATIO = 1.0;
    private static final double MAX_DRAIN = 2000.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public DaoActiveTargetResourceDrainEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
        this.nbtCooldownKey = nbtCooldownKey;
        this.debuff = debuff;
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
        if (user == null || usageInfo == null) {
            return false;
        }
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, nbtCooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        final double selfMultiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);
        final double baseRange = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final double range = baseRange * Math.max(0.0, selfMultiplier);

        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("未找到可抽取目标。"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            daoType == null ? 1.0 : DaoHenCalculator.calculateMultiplier(user, target, daoType)
        );
        final double transferRatio = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_TRANSFER_RATIO, DEFAULT_TRANSFER_RATIO),
            0.0,
            1.0
        );

        final double drainedNianTou = drain(
            target,
            user,
            usageInfo,
            multiplier,
            transferRatio,
            new DrainSpec(
                META_NIANTOU_DRAIN,
                NianTouHelper::getAmount,
                NianTouHelper::modify
            )
        );
        final double drainedJingLi = drain(
            target,
            user,
            usageInfo,
            multiplier,
            transferRatio,
            new DrainSpec(
                META_JINGLI_DRAIN,
                JingLiHelper::getAmount,
                JingLiHelper::modify
            )
        );
        final double drainedHunPo = drain(
            target,
            user,
            usageInfo,
            multiplier,
            transferRatio,
            new DrainSpec(
                META_HUNPO_DRAIN,
                HunPoHelper::getAmount,
                HunPoHelper::modify
            )
        );
        final double drainedZhenYuan = drain(
            target,
            user,
            usageInfo,
            multiplier,
            transferRatio,
            new DrainSpec(
                META_ZHENYUAN_DRAIN,
                ZhenYuanHelper::getAmount,
                ZhenYuanHelper::modify
            )
        );

        applyDebuff(
            target,
            usageInfo,
            multiplier,
            drainedNianTou,
            drainedJingLi,
            drainedHunPo,
            drainedZhenYuan
        );

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

    private static double drain(
        final LivingEntity target,
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier,
        final double transferRatio,
        final DrainSpec spec
    ) {
        if (spec == null) {
            return 0.0;
        }
        final double base = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, spec.metaKey, 0.0)
        );
        if (base <= 0.0) {
            return 0.0;
        }

        final double scaledRequested = UsageMetadataHelper.clamp(
            base * Math.max(0.0, multiplier),
            0.0,
            MAX_DRAIN
        );
        if (scaledRequested <= 0.0) {
            return 0.0;
        }

        final double current = Math.max(0.0, spec.getter.get(target));
        final double actual = Math.min(current, scaledRequested);
        if (actual <= 0.0) {
            return 0.0;
        }

        spec.modifier.modify(target, -actual);
        final double transfer = actual * transferRatio;
        if (transfer > 0.0) {
            spec.modifier.modify(user, transfer);
        }

        return actual;
    }

    private record DrainSpec(
        String metaKey,
        ResourceGetter getter,
        ResourceModifier modifier
    ) {}

    private void applyDebuff(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double multiplier,
        final double drainedNianTou,
        final double drainedJingLi,
        final double drainedHunPo,
        final double drainedZhenYuan
    ) {
        if (debuff == null) {
            return;
        }
        if (
            drainedNianTou <= 0.0
                && drainedJingLi <= 0.0
                && drainedHunPo <= 0.0
                && drainedZhenYuan <= 0.0
        ) {
            return;
        }
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_DURATION_TICKS, 0)
        );
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(duration, multiplier);
        if (scaledDuration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_DEBUFF_AMPLIFIER, 0)
        );
        target.addEffect(
            new MobEffectInstance(debuff, scaledDuration, amplifier, true, true)
        );
    }

    @FunctionalInterface
    private interface ResourceGetter {
        double get(LivingEntity entity);
    }

    @FunctionalInterface
    private interface ResourceModifier {
        void modify(LivingEntity entity, double amount);
    }
}
