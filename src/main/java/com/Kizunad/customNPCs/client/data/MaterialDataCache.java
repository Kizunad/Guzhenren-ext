package com.Kizunad.customNPCs.client.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端材料点缓存，供 UI 查询。
 */
public final class MaterialDataCache {

    private static final Map<Integer, Double> OWNER_MATERIAL = new ConcurrentHashMap<>();

    private MaterialDataCache() {}

    public static void updateOwnerMaterial(int npcId, double value) {
        OWNER_MATERIAL.put(npcId, value);
    }

    public static double getOwnerMaterial(int npcId) {
        return OWNER_MATERIAL.getOrDefault(npcId, 0.0D);
    }

    public static void clear(int npcId) {
        OWNER_MATERIAL.remove(npcId);
    }
}
