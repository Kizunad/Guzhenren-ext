package com.Kizunad.customNPCs.ai.interaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 物品基础价格管理：优先读取 config/customnpcs-item-values.json，缺失时使用 data 内置默认表。
 */
public class ItemValueManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ItemValueManager.class
    );
    private static final String FILE_NAME = "customnpcs-item-values.json";
    private static final Path CONFIG_PATH = Paths.get("config").resolve(
        FILE_NAME
    );
    private static final String DEFAULT_RESOURCE =
        "data/customnpcs/item_values.json";
    private static final Type MAP_TYPE = new TypeToken<
        Map<String, Integer>
    >() {}.getType();
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private static final ItemValueManager INSTANCE = new ItemValueManager();

    private final Map<Item, Integer> baseValues = new HashMap<>();

    private ItemValueManager() {
        loadValues();
    }

    public static ItemValueManager getInstance() {
        return INSTANCE;
    }

    /**
     * 读取物品基础价格（仅按物品类型，忽略附魔/耐久）。
     */
    public int getItemBaseValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return baseValues.getOrDefault(stack.getItem(), 0);
    }

    private void loadValues() {
        Map<String, Integer> raw = readConfig();
        if (raw.isEmpty()) {
            raw = readDefaultResource();
        }
        baseValues.clear();

        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id == null) {
                LOGGER.warn(
                    "[ItemValueManager] 无效物品ID: {}",
                    entry.getKey()
                );
                continue;
            }

            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null || item == Items.AIR) {
                LOGGER.warn(
                    "[ItemValueManager] 未找到物品 {}，忽略",
                    entry.getKey()
                );
                continue;
            }

            int value = Math.max(0, entry.getValue());
            baseValues.put(item, value);
        }

        if (baseValues.isEmpty()) {
            LOGGER.warn(
                "[ItemValueManager] 未找到有效价格配置，请检查 config/{} 或 data 资源",
                FILE_NAME
            );
        }
    }

    private Map<String, Integer> readConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            return Collections.emptyMap();
        }
        try (
            Reader reader = Files.newBufferedReader(
                CONFIG_PATH,
                StandardCharsets.UTF_8
            )
        ) {
            Map<String, Integer> data = GSON.fromJson(reader, MAP_TYPE);
            return (data != null) ? data : Collections.emptyMap();
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn(
                "[ItemValueManager] 读取 {} 失败，将使用内置默认表: {}",
                CONFIG_PATH,
                e.getMessage()
            );
            return Collections.emptyMap();
        }
    }

    private Map<String, Integer> readDefaultResource() {
        try (
            InputStream stream =
                ItemValueManager.class.getClassLoader().getResourceAsStream(
                    DEFAULT_RESOURCE
                )
        ) {
            if (stream == null) {
                LOGGER.error(
                    "[ItemValueManager] 未找到默认价格表资源 {}",
                    DEFAULT_RESOURCE
                );
                return Collections.emptyMap();
            }
            try (
                Reader reader = new InputStreamReader(
                    stream,
                    StandardCharsets.UTF_8
                )
            ) {
                Map<String, Integer> data = GSON.fromJson(reader, MAP_TYPE);
                return (data != null) ? data : Collections.emptyMap();
            }
        } catch (IOException e) {
            LOGGER.error(
                "[ItemValueManager] 读取默认价格表失败: {}",
                e.getMessage()
            );
            return Collections.emptyMap();
        }
    }
}
