package com.Kizunad.guzhenrenext.xianqiao.block;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceComponentBlock;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlock;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍方块与对应物品注册表。
 */
public final class XianqiaoBlocks {

    /**
     * 方块注册器。
     */
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GuzhenrenExt.MODID);

    /**
     * 方块物品注册器。
     */
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GuzhenrenExt.MODID);

    /**
     * 仙窍核心方块。
     */
    public static final DeferredBlock<ApertureCoreBlock> APERTURE_CORE =
        BLOCKS.register("aperture_core", (Supplier<ApertureCoreBlock>) () -> new ApertureCoreBlock());

    /**
     * 仙窍核心方块对应物品。
     */
    public static final DeferredItem<BlockItem> APERTURE_CORE_ITEM =
        ITEMS.register(
            "aperture_core",
            () -> new BlockItem(APERTURE_CORE.get(), new Item.Properties())
        );

    /**
     * 资源点核心控制器方块。
     */
    public static final DeferredBlock<ResourceControllerBlock> RESOURCE_CONTROLLER =
        BLOCKS.register("resource_controller", (Supplier<ResourceControllerBlock>) () -> new ResourceControllerBlock());

    /**
     * 时场组件方块。
     */
    public static final DeferredBlock<ResourceComponentBlock> TIME_FIELD_COMPONENT =
        BLOCKS.register("time_field_component", () -> new ResourceComponentBlock());

    public static final DeferredBlock<ResourceComponentBlock> RESOURCE_COMPONENT = TIME_FIELD_COMPONENT;

    /**
     * 资源点控制器方块物品。
     */
    public static final DeferredItem<BlockItem> RESOURCE_CONTROLLER_ITEM =
        ITEMS.register(
            "resource_controller",
            () -> new BlockItem(RESOURCE_CONTROLLER.get(), new Item.Properties())
        );

    /**
     * 时场组件方块物品。
     */
    public static final DeferredItem<BlockItem> TIME_FIELD_COMPONENT_ITEM =
        ITEMS.register(
            "time_field_component",
            () -> new BlockItem(TIME_FIELD_COMPONENT.get(), new Item.Properties())
        );

    private XianqiaoBlocks() {
    }

    /**
     * 注册仙窍方块及对应方块物品。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    /**
     * 便捷获取核心方块实例。
     *
     * @return 核心方块
     */
    public static Block apertureCoreBlock() {
        return APERTURE_CORE.get();
    }
}
