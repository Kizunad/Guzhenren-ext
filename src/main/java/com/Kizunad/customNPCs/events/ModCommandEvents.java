package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.commands.MindDebugCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 模组命令注册事件
 */
@EventBusSubscriber(modid = CustomNPCsMod.MODID)
public class ModCommandEvents {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MindDebugCommand.register(event.getDispatcher());
    }
}
