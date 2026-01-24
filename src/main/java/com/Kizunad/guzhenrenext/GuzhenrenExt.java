package com.Kizunad.guzhenrenext;

import com.Kizunad.guzhenrenext.commands.GuzhenrenDebugCommand;
import com.Kizunad.guzhenrenext.config.ClientConfig;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.guzhenrenext.network.GuzhenrenExtNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 蛊真人扩展模组主入口。
 * <p>
 * 注意：CustomNPCs 相关初始化已移除。如需启用 NPC 功能，请使用 customNpcsJar 单独构建。
 * </p>
 */
@Mod(GuzhenrenExt.MODID)
public class GuzhenrenExt {

    public static final String MODID = "guzhenrenext";

    public GuzhenrenExt(IEventBus modEventBus, ModContainer modContainer) {
        // CustomNPCs 初始化已移除
        // Registery.registerAll();
        // NpcSpawnInitializer.register();
        // NpcSecondTicker.register();
        // NpcResourceRegeneration.register();
        // NpcKongqiaoInventoryBridge.register();

        KongqiaoMenus.register(modEventBus);
        KongqiaoAttachments.register(modEventBus);
        com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntities.register(modEventBus);
        GuzhenrenExtNetworking.register(modEventBus);
        com.Kizunad.guzhenrenext.kongqiao.logic.GuModEffects.registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoModEffects.registerAll();
        com.Kizunad.guzhenrenext.registry.ModMobEffects.register(modEventBus);

        // Bastion 高转技能/特效注册
        com.Kizunad.guzhenrenext.bastion.skill.BastionHighTierEffectRegistry.registerAll();

        // Bastion system registration
        com.Kizunad.guzhenrenext.bastion.BastionBlocks.register(modEventBus);
        com.Kizunad.guzhenrenext.bastion.BastionSounds.register(modEventBus);
        com.Kizunad.guzhenrenext.bastion.BastionCreativeTab.register(modEventBus);
        com.Kizunad.guzhenrenext.bastion.BastionTicker.register();

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            runClientBootstrap(modContainer);
        }
    }

    private static void runClientBootstrap(ModContainer modContainer) {
        try {
            Class<?> bootstrap = Class.forName(
                "com.Kizunad.guzhenrenext.client.GuzhenrenExtClient"
            );
            bootstrap
                .getMethod("registerConfigScreen", ModContainer.class)
                .invoke(null, modContainer);
        } catch (ClassNotFoundException e) {
            // Dedicated Server 环境或裁剪环境下不存在客户端类，跳过即可。
        } catch (Exception e) {
            throw new RuntimeException(
                "GuzhenrenExt 客户端初始化失败，请检查客户端依赖与加载顺序。",
                e
            );
        }
    }

    private void registerCommands(RegisterCommandsEvent event) {
        GuzhenrenDebugCommand.register(event.getDispatcher());
    }

    private void onAddReloadListeners(net.neoforged.neoforge.event.AddReloadListenerEvent event) {
        event.addListener(new com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataLoader());
        event.addListener(new com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataLoader());
        event.addListener(new com.Kizunad.guzhenrenext.bastion.config.BastionTypeDataLoader());
    }
}
