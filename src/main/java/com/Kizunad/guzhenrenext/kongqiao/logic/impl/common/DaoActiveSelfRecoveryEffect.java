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
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 通用主动：自我恢复（回血/回真元/回念头/回精力/回魂魄）。
 */
public class DaoActiveSelfRecoveryEffect implements IGuEffect {

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;
    private static final double MAX_HEAL = 200.0;
    private static final double MAX_RESOURCE_GAIN = 2000.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;

    public DaoActiveSelfRecoveryEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
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
        if (user == null || usageInfo == null) {
            return false;
        }
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

        final double scale = DaoHenEffectScalingHelper.clampMultiplier(
            daoType == null ? 1.0 : DaoHenCalculator.calculateSelfMultiplier(user, daoType)
        );

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        applyHeal(user, DaoHenEffectScalingHelper.scaleValue(heal, scale));

        applyGain(
            user,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_GAIN, 0.0),
            scale,
            NianTouHelper::modify
        );
        applyGain(
            user,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_GAIN, 0.0),
            scale,
            JingLiHelper::modify
        );
        applyGain(
            user,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_GAIN, 0.0),
            scale,
            HunPoHelper::modify
        );
        applyGain(
            user,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_GAIN, 0.0),
            scale,
            ZhenYuanHelper::modify
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

    private static void applyHeal(final LivingEntity user, final double scaledHeal) {
        if (scaledHeal <= 0.0) {
            return;
        }
        final double clamped = UsageMetadataHelper.clamp(scaledHeal, 0.0, MAX_HEAL);
        if (clamped > 0.0) {
            user.heal((float) clamped);
        }
    }

    private static void applyGain(
        final LivingEntity user,
        final double base,
        final double scale,
        final ResourceModifier modifier
    ) {
        final double amount = Math.max(0.0, base);
        if (amount <= 0.0) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(amount, scale);
        final double clamped = UsageMetadataHelper.clamp(
            scaled,
            0.0,
            MAX_RESOURCE_GAIN
        );
        if (clamped > 0.0) {
            modifier.modify(user, clamped);
        }
    }

    @FunctionalInterface
    private interface ResourceModifier {
        void modify(LivingEntity user, double amount);
    }
}

