package com.Kizunad.guzhenrenext.kongqiao.niantou;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

/**
 * 念头数据加载器。
 * <p>
 * 负责从数据包 (data/guzhenrenext/niantou/*.json) 加载 NianTouData，
 * 并填充到 NianTouDataManager 中。
 * </p>
 */
public class NianTouDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public NianTouDataLoader() {
        super(GSON, "niantou"); // 对应 data/<namespace>/niantou 目录
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> object,
        ResourceManager resourceManager,
        ProfilerFiller profiler
    ) {
        // 清空旧数据
        NianTouDataManager.clear();
        LOGGER.info("开始加载念头数据 (NianTouData)...");

        int count = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                // 使用 Codec 解析 JSON
                NianTouData data = NianTouData.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new IllegalStateException("Error parsing NianTouData: " + error));

                // 注册到管理器
                NianTouDataManager.register(data);
                count++;
            } catch (Exception e) {
                LOGGER.error("无法加载念头数据: {}", location, e);
            }
        }

        LOGGER.info("成功加载 {} 个念头数据配置。", count);
    }
}
