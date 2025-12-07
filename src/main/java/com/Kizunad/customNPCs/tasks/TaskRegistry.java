package com.Kizunad.customNPCs.tasks;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务注册表（数据驱动）。
 */
public final class TaskRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRegistry.class);
    private static final TaskRegistry INSTANCE = new TaskRegistry();

    private Map<ResourceLocation, TaskDefinition> definitions = Collections.emptyMap();

    private TaskRegistry() {}

    public static TaskRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void reload(Map<ResourceLocation, TaskDefinition> incoming) {
        definitions = Map.copyOf(incoming);
        LOGGER.info("[TaskRegistry] 成功加载任务定义数量: {}", definitions.size());
    }

    public TaskDefinition get(ResourceLocation id) {
        return definitions.get(id);
    }

    public Collection<TaskDefinition> all() {
        return definitions.values();
    }

    /**
     * 随机选取若干默认任务，用于 NPC 初始任务池，避免所有 NPC 任务完全一致。
     */
    public List<ResourceLocation> pickDefaultIds(int limit, RandomSource random) {
        if (limit <= 0 || definitions.isEmpty()) {
            return List.of();
        }

        List<ResourceLocation> pool = new java.util.ArrayList<>(definitions.keySet());
        List<ResourceLocation> picked = new java.util.ArrayList<>();
        while (!pool.isEmpty() && picked.size() < limit) {
            int index = random.nextInt(pool.size());
            picked.add(pool.remove(index));
        }
        return picked;
    }
}
