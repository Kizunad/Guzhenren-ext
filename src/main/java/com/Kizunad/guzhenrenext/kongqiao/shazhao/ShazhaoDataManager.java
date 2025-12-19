package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * 杀招数据管理器。
 * <p>
 * 负责缓存与检索 data/guzhenrenext/shazhao/*.json 中加载的杀招配置。
 * </p>
 */
public final class ShazhaoDataManager {

    private static final Map<ResourceLocation, ShazhaoData> DATA_MAP =
        new HashMap<>();

    private ShazhaoDataManager() {}

    public static void clear() {
        DATA_MAP.clear();
    }

    public static void register(ShazhaoData data) {
        if (data == null || data.shazhaoID() == null) {
            return;
        }
        ResourceLocation id;
        try {
            id = ResourceLocation.parse(data.shazhaoID());
        } catch (Exception e) {
            return;
        }
        DATA_MAP.put(id, data);
    }

    @Nullable
    public static ShazhaoData get(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        return DATA_MAP.get(id);
    }

    public static Collection<ShazhaoData> getAll() {
        return List.copyOf(DATA_MAP.values());
    }
}
