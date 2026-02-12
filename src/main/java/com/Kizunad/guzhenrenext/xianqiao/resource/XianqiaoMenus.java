package com.Kizunad.guzhenrenext.xianqiao.resource;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritMenu;
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

    /** 仙窍中枢菜单。 */
    public static final DeferredHolder<
        MenuType<?>,
        MenuType<ApertureHubMenu>
    > APERTURE_HUB = MENUS.register(
        "aperture_hub",
        () -> IMenuTypeExtension.create(ApertureHubMenu::fromNetwork)
    );

    /** 地灵管理菜单。 */
    public static final DeferredHolder<
        MenuType<?>,
        MenuType<LandSpiritMenu>
    > LAND_SPIRIT = MENUS.register(
        "land_spirit",
        () -> IMenuTypeExtension.create(
            (containerId, inventory, ignoredBuffer) ->
                new LandSpiritMenu(containerId, inventory)
        )
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
