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
                .displayItems((parameters, output) -> {
                    output.accept(XianqiaoBlocks.APERTURE_CORE_ITEM.get());
                    output.accept(XianqiaoBlocks.RESOURCE_CONTROLLER_ITEM.get());
                    output.accept(XianqiaoBlocks.TIME_FIELD_COMPONENT_ITEM.get());
                    output.accept(FarmingItems.ALCHEMY_FURNACE_ITEM.get());
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
                    output.accept(XianqiaoItems.HEAVENLY_FRAGMENT.get());
                    output.accept(XianqiaoItems.STORAGE_GU.get());
                    output.accept(XianqiaoItems.TRANSFER_GU.get());
                    output.accept(XianqiaoItems.CLUSTER_NPC_SPAWN_EGG.get());
                    output.accept(XianqiaoItems.XIAN_COW_SPAWN_EGG.get());
                    output.accept(XianqiaoItems.XIAN_CHICKEN_SPAWN_EGG.get());
                    output.accept(XianqiaoItems.XIAN_SHEEP_SPAWN_EGG.get());
                })
                .build()
        );

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
