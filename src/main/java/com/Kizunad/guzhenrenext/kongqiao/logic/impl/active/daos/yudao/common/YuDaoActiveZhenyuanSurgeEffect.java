package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 宇道通用主动：元气涌动。
 * <p>
 * 消耗念头，将“悟性/思绪”快速转化为真元，提供修为续航型主动按钮。
 * </p>
 */
public class YuDaoActiveZhenyuanSurgeEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final double DEFAULT_ZHENYUAN_GAIN = 200.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;

    public YuDaoActiveZhenyuanSurgeEffect(
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

        final double gain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_GAIN,
                DEFAULT_ZHENYUAN_GAIN
            )
        );
        if (gain <= 0.0) {
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);
        final double scaledGain = gain * Math.max(0.0, multiplier);
        if (scaledGain > 0.0) {
            ZhenYuanHelper.modify(user, scaledGain);
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
