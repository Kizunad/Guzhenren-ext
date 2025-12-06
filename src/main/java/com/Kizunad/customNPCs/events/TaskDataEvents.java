package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.tasks.TaskDefinitionReloader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 任务数据监听器注册。
 */
public class TaskDataEvents {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new TaskDefinitionReloader());
    }
}
