package com.Kizunad.customNPCs;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.ModEntities;
import com.Kizunad.customNPCs.events.NpcMindEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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
        // 注册自定义实体
        ModEntities.register(modEventBus);

        // 尝试注册测试内容（在独立发布 jar 中缺失时自动跳过）
        registerOptionalTestContent(modEventBus);

        // 注册事件监听器
        NeoForge.EVENT_BUS.register(new NpcMindEvents());
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
