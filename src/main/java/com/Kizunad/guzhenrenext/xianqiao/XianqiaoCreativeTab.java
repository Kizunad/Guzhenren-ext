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
