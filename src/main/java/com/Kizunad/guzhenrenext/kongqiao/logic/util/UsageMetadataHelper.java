package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * NianTou 玩法配置元数据解析工具。
 * <p>
 * 将 JSON 中的 metadata (字符串 Map) 按需转换为数值，并提供稳定 UUID 生成。
 * </p>
 */
public final class UsageMetadataHelper {

    private UsageMetadataHelper() {}

    public static double getDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        final String raw = getString(usage, key, null);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public static int getInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        final String raw = getString(usage, key, null);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(
        final NianTouData.Usage usage,
        final String key,
        final boolean defaultValue
    ) {
        final String raw = getString(usage, key, null);
        if (raw == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw);
    }

    public static String getString(
        final NianTouData.Usage usage,
        final String key,
        final String defaultValue
    ) {
        if (usage == null) {
            return defaultValue;
        }
        final Map<String, String> meta = usage.metadata();
        if (meta == null || meta.isEmpty() || key == null || key.isBlank()) {
            return defaultValue;
        }
        return meta.getOrDefault(key, defaultValue);
    }

    public static double clamp(
        final double value,
        final double min,
        final double max
    ) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * 为 AttributeModifier / 内部标识生成稳定 UUID。
     * <p>
     * 对相同 usageId 与 salt，始终生成相同 UUID，避免重复叠加与无法移除。
     * </p>
     */
    public static UUID stableUuid(final String usageId, final String salt) {
        final String actualUsageId = usageId == null ? "" : usageId;
        final String actualSalt = salt == null ? "" : salt;
        final String seed = actualUsageId + "|" + actualSalt;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}

