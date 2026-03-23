package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

/**
 * Plan2 四类别数据重载占位加载器。
 * <p>
 * Task4 目标是先统一入口与时序，因此该加载器当前只负责把四类别的数据路径纳入
 * 统一 reload listener 挂载链，并在重载时输出可追踪日志。
 * </p>
 */
public final class Plan2CategoryDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Plan2RegistrationEntrypoint.Category category;
    private final String dataPath;

    /**
     * 创建四类别数据加载器。
     *
     * @param category 类别
     * @param dataPath 数据目录（相对 data/<namespace>/）
     */
    public Plan2CategoryDataLoader(
        final Plan2RegistrationEntrypoint.Category category,
        final String dataPath
    ) {
        super(GSON, dataPath);
        this.category = category;
        this.dataPath = dataPath;
    }

    @Override
    protected void apply(
        final Map<ResourceLocation, JsonElement> object,
        final ResourceManager resourceManager,
        final ProfilerFiller profiler
    ) {
        LOGGER.info(
            "Plan2 数据重载完成: category={}, path={}, entries={}",
            category,
            dataPath,
            object.size()
        );
    }
}
