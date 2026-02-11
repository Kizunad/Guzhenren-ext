package com.Kizunad.guzhenrenext.xianqiao.item;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍物品注册表。
 */
public final class XianqiaoItems {

    private XianqiaoItems() {
    }

    /**
     * 物品延迟注册器。
     */
    private static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(GuzhenrenExt.MODID);

    /**
     * 九天碎片物品注册项。
     */
    public static final DeferredItem<HeavenlyFragmentItem> HEAVENLY_FRAGMENT =
        ITEMS.registerItem(
            "heavenly_fragment",
            HeavenlyFragmentItem::new,
            new Item.Properties().stacksTo(64)
        );

    /**
     * 注册物品。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
