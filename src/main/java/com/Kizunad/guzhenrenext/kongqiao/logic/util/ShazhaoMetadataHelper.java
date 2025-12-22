package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.Map;

/**
 * 杀招 metadata 解析工具。
 * <p>
 * ShazhaoData 的 metadata 以字符串形式存储（与 data pack JSON 保持一致），此处提供安全的数值解析，
 * 避免各杀招实现重复写 try/catch。
 * </p>
 */
public final class ShazhaoMetadataHelper {

    private ShazhaoMetadataHelper() {}

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

    public static double getDouble(
        final ShazhaoData data,
        final String key,
        final double defaultValue
    ) {
        final String raw = getString(data, key, null);
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
        final ShazhaoData data,
        final String key,
        final int defaultValue
    ) {
        final String raw = getString(data, key, null);
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
        final ShazhaoData data,
        final String key,
        final boolean defaultValue
    ) {
        final String raw = getString(data, key, null);
        if (raw == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw);
    }

    public static String getString(
        final ShazhaoData data,
        final String key,
        final String defaultValue
    ) {
        if (data == null || key == null || key.isBlank()) {
            return defaultValue;
        }
        final Map<String, String> metadata = data.metadata();
        if (metadata == null || metadata.isEmpty()) {
            return defaultValue;
        }
        return metadata.getOrDefault(key, defaultValue);
    }
}
