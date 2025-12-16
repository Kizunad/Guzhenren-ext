package com.Kizunad.guzhenrenext.kongqiao.niantou;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 念头用途解锁判定工具。
 * <p>
 * 用于在运行时（Tick/战斗/主动触发）过滤掉尚未鉴定解锁的用途，确保“鉴定”对玩法生效。
 * </p>
 */
public final class NianTouUnlockChecker {

    private NianTouUnlockChecker() {
    }

    /**
     * 判断指定用途是否对当前使用者解锁。
     * <p>
     * - 非玩家实体：默认视为已解锁（避免 NPC/其他实体被无意义地禁用）。<br>
     * - 玩家：依赖 {@link NianTouUnlocks} 记录进行判定。<br>
     * </p>
     */
    public static boolean isUsageUnlocked(
        LivingEntity user,
        ItemStack stack,
        String usageId
    ) {
        if (!(user instanceof Player player)) {
            return true;
        }
        if (stack == null || stack.isEmpty() || usageId == null || usageId.isBlank()) {
            return false;
        }

        NianTouUnlocks unlocks = resolveUnlocks(player);
        if (unlocks == null) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return unlocks.isUsageUnlocked(itemId, usageId);
    }

    private static NianTouUnlocks resolveUnlocks(Player player) {
        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks != null) {
            return unlocks;
        }
        try {
            return player.getData(KongqiaoAttachments.NIANTOU_UNLOCKS.get());
        } catch (Exception ignored) {
            return null;
        }
    }
}
