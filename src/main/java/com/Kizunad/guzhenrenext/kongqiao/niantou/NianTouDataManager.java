package com.Kizunad.guzhenrenext.kongqiao.niantou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 念头数据管理器。
 * <p>
 * 负责存储和查找物品对应的 NianTouData 配置。
 * 未来应升级为支持 DataPack (JsonResourceReloadListener)。
 * </p>
 */
public final class NianTouDataManager {

    private static final Map<Item, NianTouData> DATA_MAP = new HashMap<>();

    private NianTouDataManager() {}

    /**
     * 通过 usageId 反查其所属物品与用途配置。
     * <p>
     * 该方法主要用于 UI（如轮盘/调整面板）根据 usageId 展示图标与标题。
     * </p>
     *
     * @param usageId 用途 ID
     * @return 查找到则返回 {@link UsageLookup}；未找到返回 null
     */
    @Nullable
    public static UsageLookup findUsageLookup(final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return null;
        }
        for (Map.Entry<Item, NianTouData> entry : DATA_MAP.entrySet()) {
            final Item item = entry.getKey();
            final NianTouData data = entry.getValue();
            if (item == null || item == Items.AIR || data == null || data.usages() == null) {
                continue;
            }
            for (NianTouData.Usage usage : data.usages()) {
                if (usage == null || usage.usageID() == null) {
                    continue;
                }
                if (Objects.equals(usage.usageID(), usageId)) {
                    return new UsageLookup(item, data, usage);
                }
            }
        }
        return null;
    }

    /**
     * usageId 的反查结果。
     *
     * @param item 对应物品
     * @param data 该物品的念头数据
     * @param usage 用途配置
     */
    public record UsageLookup(Item item, NianTouData data, NianTouData.Usage usage) {}

    /**
     * 注册物品的念头数据（用于代码硬编码注册或测试）。
     */
    public static void register(NianTouData data) {
        if (data == null || data.itemID() == null || data.itemID().isBlank()) {
            return;
        }
        ResourceLocation id = ResourceLocation.parse(data.itemID());
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item == Items.AIR) {
            return;
        }
        DATA_MAP.put(item, data);
    }

    /**
     * 获取物品对应的念头数据。
     */
    @Nullable
    public static NianTouData getData(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return DATA_MAP.get(stack.getItem());
    }
    
    /**
     * 清空缓存（重载用）。
     */
    public static void clear() {
        DATA_MAP.clear();
    }

    /**
     * 为指定物品注册/更新一个用途（技能）。
     * <p>
     * 被动技能与未来的主动技能都统一以 {@link NianTouData.Usage} 表达；轮盘/按钮等 UI
     * 只需要从对应物品的 usages 中筛选并触发 {@link com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect#onActivate}
     * 即可。
     * </p>
     * <p>
     * 行为约定：若已存在同名 {@code usageID}，则替换为新值；若不存在则追加。
     * </p>
     *
     * @param item 目标物品
     * @param usage 要注册的用途（技能）
     * @return true 表示数据发生变化
     */
    public static boolean registerUsage(Item item, NianTouData.Usage usage) {
        if (item == null || item == Items.AIR || usage == null) {
            return false;
        }
        if (usage.usageID() == null || usage.usageID().isBlank()) {
            return false;
        }

        NianTouData existing = DATA_MAP.get(item);
        String itemId = existing != null && existing.itemID() != null && !existing.itemID().isBlank()
            ? existing.itemID()
            : BuiltInRegistries.ITEM.getKey(item).toString();

        List<NianTouData.Usage> base = existing == null || existing.usages() == null
            ? List.of()
            : existing.usages();

        boolean replaced = false;
        boolean changed = false;
        List<NianTouData.Usage> merged = new ArrayList<>(base.size() + 1);
        for (NianTouData.Usage current : base) {
            if (Objects.equals(current.usageID(), usage.usageID())) {
                replaced = true;
                changed = !current.equals(usage);
                merged.add(usage);
                continue;
            }
            merged.add(current);
        }
        if (!replaced) {
            merged.add(usage);
            changed = true;
        }
        if (!changed) {
            return false;
        }

        DATA_MAP.put(item, new NianTouData(itemId, List.copyOf(merged)));
        return true;
    }

    /**
     * 从指定物品移除一个用途（技能）。
     *
     * @param item 目标物品
     * @param usageId 用途 ID
     * @return true 表示数据发生变化
     */
    public static boolean removeUsage(Item item, String usageId) {
        if (item == null || item == Items.AIR || usageId == null || usageId.isBlank()) {
            return false;
        }

        NianTouData existing = DATA_MAP.get(item);
        if (existing == null || existing.usages() == null || existing.usages().isEmpty()) {
            return false;
        }

        boolean removed = false;
        List<NianTouData.Usage> filtered = new ArrayList<>(existing.usages().size());
        for (NianTouData.Usage current : existing.usages()) {
            if (Objects.equals(current.usageID(), usageId)) {
                removed = true;
                continue;
            }
            filtered.add(current);
        }

        if (!removed) {
            return false;
        }

        if (filtered.isEmpty()) {
            DATA_MAP.remove(item);
            return true;
        }
        DATA_MAP.put(item, new NianTouData(existing.itemID(), List.copyOf(filtered)));
        return true;
    }

    /**
     * 取消注册某个物品的全部用途（技能）。
     *
     * @param item 目标物品
     * @return true 表示数据发生变化
     */
    public static boolean unregister(Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }
        return DATA_MAP.remove(item) != null;
    }

    /**
     * 按物品直接获取念头数据。
     */
    @Nullable
    public static NianTouData getData(Item item) {
        if (item == null) {
            return null;
        }
        return DATA_MAP.get(item);
    }
}
