package com.Kizunad.guzhenrenext.xianqiao.item;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍物品注册表。
 */
public final class XianqiaoItems {

    /** 集群 NPC 刷怪蛋主色。 */
    private static final int CLUSTER_NPC_EGG_PRIMARY_COLOR = 0x7D7D7D;

    /** 集群 NPC 刷怪蛋副色。 */
    private static final int CLUSTER_NPC_EGG_SECONDARY_COLOR = 0x2F4858;

    /** 仙牛刷怪蛋主色。 */
    private static final int XIAN_COW_EGG_PRIMARY_COLOR = 0x4A2D1A;

    /** 仙牛刷怪蛋副色。 */
    private static final int XIAN_COW_EGG_SECONDARY_COLOR = 0xF5EEE5;

    /** 仙鸡刷怪蛋主色。 */
    private static final int XIAN_CHICKEN_EGG_PRIMARY_COLOR = 0xEFE8D8;

    /** 仙鸡刷怪蛋副色。 */
    private static final int XIAN_CHICKEN_EGG_SECONDARY_COLOR = 0xD95F22;

    /** 仙羊刷怪蛋主色。 */
    private static final int XIAN_SHEEP_EGG_PRIMARY_COLOR = 0xF1EEE9;

    /** 仙羊刷怪蛋副色。 */
    private static final int XIAN_SHEEP_EGG_SECONDARY_COLOR = 0xB08A5A;

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
     * 储物蛊物品注册项。
     */
    public static final DeferredItem<StorageGuItem> STORAGE_GU =
        ITEMS.registerItem(
            "storage_gu",
            StorageGuItem::new,
            new Item.Properties().stacksTo(1)
        );

    /**
     * 转运蛊物品注册项。
     */
    public static final DeferredItem<TransferGuItem> TRANSFER_GU =
        ITEMS.registerItem(
            "transfer_gu",
            TransferGuItem::new,
            new Item.Properties().stacksTo(1)
        );

    /** 集群 NPC 刷怪蛋注册项。 */
    public static final DeferredItem<SpawnEggItem> CLUSTER_NPC_SPAWN_EGG =
        ITEMS.register(
            "cluster_npc_spawn_egg",
            () ->
                new DeferredSpawnEggItem(
                    XianqiaoEntities.CLUSTER_NPC,
                    CLUSTER_NPC_EGG_PRIMARY_COLOR,
                    CLUSTER_NPC_EGG_SECONDARY_COLOR,
                    new Item.Properties()
                )
        );

    /** 仙牛刷怪蛋注册项。 */
    public static final DeferredItem<SpawnEggItem> XIAN_COW_SPAWN_EGG =
        ITEMS.register(
            "xian_cow_spawn_egg",
            () ->
                new DeferredSpawnEggItem(
                    XianqiaoEntities.XIAN_COW,
                    XIAN_COW_EGG_PRIMARY_COLOR,
                    XIAN_COW_EGG_SECONDARY_COLOR,
                    new Item.Properties()
                )
        );

    /** 仙鸡刷怪蛋注册项。 */
    public static final DeferredItem<SpawnEggItem> XIAN_CHICKEN_SPAWN_EGG =
        ITEMS.register(
            "xian_chicken_spawn_egg",
            () ->
                new DeferredSpawnEggItem(
                    XianqiaoEntities.XIAN_CHICKEN,
                    XIAN_CHICKEN_EGG_PRIMARY_COLOR,
                    XIAN_CHICKEN_EGG_SECONDARY_COLOR,
                    new Item.Properties()
                )
        );

    /** 仙羊刷怪蛋注册项。 */
    public static final DeferredItem<SpawnEggItem> XIAN_SHEEP_SPAWN_EGG =
        ITEMS.register(
            "xian_sheep_spawn_egg",
            () ->
                new DeferredSpawnEggItem(
                    XianqiaoEntities.XIAN_SHEEP,
                    XIAN_SHEEP_EGG_PRIMARY_COLOR,
                    XIAN_SHEEP_EGG_SECONDARY_COLOR,
                    new Item.Properties()
                )
        );

    /** 灵铁屑注册项。 */
    public static final DeferredItem<Item> LING_TIE_XIE =
        ITEMS.registerItem(
            "ling_tie_xie",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 赤铜砂注册项。 */
    public static final DeferredItem<Item> CHI_TONG_SHA =
        ITEMS.registerItem(
            "chi_tong_sha",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 玄煤晶注册项。 */
    public static final DeferredItem<Item> XUAN_MEI_JING =
        ITEMS.registerItem(
            "xuan_mei_jing",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 赤硝粉注册项。 */
    public static final DeferredItem<Item> CHI_XIAO_FEN =
        ITEMS.registerItem(
            "chi_xiao_fen",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 月银片注册项。 */
    public static final DeferredItem<Item> YUE_YIN_PIAN =
        ITEMS.registerItem(
            "yue_yin_pian",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 寒霜盐注册项。 */
    public static final DeferredItem<Item> HAN_SHUANG_YAN =
        ITEMS.registerItem(
            "han_shuang_yan",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 炎髓渣注册项。 */
    public static final DeferredItem<Item> YAN_SUI_ZHA =
        ITEMS.registerItem(
            "yan_sui_zha",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 风纹羽片注册项。 */
    public static final DeferredItem<Item> FENG_WEN_YU_PIAN =
        ITEMS.registerItem(
            "feng_wen_yu_pian",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 地脉砂注册项。 */
    public static final DeferredItem<Item> DI_MAI_SHA =
        ITEMS.registerItem(
            "di_mai_sha",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 云母片注册项。 */
    public static final DeferredItem<Item> YUN_MU_PIAN =
        ITEMS.registerItem(
            "yun_mu_pian",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 青木芯注册项。 */
    public static final DeferredItem<Item> QING_MU_XIN =
        ITEMS.registerItem(
            "qing_mu_xin",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 幽影墨注册项。 */
    public static final DeferredItem<Item> YOU_YING_MO =
        ITEMS.registerItem(
            "you_ying_mo",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 星辉尘注册项。 */
    public static final DeferredItem<Item> XING_HUI_CHEN =
        ITEMS.registerItem(
            "xing_hui_chen",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 玉髓团注册项。 */
    public static final DeferredItem<Item> YU_SUI_TUAN =
        ITEMS.registerItem(
            "yu_sui_tuan",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 灵硫块注册项。 */
    public static final DeferredItem<Item> LING_LIU_KUAI =
        ITEMS.registerItem(
            "ling_liu_kuai",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 玄冰晶注册项。 */
    public static final DeferredItem<Item> XUAN_BING_JING =
        ITEMS.registerItem(
            "xuan_bing_jing",
            Item::new,
            new Item.Properties().stacksTo(64)
        );

    /** 归元砾注册项。 */
    public static final DeferredItem<Item> GUI_YUAN_LI =
        ITEMS.registerItem(
            "gui_yuan_li",
            Item::new,
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
