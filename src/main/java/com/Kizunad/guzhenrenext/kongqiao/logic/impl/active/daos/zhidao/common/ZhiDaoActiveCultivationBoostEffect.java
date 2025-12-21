package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.CultivationHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 智道通用主动：消耗资源换取修为进度（推演/悟道向）。
 */
public class ZhiDaoActiveCultivationBoostEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PROGRESS_GAIN = "cultivation_progress_gain";

    private static final int DEFAULT_COOLDOWN_TICKS = 400;
    private static final double DEFAULT_PROGRESS_GAIN = 10.0;

    private final String usageId;
    private final String nbtCooldownKey;

    public ZhiDaoActiveCultivationBoostEffect(
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

        final double gain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_PROGRESS_GAIN,
                DEFAULT_PROGRESS_GAIN
            )
        );
        if (gain > 0.0) {
            final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
                user,
                DaoHenHelper.DaoType.ZHI_DAO
            );
            CultivationHelper.modifyProgress(user, gain * multiplier);
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
}
