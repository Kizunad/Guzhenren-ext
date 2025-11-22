package com.Kizunad.customNPCs_test;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs_test.items.MindInspectorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 测试内容注册表
 */
public class TestRegistry {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(CustomNPCsMod.MODID);
    
    public static final DeferredHolder<Item, MindInspectorItem> MIND_INSPECTOR = ITEMS.register("mind_inspector", 
        () -> new MindInspectorItem(new Item.Properties().stacksTo(1)));
        
    /**
     * 需要在主模组构造函数中调用此方法来注册 DeferredRegister
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
