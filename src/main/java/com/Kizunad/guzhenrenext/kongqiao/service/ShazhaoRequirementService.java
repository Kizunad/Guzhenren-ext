package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 杀招启用条件：空窍蛊虫校验。
 * <p>
 * 目前杀招 JSON 的 required_items 是“参与此杀招的蛊虫列表”。无论主动或被动，
 * 都必须在玩家空窍({@link KongqiaoInventory})已解锁槽位内实际持有这些蛊虫，才允许触发效果。
 * </p>
 */
public final class ShazhaoRequirementService {

    private ShazhaoRequirementService() {}

    /**
     * 收集空窍已解锁槽位内的物品 ID 集合。
     */
    public static Set<ResourceLocation> collectPresentItemIds(
        final KongqiaoInventory inventory
    ) {
        if (inventory == null) {
            return Set.of();
        }
        final int unlockedSlots = inventory.getSettings().getUnlockedSlots();
        final Set<ResourceLocation> present = new HashSet<>();
        for (int i = 0; i < unlockedSlots; i++) {
            final ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            present.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        return present;
    }

    /**
     * 检查杀招所需蛊虫是否全部存在于空窍中。
     * <p>
     * 若 required_items 缺失/非法，则视为条件不满足（避免仅凭解锁绕过物品要求）。
     * </p>
     */
    public static boolean hasAllRequiredItems(
        final Set<ResourceLocation> presentItemIds,
        final ShazhaoData data
    ) {
        if (data == null) {
            return false;
        }
        final List<String> requiredItems = data.requiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            return false;
        }
        if (presentItemIds == null || presentItemIds.isEmpty()) {
            return false;
        }
        for (String itemId : requiredItems) {
            if (itemId == null || itemId.isBlank()) {
                return false;
            }
            final ResourceLocation parsed;
            try {
                parsed = ResourceLocation.parse(itemId);
            } catch (Exception e) {
                return false;
            }
            if (!presentItemIds.contains(parsed)) {
                return false;
            }
        }
        return true;
    }
}
