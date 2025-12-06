package com.Kizunad.customNPCs.ai.interaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

/**
 * 数据包重载监听器：同步 material_values 表到 {@link MaterialValueManager}。
 */
public class MaterialValueReloader
    extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String FOLDER = "material_values";

    public MaterialValueReloader() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> objects,
        ResourceManager resourceManager,
        ProfilerFiller profiler
    ) {
        MaterialValueManager manager = MaterialValueManager.getInstance();
        Map<String, Float> merged = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            merged.putAll(manager.parseJsonElement(entry.getValue()));
        }
        manager.reload(merged);
    }
}
