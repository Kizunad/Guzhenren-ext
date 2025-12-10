package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoOwner;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 根据最大真元动态调整空窍容量，并处理溢出物品的回收。
 */
public final class KongqiaoCapacityService {

    private static final float DEFAULT_DROP_OFFSET = 0.25F;

    private KongqiaoCapacityService() {}

    /**
     * 同步空窍容量至当前最大真元。
     *
     * @return true 表示容量发生变化
     */
    public static boolean syncCapacity(LivingEntity entity, KongqiaoOwner owner) {
        if (
            entity == null ||
            owner == null ||
            entity.level().isClientSide() ||
            owner.isClientSide()
        ) {
            return false;
        }

        KongqiaoInventory inventory = owner.getKongqiaoInventory();
        if (inventory == null) {
            return false;
        }

        KongqiaoSettings settings = inventory.getSettings();
        int currentSlots = settings.getUnlockedSlots();

        double maxZhenyuan = Math.max(0.0D, ZhenYuanHelper.getMaxAmount(entity));
        int targetRows = computeTargetRows(maxZhenyuan);
        int targetSlots = targetRows * inventory.getColumns();

        if (targetSlots == currentSlots) {
            return false;
        }

        settings.setUnlockedRows(targetRows);
        if (targetSlots < currentSlots) {
            reclaimOverflowItems(entity, inventory, targetSlots);
        }
        inventory.setChanged();
        owner.markKongqiaoDirty();
        return true;
    }

    private static int computeTargetRows(double maxZhenyuan) {
        if (KongqiaoConstants.FULL_CAPACITY_ZHENYUAN <= 0.0D) {
            return KongqiaoConstants.DYNAMIC_MAX_ROWS;
        }
        double ratio =
            Math.max(0.0D, Math.min(1.0D, maxZhenyuan / KongqiaoConstants.FULL_CAPACITY_ZHENYUAN));
        int rows = (int) Math.round(
            ratio * KongqiaoConstants.DYNAMIC_MAX_ROWS
        );
        return Math.max(0, Math.min(rows, KongqiaoConstants.DYNAMIC_MAX_ROWS));
    }

    private static void reclaimOverflowItems(
        LivingEntity entity,
        KongqiaoInventory inventory,
        int unlockedSlots
    ) {
        for (int slot = unlockedSlots; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack toReturn = stack.copy();
            inventory.setItem(slot, ItemStack.EMPTY);
            returnItem(entity, toReturn);
        }
    }

    private static void returnItem(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (entity instanceof Player player) {
            boolean added = player.getInventory().add(stack);
            if (!added) {
                player.drop(stack, false);
            }
            return;
        }
        entity.spawnAtLocation(stack, DEFAULT_DROP_OFFSET);
    }
}
