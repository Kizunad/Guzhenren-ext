package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

/**
 * 杀招数据加载器。
 * <p>
 * 负责从数据包 (data/guzhenrenext/shazhao/*.json) 加载 ShazhaoData，
 * 并填充到 ShazhaoDataManager 中。
 * </p>
 */
public class ShazhaoDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON =
        new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public ShazhaoDataLoader() {
        super(GSON, "shazhao"); // 对应 data/<namespace>/shazhao 目录
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> object,
        ResourceManager resourceManager,
        ProfilerFiller profiler
    ) {
        ShazhaoDataManager.clear();
        LOGGER.info("开始加载杀招数据 (ShazhaoData)...");

        int count = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                ShazhaoData data = ShazhaoData.CODEC.parse(
                    JsonOps.INSTANCE,
                    json
                ).getOrThrow(error ->
                    new IllegalStateException(
                        "Error parsing ShazhaoData: " + error
                    )
                );

                List<String> errors = ShazhaoDataValidator.validate(data);
                if (!errors.isEmpty()) {
                    LOGGER.error(
                        "杀招数据命名规范校验失败，将跳过加载: {} | errors={}",
                        location,
                        String.join(" | ", errors)
                    );
                    continue;
                }

                ShazhaoDataManager.register(data);
                count++;
            } catch (Exception e) {
                LOGGER.error("无法加载杀招数据: {}", location, e);
            }
        }

        LOGGER.info("成功加载 {} 个杀招数据配置。", count);
    }
}
