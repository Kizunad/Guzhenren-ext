package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 奴道主动：冷却回响（刷新/缩短若干主动用途冷却）。
 * <p>
 * 说明：底层冷却统一存储在 persistentData 中，以 “冷却截止 tick” 的形式存在；
 * 此效果会遍历并缩短符合条件的冷却键，避免“强制全清”导致玩法崩坏。
 * </p>
 */
public class NuDaoActiveCooldownRefreshEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_REDUCE_TICKS = "cooldown_reduce_ticks";
    private static final String META_ONLY_ACTIVE = "only_active";
    private static final String META_MAX_AFFECT = "max_affect";

    private static final int DEFAULT_COOLDOWN_TICKS = 1200;
    private static final int DEFAULT_REDUCE_TICKS = 200;
    private static final int MAX_REDUCE_TICKS = 800;
    private static final int DEFAULT_MAX_AFFECT = 24;

    private static final String COOLDOWN_PREFIX = "GuzhenrenExtCooldown_";

    private final String usageId;
    private final String nbtCooldownKey;

    public NuDaoActiveCooldownRefreshEffect(
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, nbtCooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.NU_DAO
        );

        final int baseReduce = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_REDUCE_TICKS,
                DEFAULT_REDUCE_TICKS
            )
        );
        final int reduceTicks = (int) Math.min(
            (long) MAX_REDUCE_TICKS,
            Math.round(
                DaoHenEffectScalingHelper.scaleValue(baseReduce, selfMultiplier)
            )
        );
        if (reduceTicks > 0) {
            final boolean onlyActive = UsageMetadataHelper.getBoolean(
                usageInfo,
                META_ONLY_ACTIVE,
                true
            );
            final int maxAffect = Math.max(
                1,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_MAX_AFFECT,
                    DEFAULT_MAX_AFFECT
                )
            );
            reduceCooldowns(user, reduceTicks, onlyActive, maxAffect);
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

    private void reduceCooldowns(
        final LivingEntity user,
        final int reduceTicks,
        final boolean onlyActive,
        final int maxAffect
    ) {
        final CompoundTag tag = user.getPersistentData();
        final Set<String> keys = tag.getAllKeys();
        int affected = 0;

        for (String key : keys) {
            if (key == null || !key.startsWith(COOLDOWN_PREFIX)) {
                continue;
            }
            if (key.equals(nbtCooldownKey)) {
                continue;
            }

            final String usageIdFromKey = key.substring(COOLDOWN_PREFIX.length());
            if (onlyActive && !NianTouUsageId.isActive(usageIdFromKey)) {
                continue;
            }

            final int untilTick = tag.getInt(key);
            final int remain = Math.max(0, untilTick - user.tickCount);
            if (remain <= 0) {
                continue;
            }
            final int nextRemain = Math.max(0, remain - reduceTicks);
            tag.putInt(key, user.tickCount + nextRemain);

            affected++;
            if (affected >= maxAffect) {
                return;
            }
        }
    }
}
