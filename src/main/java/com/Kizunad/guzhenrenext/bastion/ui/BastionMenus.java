package com.Kizunad.guzhenrenext.bastion.ui;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 地灵管理界面菜单注册。
 * <p>
 * 该注册类仅负责 SpiritManagementMenu 的 MenuType 声明与挂载，
 * 以便服务端/客户端能够通过统一类型完成容器同步与界面打开。
 * </p>
 */
public final class BastionMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, GuzhenrenExt.MODID);

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<SpiritManagementMenu>
    > SPIRIT_MANAGEMENT_MENU = MENUS.register(
        "spirit_management",
        () -> {
            IContainerFactory<SpiritManagementMenu> factory =
                (containerId, inv, data) -> new SpiritManagementMenu(containerId, inv);
            return IMenuTypeExtension.create(factory);
        }
    );

    private BastionMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
