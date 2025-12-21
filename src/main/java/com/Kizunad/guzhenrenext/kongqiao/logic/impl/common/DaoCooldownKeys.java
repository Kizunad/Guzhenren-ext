package com.Kizunad.guzhenrenext.kongqiao.logic.impl.common;

public final class DaoCooldownKeys {

    private static final String ACTIVE_PREFIX = "GuzhenrenExtCooldown_";
    private static final String PROC_PREFIX = "GuzhenrenExtProcCooldown_";

    private DaoCooldownKeys() {}

    public static String active(final String usageId) {
        return ACTIVE_PREFIX + usageId;
    }

    public static String proc(final String usageId) {
        return PROC_PREFIX + usageId;
    }
}

