package com.Kizunad.customNPCs;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.events.NpcMindEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CustomNPCsMod.MODID)
public class CustomNPCsMod {
    public static final String MODID = "customnpcs";
    
    public CustomNPCsMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 Data Attachments
        NpcMindAttachment.ATTACHMENT_TYPES.register(modEventBus);
        
        // 注册测试内容
        com.Kizunad.customNPCs_test.TestRegistry.register(modEventBus);
        
        // 注册事件监听器
        NeoForge.EVENT_BUS.register(new NpcMindEvents());
    }
}
