package com.Kizunad.customNPCs.client;

import com.Kizunad.customNPCs.client.ui.NpcInventoryScreen;
import com.Kizunad.customNPCs.menu.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientScreens {

    private ClientScreens() {}

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.NPC_INVENTORY.get(), NpcInventoryScreen::new);
    }
}
