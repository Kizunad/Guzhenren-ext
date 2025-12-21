package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoTrapArmedOnHurtEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 隐血盅主动：布置陷阱（在物品 NBT 写入“下次受击触发”窗口）。
 */
public class XueDaoActiveArmTrapEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_ARMED_DURATION_TICKS = "armed_duration_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final int DEFAULT_ARMED_DURATION_TICKS = 20 * 45;

    private final String usageId;
    private final String nbtCooldownKey;

    public XueDaoActiveArmTrapEffect(
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
        if (user == null || stack == null || usageInfo == null) {
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
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final int armedDuration = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_ARMED_DURATION_TICKS,
                DEFAULT_ARMED_DURATION_TICKS
            )
        );
        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        tag.putInt(
            XueDaoTrapArmedOnHurtEffect.KEY_TRAP_ARMED_UNTIL_TICK,
            user.tickCount + armedDuration
        );
        ItemStackCustomDataHelper.setCustomDataTag(stack, tag);
        player.displayClientMessage(Component.literal("已布置隐血陷阱。"), true);

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
