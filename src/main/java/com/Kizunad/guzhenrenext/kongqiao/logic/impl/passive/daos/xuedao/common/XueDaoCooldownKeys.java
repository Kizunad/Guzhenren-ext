package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

/**
 * 血道冷却 Key 生成工具。
 * <p>
 * 约定：
 * <ul>
 *   <li>主动技能冷却：{@code GuzhenrenExtCooldown_ + usageId}</li>
 *   <li>被动触发冷却：{@code GuzhenrenExtProcCooldown_ + usageId}</li>
 * </ul>
 * </p>
 */
public final class XueDaoCooldownKeys {

    private static final String ACTIVE_PREFIX = "GuzhenrenExtCooldown_";
    private static final String PROC_PREFIX = "GuzhenrenExtProcCooldown_";

    private XueDaoCooldownKeys() {}

    public static String active(final String usageId) {
        return ACTIVE_PREFIX + usageId;
    }

    public static String proc(final String usageId) {
        return PROC_PREFIX + usageId;
    }
}

