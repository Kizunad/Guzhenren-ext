package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoBoostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class JianDaoActiveSuiRenAmplifyEffect implements IGuEffect {

    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_AMPLIFY_MULTIPLIER = "amplify_multiplier";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_DURATION_TICKS = 20 * 8;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 0;

    private final String usageId;
    private final String nbtCooldownKey;

    public JianDaoActiveSuiRenAmplifyEffect(
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
        if (user == null) {
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

        final int durationTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        if (durationTicks <= 0) {
            return false;
        }
        final double multiplier = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_AMPLIFY_MULTIPLIER,
                DEFAULT_MULTIPLIER
            )
        );
        JianDaoBoostHelper.activateSuiRen(user, durationTicks, multiplier);

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
