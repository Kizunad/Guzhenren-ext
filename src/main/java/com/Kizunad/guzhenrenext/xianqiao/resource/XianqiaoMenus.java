package com.Kizunad.guzhenrenext.xianqiao.resource;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍资源点菜单注册表。
 */
public final class XianqiaoMenus {

    private static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, GuzhenrenExt.MODID);

    /** 资源控制器菜单。 */
    public static final DeferredHolder<
        MenuType<?>,
        MenuType<ResourceControllerMenu>
    > RESOURCE_CONTROLLER = MENUS.register(
        "resource_controller",
        () -> IMenuTypeExtension.create(ResourceControllerMenu::fromNetwork)
    );

    private XianqiaoMenus() {
    }

    /**
     * 注册菜单类型。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
