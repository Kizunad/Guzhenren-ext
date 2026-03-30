package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoOwner;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 根据最大真元动态调整空窍容量，并处理溢出物品的回收。
 * <p>
 * 容量计算已迁移至 {@link KongqiaoCapacityBridge}，该服务保留同步入口和物品回收逻辑。
 * 总行数 = 资质基础行数 + 修为追加行数，受限于 {@link KongqiaoConstants#MAX_ROWS}。
 * </p>
 */
public final class KongqiaoCapacityService {

    private static final float DEFAULT_DROP_OFFSET = 0.25F;

    private KongqiaoCapacityService() {}

    /**
     * 同步空窍容量至当前状态。
     * <p>
     * 现在使用 {@link KongqiaoCapacityBridge} 计算容量：
     * <ul>
     *   <li>资质基础行数来自资质档位</li>
     *   <li>修为追加行数来自最大真元</li>
     *   <li>总行数 = 基础行数 + 追加行数，受限于最大行数</li>
     * </ul>
     * </p>
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

        // 使用权威桥接层计算容量画像
        KongqiaoCapacityProfile profile = KongqiaoCapacityBridge.resolveFromEntity(entity);
        int targetRows = profile.totalRows();
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

    /**
     * @deprecated 请使用 {@link KongqiaoCapacityBridge#resolveFromEntity(LivingEntity)}
     *             获取完整的容量画像，包括资质基础行数和修为追加行数。
     *             该方法保留仅用于向后兼容。
     */
    @Deprecated
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

    /**
     * 获取当前容量画像的便捷方法。
     * <p>
     * 供外部调用者查询当前容量状态，不触发同步。
     * </p>
     *
     * @param entity 玩家实体
     * @return 容量画像，如果实体无效则返回默认画像
     */
    public static KongqiaoCapacityProfile getCapacityProfile(LivingEntity entity) {
        return KongqiaoCapacityBridge.resolveFromEntity(entity);
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
