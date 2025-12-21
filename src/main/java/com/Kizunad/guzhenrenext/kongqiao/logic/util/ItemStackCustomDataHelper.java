package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * ItemStack 自定义数据（CUSTOM_DATA）读写工具。
 * <p>
 * Minecraft 1.21+ 已将大部分物品 NBT 迁移到 DataComponents，这里统一用
 * {@link DataComponents#CUSTOM_DATA} 作为可写的扩展存储。
 * </p>
 */
public final class ItemStackCustomDataHelper {

    private ItemStackCustomDataHelper() {}

    /**
     * 获取 CustomData 的 tag 副本（永不返回 null）。
     */
    public static CompoundTag copyCustomDataTag(final ItemStack stack) {
        if (stack == null) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /**
     * 将 tag 写回 CustomData（tag 为 null 时写入空 tag）。
     */
    public static void setCustomDataTag(final ItemStack stack, final CompoundTag tag) {
        if (stack == null) {
            return;
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag == null ? new CompoundTag() : tag));
    }

    /**
     * 删除指定 key，并写回（若 stack 为 null 则忽略）。
     */
    public static void removeKey(final ItemStack stack, final String key) {
        if (stack == null || key == null) {
            return;
        }
        final CompoundTag tag = copyCustomDataTag(stack);
        tag.remove(key);
        setCustomDataTag(stack, tag);
    }
}

