package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.CustomNPCsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
        Registries.MENU,
        CustomNPCsMod.MODID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<NpcInventoryMenu>> NPC_INVENTORY =
        MENUS.register(
            "npc_inventory",
            () -> IMenuTypeExtension.create(NpcInventoryMenu::fromNetwork)
        );

    public static final DeferredHolder<MenuType<?>, MenuType<NpcTradeMenu>> NPC_TRADE =
        MENUS.register(
            "npc_trade",
            () -> IMenuTypeExtension.create(NpcTradeMenu::fromNetwork)
        );
}
