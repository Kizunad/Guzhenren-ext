package com.Kizunad.customNPCs.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
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
     * 选取若干默认任务（按注册顺序），用于 NPC 初始任务池。
     */
    public List<ResourceLocation> pickDefaultIds(int limit) {
        List<ResourceLocation> list = new ArrayList<>();
        if (limit <= 0) {
            return list;
        }
        for (ResourceLocation id : definitions.keySet()) {
            list.add(id);
            if (list.size() >= limit) {
                break;
            }
        }
        return list;
    }
}
