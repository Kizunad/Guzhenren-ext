package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.DeepPillFoods;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.DeepPillItem;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillItem;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillQuality;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
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

    /** 青亚草作物入口。 */
    public static final DeferredItem<ItemNameBlockItem> QING_YA_GRASS_ITEM = registerShallowCropItem(
        "qing_ya_grass",
        FarmingBlocks.QING_YA_GRASS
    );

    public static final DeferredItem<ItemNameBlockItem> NING_XUE_GEN_ITEM = registerShallowCropItem(
        "ning_xue_gen",
        FarmingBlocks.NING_XUE_GEN
    );

    public static final DeferredItem<ItemNameBlockItem> JU_YUAN_FLOWER_ITEM = registerShallowCropItem(
        "ju_yuan_flower",
        FarmingBlocks.JU_YUAN_FLOWER
    );

    public static final DeferredItem<ItemNameBlockItem> XI_SUI_VINE_ITEM = registerShallowCropItem(
        "xi_sui_vine",
        FarmingBlocks.XI_SUI_VINE
    );

    public static final DeferredItem<ItemNameBlockItem> TIE_PI_BAMBOO_ITEM = registerShallowCropItem(
        "tie_pi_bamboo",
        FarmingBlocks.TIE_PI_BAMBOO
    );

    public static final DeferredItem<ItemNameBlockItem> HUO_LING_ZHI_MUSHROOM_ITEM = registerShallowCropItem(
        "huo_ling_zhi_mushroom",
        FarmingBlocks.HUO_LING_ZHI_MUSHROOM
    );

    public static final DeferredItem<ItemNameBlockItem> BING_XIN_GRASS_ITEM = registerShallowCropItem(
        "bing_xin_grass",
        FarmingBlocks.BING_XIN_GRASS
    );

    public static final DeferredItem<ItemNameBlockItem> HUAN_DU_MUSHROOM_ITEM = registerShallowCropItem(
        "huan_du_mushroom",
        FarmingBlocks.HUAN_DU_MUSHROOM
    );

    public static final DeferredItem<ItemNameBlockItem> YING_TAI_LICHEN_ITEM = registerShallowCropItem(
        "ying_tai_lichen",
        FarmingBlocks.YING_TAI_LICHEN
    );

    public static final DeferredItem<ItemNameBlockItem> CI_VINE_ITEM = registerShallowCropItem(
        "ci_vine",
        FarmingBlocks.CI_VINE
    );

    public static final DeferredItem<ItemNameBlockItem> JIAN_YE_GRASS_ITEM = registerShallowCropItem(
        "jian_ye_grass",
        FarmingBlocks.JIAN_YE_GRASS
    );

    public static final DeferredItem<ItemNameBlockItem> CHEN_SHUI_LILY_PAD_ITEM = registerShallowCropItem(
        "chen_shui_lily_pad",
        FarmingBlocks.CHEN_SHUI_LILY_PAD
    );

    public static final DeferredItem<ItemNameBlockItem> DI_LONG_BERRY_BUSH_ITEM = registerShallowCropItem(
        "di_long_berry_bush",
        FarmingBlocks.DI_LONG_BERRY_BUSH
    );

    public static final DeferredItem<ItemNameBlockItem> FENG_XIN_ZI_ITEM = registerShallowCropItem(
        "feng_xin_zi",
        FarmingBlocks.FENG_XIN_ZI
    );

    public static final DeferredItem<ItemNameBlockItem> LEI_GU_SAPLING_ITEM = registerShallowCropItem(
        "lei_gu_sapling",
        FarmingBlocks.LEI_GU_SAPLING
    );

    public static final DeferredItem<ItemNameBlockItem> SHI_YIN_GRASS_ITEM = registerShallowCropItem(
        "shi_yin_grass",
        FarmingBlocks.SHI_YIN_GRASS
    );

    public static final DeferredItem<ItemNameBlockItem> CHUN_YANG_FLOWER_ITEM = registerShallowCropItem(
        "chun_yang_flower",
        FarmingBlocks.CHUN_YANG_FLOWER
    );

    public static final DeferredItem<ItemNameBlockItem> YAN_SHOU_COCOA_ITEM = registerShallowCropItem(
        "yan_shou_cocoa",
        FarmingBlocks.YAN_SHOU_COCOA
    );

    public static final DeferredItem<ItemNameBlockItem> WANG_YOU_GRASS_ITEM = registerShallowCropItem(
        "wang_you_grass",
        FarmingBlocks.WANG_YOU_GRASS
    );

    public static final DeferredItem<ItemNameBlockItem> SHE_YAN_MELON_STEM_ITEM = registerShallowCropItem(
        "she_yan_melon_stem",
        FarmingBlocks.SHE_YAN_MELON_STEM
    );

    public static final DeferredItem<BlockItem> LIGHTNING_ATTRACTING_FERN_ITEM = ITEMS.register(
        "lightning_attracting_fern",
        () -> new BlockItem(FarmingBlocks.LIGHTNING_ATTRACTING_FERN.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> MAN_EATING_SPORE_BLOSSOM_ITEM = ITEMS.register(
        "man_eating_spore_blossom",
        () -> new BlockItem(FarmingBlocks.MAN_EATING_SPORE_BLOSSOM.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> CAVE_VINES_ITEM = ITEMS.register(
        "cave_vines",
        () -> new BlockItem(FarmingBlocks.CAVE_VINES.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> LEI_YING_SHA = ITEMS.register(
        "lei_ying_sha",
        () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> XUE_PO_LI = ITEMS.register(
        "xue_po_li",
        () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> JIN_SUI_XIE = ITEMS.register(
        "jin_sui_xie",
        () -> new Item(new Item.Properties())
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

    private static DeferredItem<ItemNameBlockItem> registerShallowCropItem(
        String registryName,
        DeferredBlock<CropBlock> block
    ) {
        return ITEMS.register(registryName, () -> new ItemNameBlockItem(block.get(), new Item.Properties()));
    }

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
