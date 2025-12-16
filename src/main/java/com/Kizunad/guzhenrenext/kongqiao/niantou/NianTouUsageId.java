package com.Kizunad.guzhenrenext.kongqiao.niantou;

import javax.annotation.Nullable;

/**
 * 念头/蛊虫用途（Usage）命名工具。
 * <p>
 * 当前项目中，{@link NianTouData.Usage#usageID()} 同时承担“唯一 ID”与“类型标记”的职责：
 * <ul>
 *   <li>主动用途：usageID 路径中包含 {@code _active_}</li>
 *   <li>被动用途：usageID 路径中包含 {@code _passive_}</li>
 * </ul>
 * 该约定用于 UI（轮盘/按钮）筛选“可主动触发”的技能列表，避免与被动效果混淆。
 * </p>
 */
public final class NianTouUsageId {

    /**
     * 主动用途类型标记。
     */
    public static final String NAME_TYPE_ACTIVE = "active";

    private static final String TOKEN_ACTIVE = "_" + NAME_TYPE_ACTIVE + "_";
    private static final String TOKEN_PASSIVE = "_passive_";
    private static final char NAMESPACE_SEPARATOR = ':';

    private NianTouUsageId() {}

    /**
     * 从 usageID 中提取类型标记："" 或 "active"。
     *
     * @param usageId 用途 ID（形如 {@code guzhenren:langhungu_active_xxx}）
     * @return 空串表示被动用途；返回 {@link #NAME_TYPE_ACTIVE} 表示主动用途
     */
    public static String extractNameType(@Nullable final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return "";
        }
        return isActive(usageId) ? NAME_TYPE_ACTIVE : "";
    }

    /**
     * 判定指定 usageID 是否为主动用途（可由轮盘触发）。
     *
     * @param usageId 用途 ID
     * @return true 表示主动用途
     */
    public static boolean isActive(@Nullable final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return false;
        }
        return resolvePath(usageId).contains(TOKEN_ACTIVE);
    }

    /**
     * 判定指定 usageID 是否为被动用途。
     *
     * @param usageId 用途 ID
     * @return true 表示被动用途
     */
    public static boolean isPassive(@Nullable final String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return false;
        }
        return resolvePath(usageId).contains(TOKEN_PASSIVE);
    }

    /**
     * 兼容旧命名：主动用途曾使用 {@code _skill_} 标记。
     * <p>
     * 新规范已强制使用 {@code _active_}，因此该方法仅做语义别名，避免散落的调用点产生歧义。
     * </p>
     *
     * @param usageId 用途 ID
     * @return true 表示主动用途
     */
    public static boolean isSkill(@Nullable final String usageId) {
        return isActive(usageId);
    }

    private static String resolvePath(final String usageId) {
        final int separatorIndex = usageId.indexOf(NAMESPACE_SEPARATOR);
        if (separatorIndex < 0 || separatorIndex + 1 >= usageId.length()) {
            return usageId;
        }
        return usageId.substring(separatorIndex + 1);
    }
}
