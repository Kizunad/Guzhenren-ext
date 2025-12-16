package com.Kizunad.customNPCs.config;

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
 * 生成相关配置加载器：
 * <p>在 config/customnpcs-spawn.json 生成/读取配置，并应用到 SpawnConfig。</p>
 * <p>不依赖 NeoForge ConfigSpec，保持与项目现有 json 配置风格一致。</p>
 */
public final class SpawnConfigs {

    private static final Logger LOGGER = LoggerFactory.getLogger("CustomNPCsSpawnConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "customnpcs-spawn.json";

    private SpawnConfigs() {}

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
        SpawnConfig.getInstance()
            .setNaturalSpawnEnabled(data.isNaturalSpawnEnabled())
            .setMaxNaturalSpawns(data.getMaxNaturalSpawns());
    }

    /**
     * 配置数据结构。缺少字段时 Gson 会使用默认值。
     */
    private static final class ConfigData {

        private static final boolean DEFAULT_NATURAL_SPAWN_ENABLED = true;
        private static final int DEFAULT_MAX_NATURAL_SPAWNS =
            SpawnConfig.DEFAULT_MAX_NATURAL_SPAWNS;

        private boolean naturalSpawnEnabled = DEFAULT_NATURAL_SPAWN_ENABLED;
        private int maxNaturalSpawns = DEFAULT_MAX_NATURAL_SPAWNS;

        public boolean isNaturalSpawnEnabled() {
            return naturalSpawnEnabled;
        }

        public int getMaxNaturalSpawns() {
            return maxNaturalSpawns;
        }
    }
}

