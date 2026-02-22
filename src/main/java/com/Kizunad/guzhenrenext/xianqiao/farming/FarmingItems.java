package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillItem;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillQuality;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 农耕体系物品注册表。
 */
public final class FarmingItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GuzhenrenExt.MODID);

    /** 丹药默认堆叠上限。 */
    private static final int PILL_STACK_SIZE = 16;

    /** 炼丹炉方块物品。 */
    public static final DeferredItem<BlockItem> ALCHEMY_FURNACE_ITEM = ITEMS.register(
        "alchemy_furnace",
        () -> new BlockItem(FarmingBlocks.ALCHEMY_FURNACE.get(), new Item.Properties())
    );

    /** 催生丹。 */
    public static final DeferredItem<PillItem> CUI_SHENG_DAN = ITEMS.register(
        "cui_sheng_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    /** 护体丹。 */
    public static final DeferredItem<PillItem> HU_TI_DAN = ITEMS.register(
        "hu_ti_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    /** 回春丹。 */
    public static final DeferredItem<PillItem> HUI_CHUN_DAN = ITEMS.register(
        "hui_chun_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    /** 润泽丹。 */
    public static final DeferredItem<PillItem> RUN_ZE_DAN = ITEMS.register(
        "run_ze_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    private FarmingItems() {
    }

    /**
     * 注册农耕体系物品。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
