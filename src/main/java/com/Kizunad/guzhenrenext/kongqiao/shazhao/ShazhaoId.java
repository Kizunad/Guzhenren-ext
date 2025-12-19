package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

/**
 * 杀招 ID 命名工具。
 * <p>
 * 规范：{@code shazhao_passive_xxx} / {@code shazhao_active_xxx}。
 * </p>
 */
public final class ShazhaoId {

    public static final String PREFIX = "shazhao_";
    public static final String PREFIX_PASSIVE = "shazhao_passive_";
    public static final String PREFIX_ACTIVE = "shazhao_active_";

    private static final char NAMESPACE_SEPARATOR = ':';

    private ShazhaoId() {}

    /**
     * 判断是否为被动杀招 ID。
     */
    public static boolean isPassive(@Nullable final String shazhaoId) {
        if (shazhaoId == null || shazhaoId.isBlank()) {
            return false;
        }
        return resolvePath(shazhaoId).startsWith(PREFIX_PASSIVE);
    }

    /**
     * 判断是否为主动杀招 ID。
     */
    public static boolean isActive(@Nullable final String shazhaoId) {
        if (shazhaoId == null || shazhaoId.isBlank()) {
            return false;
        }
        return resolvePath(shazhaoId).startsWith(PREFIX_ACTIVE);
    }

    /**
     * 判断 path 是否符合杀招命名规范。
     */
    public static boolean isValidPath(@Nullable final String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return path.startsWith(PREFIX_PASSIVE) || path.startsWith(PREFIX_ACTIVE);
    }

    /**
     * 迁移旧版杀招 ID（shazhao_xxx -> shazhao_passive_xxx）。
     */
    @Nullable
    public static ResourceLocation migrateLegacyId(
        @Nullable final ResourceLocation id
    ) {
        if (id == null) {
            return null;
        }
        final String path = id.getPath();
        if (path.startsWith(PREFIX_PASSIVE) || path.startsWith(PREFIX_ACTIVE)) {
            return id;
        }
        if (!path.startsWith(PREFIX)) {
            return id;
        }
        final String suffix = path.substring(PREFIX.length());
        if (suffix.isBlank()) {
            return id;
        }
        return ResourceLocation.fromNamespaceAndPath(
            id.getNamespace(),
            PREFIX_PASSIVE + suffix
        );
    }

    private static String resolvePath(final String shazhaoId) {
        final int separatorIndex = shazhaoId.indexOf(NAMESPACE_SEPARATOR);
        if (separatorIndex < 0 || separatorIndex + 1 >= shazhaoId.length()) {
            return shazhaoId;
        }
        return shazhaoId.substring(separatorIndex + 1);
    }
}
