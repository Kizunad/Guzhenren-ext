package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.DeepPillFoods;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.DeepPillItem;
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

    public static final DeferredItem<PillItem> XIAO_HUAN_DAN = ITEMS.register(
        "xiao_huan_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> JU_QI_SAN = ITEMS.register(
        "ju_qi_san",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> CUI_TI_DAN = ITEMS.register(
        "cui_ti_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> JI_FENG_DAN = ITEMS.register(
        "ji_feng_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> TIE_GU_DAN = ITEMS.register(
        "tie_gu_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> BI_DU_DAN = ITEMS.register(
        "bi_du_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> PO_HUAN_DAN = ITEMS.register(
        "po_huan_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> GUI_XI_DAN = ITEMS.register(
        "gui_xi_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> BI_HUO_DAN = ITEMS.register(
        "bi_huo_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> YE_SHI_DAN = ITEMS.register(
        "ye_shi_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> BAO_SHI_DAN = ITEMS.register(
        "bao_shi_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> QING_SHEN_DAN = ITEMS.register(
        "qing_shen_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> YIN_XI_DAN = ITEMS.register(
        "yin_xi_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> KUANG_BAO_DAN = ITEMS.register(
        "kuang_bao_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> NING_SHEN_DAN = ITEMS.register(
        "ning_shen_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> SHOU_LIANG_WAN = ITEMS.register(
        "shou_liang_wan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> LING_ZHI_YE = ITEMS.register(
        "ling_zhi_ye",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> BI_GU_DAN = ITEMS.register(
        "bi_gu_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> QU_SHOU_SAN = ITEMS.register(
        "qu_shou_san",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<PillItem> XUN_MAI_DAN = ITEMS.register(
        "xun_mai_dan",
        () -> new PillItem(new Item.Properties().stacksTo(PILL_STACK_SIZE), PillQuality.HUANG)
    );

    public static final DeferredItem<DeepPillItem> SHENG_SI_ZAO_HUA_DAN = ITEMS.register(
        "sheng_si_zao_hua_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.LIFE_DEATH_REBIRTH
        )
    );

    public static final DeferredItem<DeepPillItem> QIANG_ZHI_PO_JING_DAN = ITEMS.register(
        "qiang_zhi_po_jing_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.FORCED_BREAKTHROUGH
        )
    );

    public static final DeferredItem<DeepPillItem> XI_SUI_FA_GU_DAN = ITEMS.register(
        "xi_sui_fa_gu_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.MARROW_REFORGE
        )
    );

    public static final DeferredItem<DeepPillItem> NI_SHI_DAN = ITEMS.register(
        "ni_shi_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.TIME_REVERSAL
        )
    );

    public static final DeferredItem<DeepPillItem> YIN_ZAI_DAN = ITEMS.register(
        "yin_zai_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.TRIBULATION_LURE
        )
    );

    public static final DeferredItem<DeepPillItem> SAN_GONG_DAN = ITEMS.register(
        "san_gong_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.POWER_DISPERSE
        )
    );

    public static final DeferredItem<DeepPillItem> NU_SHOU_YIN = ITEMS.register(
        "nu_shou_yin",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.BEAST_BRAND
        )
    );

    public static final DeferredItem<DeepPillItem> DUO_TIAN_DAN = ITEMS.register(
        "duo_tian_dan",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.HEAVEN_SNATCH
        )
    );

    public static final DeferredItem<DeepPillItem> WU_DAO_CHA = ITEMS.register(
        "wu_dao_cha",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.ENLIGHTENMENT_TEA
        )
    );

    public static final DeferredItem<DeepPillItem> SU_TI_NI = ITEMS.register(
        "su_ti_ni",
        () -> new DeepPillItem(
            new Item.Properties().stacksTo(PILL_STACK_SIZE).food(DeepPillFoods.DEEP_PILL_FOOD),
            DeepPillItem.Mechanism.BODY_RESHAPE
        )
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
