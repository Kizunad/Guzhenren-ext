package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

final class NianTouDataMetadataBool {

    private NianTouDataMetadataBool() {}

    static boolean get(
        final NianTouData.Usage usageInfo,
        final String key,
        final boolean defaultValue
    ) {
        if (usageInfo == null || key == null || key.isBlank()) {
            return defaultValue;
        }
        final String raw = usageInfo.metadata().get(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        final double num = UsageMetadataHelper.getDouble(usageInfo, key, defaultValue ? 1.0 : 0.0);
        return num > 0.0;
    }
}

