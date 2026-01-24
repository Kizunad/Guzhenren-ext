package com.Kizunad.guzhenrenext.bastion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地类型配置加载器。
 * <p>
 * 从数据包 data/guzhenrenext/bastion_type/*.json 加载 BastionTypeConfig，
 * 并注册到 BastionTypeManager。
 * </p>
 */
public class BastionTypeDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionTypeDataLoader.class);
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    public BastionTypeDataLoader() {
        super(GSON, "bastion_type"); // 对应 data/<namespace>/bastion_type 目录
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> entries,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        BastionTypeManager.clear();
        LOGGER.info("开始加载基地类型配置 (BastionTypeConfig)...");

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                BastionTypeConfig config = BastionTypeConfig.CODEC.parse(
                    JsonOps.INSTANCE,
                    json
                ).getOrThrow(error ->
                    new IllegalStateException("解析 BastionTypeConfig 失败: " + error)
                );

                // 验证配置 ID 与文件名一致
                String expectedId = location.getPath();
                if (!config.id().equals(expectedId)) {
                    LOGGER.warn(
                        "配置 ID 与文件名不匹配: 文件={}, 配置ID={}，将使用配置中的 ID",
                        expectedId, config.id()
                    );
                }

                BastionTypeManager.register(config);
                successCount++;
            } catch (Exception e) {
                LOGGER.error("无法加载基地类型配置: {}", location, e);
                failCount++;
            }
        }

        LOGGER.info(
            "基地类型配置加载完成: 成功={}, 失败={}",
            successCount, failCount
        );
    }
}
