package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.ai.interaction.MaterialValueReloader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 数据包重载事件：注册材料表监听器。
 */
public class MaterialValueEvents {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MaterialValueReloader());
    }
}
