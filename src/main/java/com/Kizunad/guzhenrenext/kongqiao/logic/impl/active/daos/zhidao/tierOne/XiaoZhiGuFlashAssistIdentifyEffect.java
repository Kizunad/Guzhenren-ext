package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 一转小智蛊：主动【灵光助鉴】。
 * <p>
 * 设计目标：提供一个“念头相关”的主动辅助能力，但不绕开鉴定系统的随机性。
 * 仅在玩家已有鉴定进程时，允许消耗剩余念头成本，瞬间完成当前鉴定。
 * </p>
 */
public class XiaoZhiGuFlashAssistIdentifyEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:xiaozhigu_active_flash_assist_identify";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "XiaoZhiGuFlashAssistIdentifyCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_FINISH_COST_MULTIPLIER = 1.10;
    private static final int DEFAULT_COOLDOWN_TICKS = 10 * TICKS_PER_SECOND;

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

        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null) {
            player.displayClientMessage(
                Component.literal("无法读取鉴定记录，灵光无从着力。"),
                true
            );
            return false;
        }

        final int currentTick = player.tickCount;
        final int cooldownUntil = player.getPersistentData()
            .getInt(NBT_COOLDOWN_UNTIL_TICK);
        if (cooldownUntil > currentTick) {
            final int remainTicks = cooldownUntil - currentTick;
            player.displayClientMessage(
                Component.literal(
                    "灵光助鉴冷却中，剩余 "
                        + (remainTicks / TICKS_PER_SECOND)
                        + " 秒"
                ),
                true
            );
            return false;
        }

        if (!unlocks.isProcessing()) {
            player.displayClientMessage(
                Component.literal("当前没有进行中的鉴定，灵光无处可用。"),
                true
            );
            return false;
        }

        final NianTouUnlocks.UnlockProcess process = unlocks.getCurrentProcess();
        if (process == null || process.itemId == null || process.usageId == null) {
            player.displayClientMessage(
                Component.literal("鉴定进程异常，无法使用灵光助鉴。"),
                true
            );
            return false;
        }
        if (process.remainingTicks <= 0) {
            player.displayClientMessage(
                Component.literal("鉴定已接近完成，无需灵光助鉴。"),
                true
            );
            return false;
        }

        final int effectiveTotalTicks = Math.max(1, process.totalTicks);
        final double costPerTick = (double) process.totalCost / effectiveTotalTicks;
        final double remainingBaseCost = Math.max(
            0.0,
            costPerTick * process.remainingTicks
        );

        final double multiplier = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "finish_cost_multiplier",
                DEFAULT_FINISH_COST_MULTIPLIER
            )
        );
        final double needCost = remainingBaseCost * multiplier;

        final double currentNianTou = NianTouHelper.getAmount(player);
        if (needCost > 0.0 && currentNianTou < needCost) {
            player.displayClientMessage(
                Component.literal(
                    "念头不足，无法灵光助鉴（需要 "
                        + trimDouble(needCost)
                        + "）"
                ),
                true
            );
            return false;
        }

        if (needCost > 0.0) {
            NianTouHelper.modify(player, -needCost);
        }

        unlocks.unlock(process.itemId, process.usageId);
        ShazhaoUnlockService.tryUnlockRandom(player.getRandom(), unlocks);
        unlocks.clearProcess();
        PacketDistributor.sendToPlayer(player, new PacketSyncNianTouUnlocks(unlocks));

        final int cooldownTicks = Math.max(
            0,
            getMetaInt(usageInfo, "cooldown_ticks", DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            player.getPersistentData()
                .putInt(NBT_COOLDOWN_UNTIL_TICK, currentTick + cooldownTicks);
        }
        return true;
    }

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
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

    private static String trimDouble(final double value) {
        final String text = String.format(java.util.Locale.ROOT, "%.2f", value);
        int end = text.length();
        while (end > 0 && text.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && text.charAt(end - 1) == '.') {
            end--;
        }
        return text.substring(0, end);
    }
}
