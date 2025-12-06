package com.Kizunad.customNPCs.ai.interaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 材料点数配置：优先读取 config/customnpcs-material-values.json，不存在时使用 data 内置默认表。
 */
public class MaterialValueManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        MaterialValueManager.class
    );
    private static final String FILE_NAME = "customnpcs-material-values.json";
    private static final Path CONFIG_PATH = Paths.get("config").resolve(
        FILE_NAME
    );
    private static final String DEFAULT_RESOURCE = "data/customnpcs/material_values.json";
    private static final Type MAP_TYPE = new TypeToken<Map<String, Float>>() {}
        .getType();
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private static final MaterialValueManager INSTANCE =
        new MaterialValueManager();

    private final Map<Item, Float> values = new HashMap<>();

    private MaterialValueManager() {
        reload(Collections.emptyMap());
    }

    public static MaterialValueManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取物品对应的材料点数。
     */
    public float getMaterialValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }
        return values.getOrDefault(stack.getItem(), 0.0F);
    }

    /**
     * 按物品 ID 排序后的材料配置列表，供客户端 UI 构建选项。
     */
    public List<MaterialValueEntry> getEntriesSorted() {
        return values
            .entrySet()
            .stream()
            .sorted(
                Comparator.comparing(entry ->
                    BuiltInRegistries.ITEM.getKey(entry.getKey()).toString()
                )
            )
            .map(entry -> new MaterialValueEntry(entry.getKey(), entry.getValue()))
            .toList();
    }

    /**
     * 使用传入的数据重新加载材料表，优先采用数据包/配置内容，缺省时回退到内置默认表。
     * @param dataFromResources 数据包收集到的原始键值表（可以为空）
     */
    public synchronized void reload(Map<String, Float> dataFromResources) {
        Map<String, Float> merged = new HashMap<>();
        if (dataFromResources != null && !dataFromResources.isEmpty()) {
            merged.putAll(dataFromResources);
        }
        Map<String, Float> configValues = readConfig();
        if (!configValues.isEmpty()) {
            // 配置文件优先级最高，用于覆盖数据包配置
            merged.putAll(configValues);
        }
        if (merged.isEmpty()) {
            merged = readDefaultResource();
        }
        applyRawValues(merged);
    }

    /**
     * 将原始字符串键值表转换成游戏内物品映射。
     */
    private void applyRawValues(Map<String, Float> raw) {
        values.clear();
        for (Map.Entry<String, Float> entry : raw.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id == null) {
                LOGGER.warn(
                    "[MaterialValueManager] 无效物品ID: {}",
                    entry.getKey()
                );
                continue;
            }

            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null || item == Items.AIR) {
                LOGGER.warn(
                    "[MaterialValueManager] 未找到物品 {}，忽略",
                    entry.getKey()
                );
                continue;
            }

            float value = Math.max(0.0F, entry.getValue());
            values.put(item, value);
        }

        if (values.isEmpty()) {
            LOGGER.warn(
                "[MaterialValueManager] 未找到有效材料配置，请检查 config/{} 或 data 资源",
                FILE_NAME
            );
        } else {
            LOGGER.info(
                "[MaterialValueManager] 材料表加载完成，记录条目数: {}",
                values.size()
            );
        }
    }

    private Map<String, Float> readConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            return Collections.emptyMap();
        }
        try (Reader reader = Files.newBufferedReader(
                    CONFIG_PATH,
                    StandardCharsets.UTF_8
                )) {
            Map<String, Float> data = GSON.fromJson(reader, MAP_TYPE);
            return (data != null) ? data : Collections.emptyMap();
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn(
                "[MaterialValueManager] 读取 {} 失败，将使用内置默认表: {}",
                CONFIG_PATH,
                e.getMessage()
            );
            return Collections.emptyMap();
        }
    }

    private Map<String, Float> readDefaultResource() {
        try (InputStream stream = MaterialValueManager.class
            .getClassLoader()
            .getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                LOGGER.error(
                    "[MaterialValueManager] 未找到默认材料表资源 {}",
                    DEFAULT_RESOURCE
                );
                return Collections.emptyMap();
            }
            try (Reader reader = new InputStreamReader(
                        stream,
                        StandardCharsets.UTF_8
                    )) {
                Map<String, Float> data = GSON.fromJson(reader, MAP_TYPE);
                return (data != null) ? data : Collections.emptyMap();
            }
        } catch (IOException e) {
            LOGGER.error(
                "[MaterialValueManager] 读取默认材料表失败: {}",
                e.getMessage()
            );
            return Collections.emptyMap();
        }
    }

    /**
     * 解析来自数据包的 JsonElement，转换为字符串键值表。
     * @param element 原始 json
     * @return 物品 ID -> 材料值 映射（无效条目自动过滤）
     */
    public Map<String, Float> parseJsonElement(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Float> data = GSON.fromJson(element, MAP_TYPE);
            return data != null ? data : Collections.emptyMap();
        } catch (JsonSyntaxException ex) {
            LOGGER.warn(
                "[MaterialValueManager] 解析数据包材料表失败: {}",
                ex.getMessage()
            );
            return Collections.emptyMap();
        }
    }

    /**
     * 材料配置行。
     * @param item 物品
     * @param value 单位材料值
     */
    public record MaterialValueEntry(Item item, float value) {}
}
