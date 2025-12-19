package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 一转小慧蛊：主动【引导鉴定】。
 * <p>
 * 设计目标：不改变“鉴定随机解锁”的基调，但允许玩家从轮盘对“手持物品”发起鉴定进程，
 * 并优先选择“念头消耗最低”的未解锁用途，减少盲抽的挫败感。
 * </p>
 */
public class XiaoHuiGuGuidedIdentifyEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:xiaohuigu_active_guided_identify";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "XiaoHuiGuGuidedIdentifyCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;

    @Override
    public String getUsageId() {
        return USAGE_ID;
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

        final int currentTick = player.tickCount;
        final int cooldownUntil = player.getPersistentData()
            .getInt(NBT_COOLDOWN_UNTIL_TICK);
        if (cooldownUntil > currentTick) {
            final int remain = cooldownUntil - currentTick;
            player.displayClientMessage(
                Component.literal(
                    "引导鉴定冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null) {
            player.displayClientMessage(
                Component.literal("无法读取鉴定记录，引导失败。"),
                true
            );
            return false;
        }
        if (unlocks.isProcessing()) {
            player.displayClientMessage(
                Component.literal("已有鉴定正在进行"),
                true
            );
            return false;
        }

        final ItemStack targetStack = resolveTargetStack(player);
        if (targetStack.isEmpty()) {
            player.displayClientMessage(
                Component.literal("请手持需要鉴定的物品"),
                true
            );
            return false;
        }

        final NianTouData data = NianTouDataManager.getData(targetStack);
        if (data == null || data.usages() == null || data.usages().isEmpty()) {
            player.displayClientMessage(
                Component.literal("该物品没有可鉴定的用途"),
                true
            );
            return false;
        }

        final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(
            targetStack.getItem()
        );
        final List<NianTouData.Usage> locked = new ArrayList<>();
        for (NianTouData.Usage usage : data.usages()) {
            if (usage == null || usage.usageID() == null) {
                continue;
            }
            if (!unlocks.isUsageUnlocked(itemId, usage.usageID())) {
                locked.add(usage);
            }
        }
        if (locked.isEmpty()) {
            player.displayClientMessage(
                Component.literal("该物品所有用途均已鉴定"),
                true
            );
            return false;
        }

        final NianTouData.Usage selected = selectCheapestUsage(player, locked);
        int duration = selected.costDuration();
        int cost = selected.costTotalNiantou();
        if (duration <= 0) {
            duration = 1;
        }
        if (cost < 0) {
            cost = 0;
        }

        unlocks.startProcess(itemId, selected.usageID(), duration, cost);
        PacketDistributor.sendToPlayer(player, new PacketSyncNianTouUnlocks(unlocks));
        player.displayClientMessage(
            Component.literal("开始引导鉴定：" + selected.usageTitle()),
            true
        );

        applyCooldown(player, usageInfo, currentTick);
        return true;
    }

    private static ItemStack resolveTargetStack(final ServerPlayer player) {
        if (player.getMainHandItem() != null && !player.getMainHandItem().isEmpty()) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem() != null && !player.getOffhandItem().isEmpty()) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static NianTouData.Usage selectCheapestUsage(
        final ServerPlayer player,
        final List<NianTouData.Usage> locked
    ) {
        NianTouData.Usage best = locked.get(0);
        for (int i = 1; i < locked.size(); i++) {
            final NianTouData.Usage current = locked.get(i);
            if (current == null) {
                continue;
            }
            if (current.costTotalNiantou() < best.costTotalNiantou()) {
                best = current;
                continue;
            }
            if (current.costTotalNiantou() == best.costTotalNiantou()
                && current.costDuration() < best.costDuration()) {
                best = current;
                continue;
            }
            if (current.costTotalNiantou() == best.costTotalNiantou()
                && current.costDuration() == best.costDuration()
                && player.getRandom().nextBoolean()) {
                best = current;
            }
        }
        return best;
    }

    private static void applyCooldown(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo,
        final int currentTick
    ) {
        final int cooldownTicks = Math.max(
            0,
            getMetaInt(usageInfo, "cooldown_ticks", DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks <= 0) {
            return;
        }
        player.getPersistentData()
            .putInt(NBT_COOLDOWN_UNTIL_TICK, currentTick + cooldownTicks);
    }

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}

