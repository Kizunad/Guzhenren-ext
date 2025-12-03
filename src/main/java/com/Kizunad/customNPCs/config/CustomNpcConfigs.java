package com.Kizunad.customNPCs.config;

import com.Kizunad.customNPCs.ai.llm.LlmConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简易配置加载器：在 config/customnpcs-llm.json 生成/读取配置，并应用到 LlmConfig。
 * 不依赖 Forge/NeoForge ConfigSpec，避免新增依赖。
 */
public final class CustomNpcConfigs {

    private static final Logger LOGGER = LoggerFactory.getLogger("CustomNPCsConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "customnpcs-llm.json";

    private CustomNpcConfigs() {}

    public static void load() {
        Path path = Paths.get("config").resolve(FILE_NAME);
        ensureDir(path.getParent());
        ConfigData data = readOrDefault(path);
        apply(data);
    }

    private static void ensureDir(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOGGER.warn("Failed to create config directory: {}", e.getMessage());
        }
    }

    private static ConfigData readOrDefault(Path path) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    return data;
                }
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.warn("Failed to read {}, using defaults: {}", path, e.getMessage());
            }
        }
        ConfigData defaults = new ConfigData();
        write(path, defaults);
        return defaults;
    }

    private static void write(Path path, ConfigData data) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.warn("Failed to write {}: {}", path, e.getMessage());
        }
    }

    private static void apply(ConfigData data) {
        LlmConfig cfg = LlmConfig.getInstance();
        cfg
            .setEnabled(data.enable)
            .setRequestIntervalTicks(data.requestIntervalTicks)
            .setPlanTtlTicks(data.planTtlTicks)
            .setModel(data.model)
            .setTemperature(data.temperature)
            .setMaxTokens(data.maxTokens)
            .setHttpTimeoutMs(data.httpTimeoutMs)
            .setLogRequest(data.logRequest)
            .setLogResponse(data.logResponse)
            .setApiKey(data.apiKey)
            .setApiReferer(data.apiReferer)
            .setApiTitle(data.apiTitle);

        SpawnConfig.getInstance()
            .setNaturalSpawnEnabled(data.naturalSpawnEnabled)
            .setMaxNaturalSpawns(data.maxNaturalSpawns);
    }

    /**
     * 配置数据结构。若缺少字段，Gson 会使用默认值。
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    private static final class ConfigData {
        private static final int DEFAULT_INTERVAL = 200;
        private static final int DEFAULT_PLAN_TTL = 400;
        private static final double DEFAULT_TEMPERATURE = 0.4d;
        private static final int DEFAULT_MAX_TOKENS = 512;
        private static final int DEFAULT_HTTP_TIMEOUT_MS = 120_000;

        public boolean enable = false;
        public int requestIntervalTicks = DEFAULT_INTERVAL;
        public int planTtlTicks = DEFAULT_PLAN_TTL;
        public String model = "openrouter/auto";
        public double temperature = DEFAULT_TEMPERATURE;
        public int maxTokens = DEFAULT_MAX_TOKENS;
        public int httpTimeoutMs = DEFAULT_HTTP_TIMEOUT_MS;
        public boolean logRequest = true;
        public boolean logResponse = true;
        public String apiKey = "";
        public String apiReferer = "";
        public String apiTitle = "";

        // 生成配置
        public boolean naturalSpawnEnabled = true;
        public int maxNaturalSpawns = SpawnConfig.DEFAULT_MAX_NATURAL_SPAWNS;
    }
}
