package com.Kizunad.guzhenrenext.bastion.menu;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地系统菜单注册表。
 */
public final class BastionMenus {

    private BastionMenus() {
    }

    private static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, GuzhenrenExt.MODID);

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<BastionManagementMenu>
    > BASTION_MANAGEMENT = MENUS.register("bastion_management", () ->
        IMenuTypeExtension.create(BastionManagementMenu::fromNetwork)
    );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
