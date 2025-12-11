package com.Kizunad.guzhenrenext.kongqiao.niantou;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
     * 注册物品的念头数据（用于代码硬编码注册或测试）。
     */
    public static void register(NianTouData data) {
        ResourceLocation id = ResourceLocation.parse(data.itemID());
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item != null) {
            DATA_MAP.put(item, data);
        }
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
}
