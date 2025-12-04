package com.Kizunad.customNPCs.ai.status;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.dto.NpcStatusEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * 状态提供者注册表。
 * <p>
 * 保持注册顺序, 便于在 UI 中按模块顺序渲染状态行。
 * </p>
 */
public final class StatusProviderRegistry {

    private static final Map<ResourceLocation, IStatusProvider> PROVIDERS =
        new LinkedHashMap<>();

    private StatusProviderRegistry() {}

    /**
     * 注册一个状态提供者。
     *
     * @param id 唯一标识, 推荐使用模组命名空间
     * @param provider 状态提供者实现
     */
    public static void register(
        ResourceLocation id,
        IStatusProvider provider
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(provider, "provider");
        synchronized (PROVIDERS) {
            PROVIDERS.put(id, provider);
        }
    }

    /**
     * 拉取所有已注册提供者的状态数据。
     *
     * @param npc 目标 NPC
     * @return 聚合后的状态列表, 复制后的不可变 List
     */
    public static List<NpcStatusEntry> collect(CustomNpcEntity npc) {
        Objects.requireNonNull(npc, "npc");
        List<NpcStatusEntry> results = new ArrayList<>();
        synchronized (PROVIDERS) {
            for (IStatusProvider provider : PROVIDERS.values()) {
                List<NpcStatusEntry> provided = provider.collectStatuses(npc);
                if (provided == null || provided.isEmpty()) {
                    continue;
                }
                for (NpcStatusEntry entry : provided) {
                    if (entry != null) {
                        results.add(entry);
                    }
                }
            }
        }
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(results);
    }
}
