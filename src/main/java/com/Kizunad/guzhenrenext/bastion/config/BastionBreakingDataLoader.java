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
 * 基地破坏时间配置加载器。
 * <p>
 * 从数据包 data/guzhenrenext/bastion_breaking/*.json 加载 BastionBreakingConfigData。
 * </p>
 */
public class BastionBreakingDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionBreakingDataLoader.class);
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    public BastionBreakingDataLoader() {
        super(GSON, "bastion_breaking");
        BastionBreakingConfig.clear();
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> entries,
        ResourceManager resourceManager,
        ProfilerFiller profiler
    ) {
        // 只读取 default.json（允许后续扩展多套配置）
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(
            com.Kizunad.guzhenrenext.GuzhenrenExt.MODID,
            "default"
        );

        JsonElement json = entries.get(key);
        if (json == null) {
            LOGGER.warn("未找到 bastion_breaking/default.json，使用内置默认破坏时间配置");
            BastionBreakingConfig.clear();
            return;
        }

        try {
            BastionBreakingConfigData data = BastionBreakingConfigData.CODEC.parse(
                JsonOps.INSTANCE,
                json
            ).getOrThrow(error -> new IllegalStateException("解析 BastionBreakingConfigData 失败: " + error));
            BastionBreakingConfig.apply(data);
            LOGGER.info("已加载基地破坏时间配置: {}", key);
        } catch (Exception e) {
            LOGGER.error("无法加载基地破坏时间配置，将使用默认值: {}", key, e);
            BastionBreakingConfig.clear();
        }
    }
}
