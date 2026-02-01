package com.Kizunad.guzhenrenext.bastion.config;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地类型配置管理器 - 管理所有已加载的基地类型配置。
 * <p>
 * 此类为单例模式，配置在数据包重载时刷新。
 * </p>
 */
public final class BastionTypeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionTypeManager.class);

    private BastionTypeManager() {
        // 工具类
    }

    /** 已注册的配置映射（ID -> 配置）。 */
    private static final Map<String, BastionTypeConfig> CONFIGS = new ConcurrentHashMap<>();

    /** 默认配置 ID（当指定配置不存在时使用）。 */
    private static final String DEFAULT_CONFIG_ID = "default";

    // ===== 公开 API =====

    /**
     * 注册基地类型配置。
     *
     * @param config 配置数据
     */
    public static void register(BastionTypeConfig config) {
        CONFIGS.put(config.id(), config);
        LOGGER.debug("注册基地类型配置: {}", config.id());
    }

    /**
     * 清空所有已注册配置（数据包重载时调用）。
     */
    public static void clear() {
        CONFIGS.clear();
    }

    /**
     * 根据 ID 获取配置。
     *
     * @param configId 配置 ID
     * @return 配置，如果不存在则返回 null
     */
    @Nullable
    public static BastionTypeConfig get(String configId) {
        return CONFIGS.get(configId);
    }

    /**
     * 根据 ID 获取配置，不存在时返回默认配置。
     *
     * @param configId 配置 ID
     * @return 配置或默认配置
     */
    public static BastionTypeConfig getOrDefault(String configId) {
        BastionTypeConfig config = CONFIGS.get(configId);
        if (config != null) {
            return config;
        }

        // 尝试获取默认配置
        BastionTypeConfig defaultConfig = CONFIGS.get(DEFAULT_CONFIG_ID);
        if (defaultConfig != null) {
            return defaultConfig;
        }

        // 如果连默认配置都不存在，返回程序默认值
        LOGGER.warn("配置 {} 和默认配置都不存在，使用程序默认值", configId);
        return createFallbackConfig(configId);
    }

    /**
     * 根据道途获取适合的配置。
     *
     * @param dao 道途类型
     * @return 匹配的配置或默认配置
     */
    public static BastionTypeConfig getByDao(BastionDao dao) {
        // 优先查找精确匹配的配置
        for (BastionTypeConfig config : CONFIGS.values()) {
            if (config.primaryDao() == dao) {
                return config;
            }
        }

        // 回退到默认配置
        return getOrDefault(DEFAULT_CONFIG_ID);
    }

    /**
     * 获取所有已注册的配置。
     *
     * @return 配置集合
     */
    public static Collection<BastionTypeConfig> getAll() {
        return CONFIGS.values();
    }

    /**
     * 获取已注册配置数量。
     *
     * @return 配置数量
     */
    public static int size() {
        return CONFIGS.size();
    }

    /**
     * 检查配置是否存在。
     *
     * @param configId 配置 ID
     * @return 是否存在
     */
    public static boolean exists(String configId) {
        return CONFIGS.containsKey(configId);
    }

    // ===== 内部方法 =====

    /** 程序默认最大转数（支持高转内容）。 */
    private static final int FALLBACK_MAX_TIER = 9;

    /**
     * 创建程序默认配置（当无配置文件时使用）。
     */
    private static BastionTypeConfig createFallbackConfig(String id) {
        return new BastionTypeConfig(
            id,
            "默认基地",
            BastionDao.ZHI_DAO,
            FALLBACK_MAX_TIER,
            BastionTypeConfig.UpkeepConfig.DEFAULT,
            BastionTypeConfig.SpawningConfig.DEFAULT,
            BastionTypeConfig.ExpansionConfig.DEFAULT,
            BastionTypeConfig.ConnectivityConfig.DEFAULT,
            BastionTypeConfig.ShellConfig.DEFAULT,
            BastionTypeConfig.DecayConfig.DEFAULT,
            BastionTypeConfig.EvolutionConfig.DEFAULT,
            BastionTypeConfig.AuraConfig.DEFAULT,
            BastionTypeConfig.EnergyConfig.DEFAULT,
            BastionTypeConfig.HatcheryConfig.DEFAULT,
            BastionTypeConfig.EliteConfig.DEFAULT,
            BastionTypeConfig.DEFAULT_ANCHORS_WEIGHT,
            BastionTypeConfig.DEFAULT_MYCELIUM_WEIGHT,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }
}
