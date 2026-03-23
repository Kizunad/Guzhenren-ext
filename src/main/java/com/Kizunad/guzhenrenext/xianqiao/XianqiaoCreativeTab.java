package com.Kizunad.guzhenrenext.xianqiao;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍系统创意模式标签页注册表。
 *
 * <p>该类统一维护仙窍系统在创意模式中的物品展示入口，避免分散在各注册类中难以维护。
 */
public final class XianqiaoCreativeTab {

    /**
     * 创意模式标签页延迟注册器。
     */
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GuzhenrenExt.MODID);

    /**
     * 仙窍创意标签页。
     *
     * <p>图标使用九天碎片，展示内容包含仙窍核心方块、炼丹炉方块与基础丹药。
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> XIANQIAO =
        CREATIVE_MODE_TABS.register(
            "xianqiao",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.guzhenrenext.xianqiao"))
                .icon(() -> new ItemStack(XianqiaoItems.HEAVENLY_FRAGMENT.get()))
                .displayItems((parameters, output) -> addGeneralTabItems(output))
                .build()
        );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> XIANQIAO_PILLS =
        CREATIVE_MODE_TABS.register(
            "xianqiao_pills",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.guzhenrenext.xianqiao_pills"))
                .icon(() -> new ItemStack(FarmingItems.CUI_SHENG_DAN.get()))
                .displayItems((parameters, output) -> addPillItems(output))
                .build()
        );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> XIANQIAO_PLANTS =
        CREATIVE_MODE_TABS.register(
            "xianqiao_plants",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.guzhenrenext.xianqiao_plants"))
                .icon(() -> new ItemStack(FarmingItems.QING_YA_GRASS_ITEM.get()))
                .displayItems((parameters, output) -> addPlantItems(output))
                .build()
        );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> XIANQIAO_CREATURES =
        CREATIVE_MODE_TABS.register(
            "xianqiao_creatures",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.guzhenrenext.xianqiao_creatures"))
                .icon(() -> new ItemStack(XianqiaoItems.CLUSTER_NPC_SPAWN_EGG.get()))
                .displayItems((parameters, output) -> addCreatureItems(output))
                .build()
        );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> XIANQIAO_MATERIALS =
        CREATIVE_MODE_TABS.register(
            "xianqiao_materials",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.guzhenrenext.xianqiao_materials"))
                .icon(() -> new ItemStack(XianqiaoItems.LING_TIE_XIE.get()))
                .displayItems((parameters, output) -> addMaterialItems(output))
                .build()
        );

    private static void addGeneralTabItems(CreativeModeTab.Output output) {
        output.accept(XianqiaoBlocks.APERTURE_CORE_ITEM.get());
        output.accept(XianqiaoBlocks.RESOURCE_CONTROLLER_ITEM.get());
        output.accept(XianqiaoBlocks.TIME_FIELD_COMPONENT_ITEM.get());
        output.accept(FarmingItems.ALCHEMY_FURNACE_ITEM.get());
        addPillItems(output);
        output.accept(XianqiaoItems.HEAVENLY_FRAGMENT.get());
        output.accept(XianqiaoItems.STORAGE_GU.get());
        output.accept(XianqiaoItems.TRANSFER_GU.get());
        addCreatureItems(output);
    }

    private static void addPillItems(CreativeModeTab.Output output) {
        output.accept(FarmingItems.CUI_SHENG_DAN.get());
        output.accept(FarmingItems.HU_TI_DAN.get());
        output.accept(FarmingItems.HUI_CHUN_DAN.get());
        output.accept(FarmingItems.RUN_ZE_DAN.get());
        output.accept(FarmingItems.XIAO_HUAN_DAN.get());
        output.accept(FarmingItems.JU_QI_SAN.get());
        output.accept(FarmingItems.CUI_TI_DAN.get());
        output.accept(FarmingItems.JI_FENG_DAN.get());
        output.accept(FarmingItems.TIE_GU_DAN.get());
        output.accept(FarmingItems.BI_DU_DAN.get());
        output.accept(FarmingItems.PO_HUAN_DAN.get());
        output.accept(FarmingItems.GUI_XI_DAN.get());
        output.accept(FarmingItems.BI_HUO_DAN.get());
        output.accept(FarmingItems.YE_SHI_DAN.get());
        output.accept(FarmingItems.BAO_SHI_DAN.get());
        output.accept(FarmingItems.QING_SHEN_DAN.get());
        output.accept(FarmingItems.YIN_XI_DAN.get());
        output.accept(FarmingItems.KUANG_BAO_DAN.get());
        output.accept(FarmingItems.NING_SHEN_DAN.get());
        output.accept(FarmingItems.SHOU_LIANG_WAN.get());
        output.accept(FarmingItems.LING_ZHI_YE.get());
        output.accept(FarmingItems.BI_GU_DAN.get());
        output.accept(FarmingItems.QU_SHOU_SAN.get());
        output.accept(FarmingItems.XUN_MAI_DAN.get());
        output.accept(FarmingItems.SHENG_SI_ZAO_HUA_DAN.get());
        output.accept(FarmingItems.QIANG_ZHI_PO_JING_DAN.get());
        output.accept(FarmingItems.XI_SUI_FA_GU_DAN.get());
        output.accept(FarmingItems.NI_SHI_DAN.get());
        output.accept(FarmingItems.YIN_ZAI_DAN.get());
        output.accept(FarmingItems.SAN_GONG_DAN.get());
        output.accept(FarmingItems.NU_SHOU_YIN.get());
        output.accept(FarmingItems.DUO_TIAN_DAN.get());
        output.accept(FarmingItems.WU_DAO_CHA.get());
        output.accept(FarmingItems.SU_TI_NI.get());
    }

    private static void addPlantItems(CreativeModeTab.Output output) {
        output.accept(FarmingItems.QING_YA_GRASS_ITEM.get());
        output.accept(FarmingItems.NING_XUE_GEN_ITEM.get());
        output.accept(FarmingItems.JU_YUAN_FLOWER_ITEM.get());
        output.accept(FarmingItems.XI_SUI_VINE_ITEM.get());
        output.accept(FarmingItems.TIE_PI_BAMBOO_ITEM.get());
        output.accept(FarmingItems.HUO_LING_ZHI_MUSHROOM_ITEM.get());
        output.accept(FarmingItems.BING_XIN_GRASS_ITEM.get());
        output.accept(FarmingItems.HUAN_DU_MUSHROOM_ITEM.get());
        output.accept(FarmingItems.YING_TAI_LICHEN_ITEM.get());
        output.accept(FarmingItems.CI_VINE_ITEM.get());
        output.accept(FarmingItems.JIAN_YE_GRASS_ITEM.get());
        output.accept(FarmingItems.CHEN_SHUI_LILY_PAD_ITEM.get());
        output.accept(FarmingItems.DI_LONG_BERRY_BUSH_ITEM.get());
        output.accept(FarmingItems.FENG_XIN_ZI_ITEM.get());
        output.accept(FarmingItems.LEI_GU_SAPLING_ITEM.get());
        output.accept(FarmingItems.SHI_YIN_GRASS_ITEM.get());
        output.accept(FarmingItems.CHUN_YANG_FLOWER_ITEM.get());
        output.accept(FarmingItems.YAN_SHOU_COCOA_ITEM.get());
        output.accept(FarmingItems.WANG_YOU_GRASS_ITEM.get());
        output.accept(FarmingItems.SHE_YAN_MELON_STEM_ITEM.get());
        output.accept(FarmingItems.LIGHTNING_ATTRACTING_FERN_ITEM.get());
        output.accept(FarmingItems.MAN_EATING_SPORE_BLOSSOM_ITEM.get());
        output.accept(FarmingItems.SPIRIT_GATHERING_TREE_ITEM.get());
        output.accept(FarmingItems.CAVE_VINES_ITEM.get());
    }

    private static void addCreatureItems(CreativeModeTab.Output output) {
        output.accept(XianqiaoItems.CLUSTER_NPC_SPAWN_EGG.get());
        output.accept(XianqiaoItems.XIAN_COW_SPAWN_EGG.get());
        output.accept(XianqiaoItems.XIAN_CHICKEN_SPAWN_EGG.get());
        output.accept(XianqiaoItems.XIAN_SHEEP_SPAWN_EGG.get());
    }

    private static void addMaterialItems(CreativeModeTab.Output output) {
        output.accept(XianqiaoItems.LING_TIE_XIE.get());
        output.accept(XianqiaoItems.CHI_TONG_SHA.get());
        output.accept(XianqiaoItems.XUAN_MEI_JING.get());
        output.accept(XianqiaoItems.CHI_XIAO_FEN.get());
        output.accept(XianqiaoItems.YUE_YIN_PIAN.get());
        output.accept(XianqiaoItems.HAN_SHUANG_YAN.get());
        output.accept(XianqiaoItems.YAN_SUI_ZHA.get());
        output.accept(XianqiaoItems.FENG_WEN_YU_PIAN.get());
        output.accept(XianqiaoItems.DI_MAI_SHA.get());
        output.accept(XianqiaoItems.YUN_MU_PIAN.get());
        output.accept(XianqiaoItems.QING_MU_XIN.get());
        output.accept(XianqiaoItems.YOU_YING_MO.get());
        output.accept(XianqiaoItems.XING_HUI_CHEN.get());
        output.accept(XianqiaoItems.YU_SUI_TUAN.get());
        output.accept(XianqiaoItems.LING_LIU_KUAI.get());
        output.accept(XianqiaoItems.XUAN_BING_JING.get());
        output.accept(XianqiaoItems.GUI_YUAN_LI.get());
        output.accept(XianqiaoItems.ZHEN_QIAO_XUAN_TIE_HE.get());
        output.accept(XianqiaoItems.NI_MAI_XING_YUN_HE.get());
        output.accept(XianqiaoItems.TIAN_LEI_CI_MU.get());
        output.accept(XianqiaoItems.KONG_SHI_HEI_JING.get());
        output.accept(XianqiaoItems.JIU_ZHUAN_SUI_JING.get());
        output.accept(XianqiaoItems.DI_MAI_LONG_JING.get());
        output.accept(XianqiaoItems.WAN_XIANG_JIN_SHA.get());
        output.accept(XianqiaoItems.SHI_SHA_LIU_LI.get());
        output.accept(XianqiaoItems.YOU_HUN_NING_PO_SHI.get());
        output.accept(XianqiaoItems.DAO_YUAN_MU_KUANG.get());
        output.accept(FarmingItems.LEI_YING_SHA.get());
        output.accept(FarmingItems.XUE_PO_LI.get());
        output.accept(FarmingItems.JIN_SUI_XIE.get());
    }

    private XianqiaoCreativeTab() {
    }

    /**
     * 注册仙窍创意模式标签页。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
