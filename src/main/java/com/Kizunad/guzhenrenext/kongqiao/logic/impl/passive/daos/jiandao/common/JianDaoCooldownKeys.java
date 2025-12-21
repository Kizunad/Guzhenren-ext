package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

public final class JianDaoCooldownKeys {

    private static final String ACTIVE_PREFIX = "GuzhenrenExtCooldown_";
    private static final String PROC_PREFIX = "GuzhenrenExtProcCooldown_";

    private JianDaoCooldownKeys() {}

    public static String active(final String usageId) {
        return ACTIVE_PREFIX + usageId;
    }

    public static String proc(final String usageId) {
        return PROC_PREFIX + usageId;
    }
}

