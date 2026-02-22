package com.Kizunad.guzhenrenext.xianqiao.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 储物蛊数据模型，采用“物品 ID -> long 总量”表示可超 64 堆叠库存。
 */
public class StorageGuData {

    private static final String KEY_STORAGE_GU = "storage_gu";
    private static final String KEY_ENTRIES = "entries";
    private static final String KEY_ITEM = "item";
    private static final String KEY_COUNT = "count";
    private static final long MIN_VALID_COUNT = 1L;
    private static final int TAG_COMPOUND = Tag.TAG_COMPOUND;
    private static final int TAG_LIST = Tag.TAG_LIST;
    private static final int TAG_STRING = Tag.TAG_STRING;
    private static final int TAG_NUMERIC = Tag.TAG_ANY_NUMERIC;

    private final Map<ResourceLocation, Long> totals = new HashMap<>();

    public static StorageGuData fromItemStack(ItemStack stack) {
        StorageGuData data = new StorageGuData();
        if (stack == null || stack.isEmpty()) {
            return data;
        }
        CompoundTag customDataTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!customDataTag.contains(KEY_STORAGE_GU, TAG_COMPOUND)) {
            return data;
        }
        data.readFromTag(customDataTag.getCompound(KEY_STORAGE_GU));
        return data;
    }

    public void writeToItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag customDataTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        customDataTag.put(KEY_STORAGE_GU, writeToTag());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
    }

    public long getCount(Item item) {
        if (item == null) {
            return 0L;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null) {
            return 0L;
        }
        return getCount(itemId);
    }

    public long getCount(ResourceLocation itemId) {
        if (itemId == null) {
            return 0L;
        }
        return totals.getOrDefault(itemId, 0L);
    }

    /**
     * 增加数量，溢出时采用饱和保护：上限固定为 {@link Long#MAX_VALUE}。
     */
    public long add(ResourceLocation itemId, long delta) {
        if (itemId == null || delta <= 0L) {
            return 0L;
        }
        long current = getCount(itemId);
        long room = Long.MAX_VALUE - current;
        long accepted = Math.min(delta, room);
        if (accepted <= 0L) {
            return 0L;
        }
        totals.put(itemId, current + accepted);
        return accepted;
    }

    public long remove(ResourceLocation itemId, long delta) {
        if (itemId == null || delta <= 0L) {
            return 0L;
        }
        long current = getCount(itemId);
        long removed = Math.min(current, delta);
        long left = current - removed;
        if (left <= 0L) {
            totals.remove(itemId);
        } else {
            totals.put(itemId, left);
        }
        return removed;
    }

    public Map<ResourceLocation, Long> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(totals));
    }

    public long getTotalCountSaturated() {
        long total = 0L;
        for (long count : totals.values()) {
            long room = Long.MAX_VALUE - total;
            long accepted = Math.min(count, room);
            total += accepted;
            if (total == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
        }
        return total;
    }

    public CompoundTag writeToTag() {
        CompoundTag root = new CompoundTag();
        ListTag entries = new ListTag();
        for (Map.Entry<ResourceLocation, Long> entry : totals.entrySet()) {
            long count = entry.getValue();
            if (count < MIN_VALID_COUNT) {
                continue;
            }
            CompoundTag itemTag = new CompoundTag();
            itemTag.putString(KEY_ITEM, entry.getKey().toString());
            itemTag.putLong(KEY_COUNT, count);
            entries.add(itemTag);
        }
        root.put(KEY_ENTRIES, entries);
        return root;
    }

    public void readFromTag(CompoundTag root) {
        totals.clear();
        if (root == null || !root.contains(KEY_ENTRIES, TAG_LIST)) {
            return;
        }
        ListTag entries = root.getList(KEY_ENTRIES, TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            Tag rawTag = entries.get(index);
            if (!(rawTag instanceof CompoundTag itemTag)) {
                continue;
            }
            if (!itemTag.contains(KEY_ITEM, TAG_STRING) || !itemTag.contains(KEY_COUNT, TAG_NUMERIC)) {
                continue;
            }
            ResourceLocation itemId = ResourceLocation.tryParse(itemTag.getString(KEY_ITEM));
            if (itemId == null) {
                continue;
            }
            long count = itemTag.getLong(KEY_COUNT);
            if (count < MIN_VALID_COUNT) {
                continue;
            }
            mergeSaturated(itemId, count);
        }
    }

    private void mergeSaturated(ResourceLocation itemId, long count) {
        long current = totals.getOrDefault(itemId, 0L);
        long merged = current + count;
        if (merged < current) {
            totals.put(itemId, Long.MAX_VALUE);
            return;
        }
        totals.put(itemId, merged);
    }

    /**
     * 储物蛊菜单读取/写入抽象接口，供后续菜单层直接对接 long 语义库存。
     */
    public interface StorageGuHandler {

        long getCount(ResourceLocation itemId);

        long insert(ResourceLocation itemId, long amount);

        long extract(ResourceLocation itemId, long amount);

        Map<ResourceLocation, Long> snapshot();
    }
}
