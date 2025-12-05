package com.Kizunad.customNPCs.ai.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

/**
 * 简单的 NPC 背包实现（参考玩家背包大小）。
 * <p>
 * 仅负责基础的存取与序列化，不处理界面交互。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class NpcInventory implements Container {

    public static final int MAIN_SIZE = 36; // 与玩家主背包一致
    public static final int ARMOR_SIZE = 4; // 头、胸、腿、脚
    public static final int OFFHAND_SIZE = 1;
    public static final int TOTAL_SIZE = MAIN_SIZE + ARMOR_SIZE + OFFHAND_SIZE;

    private final NonNullList<ItemStack> items;
    private Player viewer;

    public NpcInventory() {
        this(TOTAL_SIZE);
    }

    public NpcInventory(int size) {
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void setViewer(Player player) {
        this.viewer = player;
    }

    /**
     * 尝试添加物品。
     * @param stack 要添加的物品
     * @return 未能放入的剩余物品（全部放入则返回 EMPTY）
     *
     * 注意：会直接修改传入的 stack，剩余未放入的数量保留在该栈中，调用方无需再额外缩减以避免复制。
     */
    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack;

        // 先尝试与已有同类物品合并
        for (int i = 0; i < items.size(); i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(slot, remaining)) {
                int transferable = Math.min(
                    remaining.getCount(),
                    slot.getMaxStackSize() - slot.getCount()
                );
                if (transferable > 0) {
                    slot.grow(transferable);
                    remaining.shrink(transferable);
                }
            }

            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        // 再尝试放入空槽位
        for (int i = 0; i < items.size() && !remaining.isEmpty(); i++) {
            if (!items.get(i).isEmpty()) {
                continue;
            }
            int toInsert = Math.min(
                remaining.getCount(),
                remaining.getMaxStackSize()
            );
            items.set(i, remaining.copyWithCount(toInsert));
            remaining.shrink(toInsert);
        }

        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    /**
     * 获取指定槽位的物品。
     */
    public ItemStack getItem(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        return items.get(slot);
    }

    /**
     * 设置指定槽位的物品。
     */
    public void setItem(int slot, ItemStack stack) {
        if (!isValidSlot(slot)) {
            return;
        }
        items.set(slot, stack);
    }

    /**
     * 移除指定槽位的整堆物品。
     * @return 被移除的物品
     */
    public ItemStack removeItem(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return removed;
    }

    /**
     * 按数量移除指定槽位的物品。
     * @param slot 槽位
     * @param amount 数量
     * @return 移除的物品
     */
    public ItemStack removeItem(int slot, int amount) {
        if (!isValidSlot(slot) || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.split(amount);
    }

    /**
     * 查找第一个满足条件的槽位。
     * @param predicate 条件
     * @return 槽位索引，未找到返回 -1
     */
    public int findFirstSlot(Predicate<ItemStack> predicate) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 是否存在满足条件的物品。
     */
    public boolean anyMatch(Predicate<ItemStack> predicate) {
        return findFirstSlot(predicate) >= 0;
    }

    public int size() {
        return items.size();
    }

    /**
     * 统计非空槽位数量。
     */
    public int getFilledSlotCount() {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 背包填充比例（0.0-1.0）。
     */
    public float getFillRatio() {
        if (items.isEmpty()) {
            return 0.0F;
        }
        return (float) getFilledSlotCount() / (float) items.size();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, provider);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < items.size();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    // ========== Container 接口 ==========
    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setChanged() {
        // 无额外标记
    }

    @Override
    public boolean stillValid(Player player) {
        if (player == null) {
            return true;
        }
        return viewer == null || viewer == player;
    }
}
