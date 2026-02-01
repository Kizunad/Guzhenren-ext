package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地系统创意模式标签页注册。
 * <p>
 * 将基地相关物品添加到创意模式物品栏。
 * </p>
 */
public final class BastionCreativeTab {

    private BastionCreativeTab() {
        // 工具类
    }

    /** 创意模式标签页注册表。 */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GuzhenrenExt.MODID);

    /**
     * 蛊真人扩展标签页 - 包含基地系统物品。
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GUZHENREN_EXT_TAB =
        CREATIVE_MODE_TABS.register(
            "guzhenrenext",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.guzhenrenext"))
                .icon(() -> new ItemStack(BastionBlocks.BASTION_CORE_ITEM.get()))
                .displayItems((parameters, output) -> {
                    // 基地系统物品
                    output.accept(BastionBlocks.BASTION_CORE_ITEM.get());
                    output.accept(BastionBlocks.BASTION_NODE_ITEM.get());
                    output.accept(BastionBlocks.BASTION_REVERSAL_ARRAY_ITEM.get());
                    output.accept(BastionBlocks.BASTION_SCOUT_ITEM.get());
                })
                .build()
        );

    /**
     * 注册创意模式标签页。
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
