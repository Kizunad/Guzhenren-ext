package com.Kizunad.guzhenrenext;

import com.Kizunad.guzhenrenext.client.gui.GuzhenrenConfigScreen;
import com.Kizunad.guzhenrenext.commands.GuzhenrenDebugCommand;
import com.Kizunad.guzhenrenext.config.ClientConfig;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Registery;
import com.Kizunad.guzhenrenext.customNPCImpl.lifecycle.NpcResourceRegeneration;
import com.Kizunad.guzhenrenext.customNPCImpl.lifecycle.NpcSecondTicker;
import com.Kizunad.guzhenrenext.customNPCImpl.lifecycle.NpcSpawnInitializer;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.guzhenrenext.network.GuzhenrenExtNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(GuzhenrenExt.MODID)
public class GuzhenrenExt {

    public static final String MODID = "guzhenrenext";

    public GuzhenrenExt(IEventBus modEventBus, ModContainer modContainer) {
        Registery.registerAll();
        NpcSpawnInitializer.register();
        NpcSecondTicker.register();
        NpcResourceRegeneration.register();
        KongqiaoMenus.register(modEventBus);
        KongqiaoAttachments.register(modEventBus);
        GuzhenrenExtNetworking.register(modEventBus);
        com.Kizunad.guzhenrenext.kongqiao.logic.GuModEffects.registerAll();
        com.Kizunad.guzhenrenext.registry.ModMobEffects.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, 
                (minecraft, screen) -> new GuzhenrenConfigScreen(screen));
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        GuzhenrenDebugCommand.register(event.getDispatcher());
    }

    private void onAddReloadListeners(net.neoforged.neoforge.event.AddReloadListenerEvent event) {
        event.addListener(new com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataLoader());
    }
}
