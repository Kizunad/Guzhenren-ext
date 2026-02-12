package com.Kizunad.guzhenrenext.xianqiao.resource;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureCoreBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍资源点方块实体注册表。
 */
public final class XianqiaoBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GuzhenrenExt.MODID);

    /** 资源控制器方块实体。 */
    public static final DeferredHolder<
        BlockEntityType<?>,
        BlockEntityType<ResourceControllerBlockEntity>
    > RESOURCE_CONTROLLER = BLOCK_ENTITIES.register(
        "resource_controller",
        () -> BlockEntityType.Builder
            .of(ResourceControllerBlockEntity::new, XianqiaoBlocks.RESOURCE_CONTROLLER.get())
            .build(null)
    );

    /** 仙窍核心方块实体。 */
    public static final DeferredHolder<
        BlockEntityType<?>,
        BlockEntityType<ApertureCoreBlockEntity>
    > APERTURE_CORE = BLOCK_ENTITIES.register(
        "aperture_core",
        () -> BlockEntityType.Builder
            .of(ApertureCoreBlockEntity::new, XianqiaoBlocks.APERTURE_CORE.get())
            .build(null)
    );

    private XianqiaoBlockEntities() {
    }

    /**
     * 注册方块实体类型。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
