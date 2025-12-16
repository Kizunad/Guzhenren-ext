package com.Kizunad.customNPCs;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.ModEntities;
import com.Kizunad.customNPCs.events.NpcMindEvents;
import com.Kizunad.customNPCs.events.MaterialValueEvents;
import com.Kizunad.customNPCs.config.ModGameRules;
import com.Kizunad.customNPCs.config.CustomNpcConfigs;
import com.Kizunad.customNPCs.config.SpawnConfigs;
import com.Kizunad.customNPCs.network.ModNetworking;
import com.Kizunad.customNPCs.menu.ModMenus;
import com.Kizunad.customNPCs.client.ClientScreens;
import com.Kizunad.customNPCs.ai.status.StatusProvidersBootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CustomNPCsMod.MODID)
public class CustomNPCsMod {

    public static final String MODID = "customnpcs";
    private static final Logger LOGGER = LoggerFactory.getLogger(
        CustomNPCsMod.class
    );

    public CustomNPCsMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 Data Attachments
        NpcMindAttachment.ATTACHMENT_TYPES.register(modEventBus);
        com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment.ATTACHMENT_TYPES.register(modEventBus);
        // 注册自定义实体
        ModEntities.register(modEventBus);
        // 注册菜单
        ModMenus.MENUS.register(modEventBus);
        // 注册网络通道
        ModNetworking.hook(modEventBus);
        // 注册自定义 gamerule
        ModGameRules.register();
        // 加载配置文件（config/customnpcs-llm.json）
        CustomNpcConfigs.load();
        // 加载生成配置文件（config/customnpcs-spawn.json），覆盖旧版 llm 配置中的生成字段
        SpawnConfigs.load();
        if (FMLEnvironment.dist == Dist.CLIENT) {
        modEventBus.addListener(ClientScreens::registerScreens);
        }

        // 注册内置状态提供者
        StatusProvidersBootstrap.registerBuiltins();
        // 数据包重载监听器：材料/任务数据
        NeoForge.EVENT_BUS.addListener(MaterialValueEvents::onAddReloadListener);
        NeoForge.EVENT_BUS.addListener(com.Kizunad.customNPCs.events.TaskDataEvents::onAddReloadListener);

        // 尝试注册测试内容（在独立发布 jar 中缺失时自动跳过）
        registerOptionalTestContent(modEventBus);

        // 注册事件监听器
        NeoForge.EVENT_BUS.register(new NpcMindEvents());
        NeoForge.EVENT_BUS.register(new com.Kizunad.customNPCs.handler.NpcSpawningHandler());
        NeoForge.EVENT_BUS.register(new com.Kizunad.customNPCs.events.TaskProgressEvents());
    }

    private void registerOptionalTestContent(IEventBus modEventBus) {
        try {
            Class<?> testRegistryClass = Class.forName(
                "com.Kizunad.customNPCs_test.TestRegistry"
            );
            testRegistryClass
                .getMethod("register", IEventBus.class)
                .invoke(null, modEventBus);
            LOGGER.info("Loaded CustomNPCs test registry for dev environment");
        } catch (ClassNotFoundException e) {
            LOGGER.debug(
                "CustomNPCs test registry not present; skipping test content"
            );
        } catch (Exception e) {
            LOGGER.warn(
                "Failed to register CustomNPCs test registry: {}",
                e.getMessage()
            );
        }
    }
}
