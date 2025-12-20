package com.Kizunad.guzhenrenext.guzhenrenBridge;

import java.lang.reflect.Field;
import net.guzhenren.network.GuzhenrenModVariables;

/**
 * 蛊真人 PlayerVariables 同步兼容工具。
 * <p>
 * 部分玩家使用的蛊真人本体版本可能缺少 {@code markSyncDirty()} 方法，
 * 直接调用会触发 {@link NoSuchMethodError} 并导致服务器崩溃。
 * </p>
 * <p>
 * 本工具会优先调用 {@code markSyncDirty()}；若不存在则回退为反射写入
 * {@code _syncDirty}/{@code syncDirty} 字段，以触发原模组的 Tick 同步逻辑。
 * </p>
 */
public final class PlayerVariablesSyncHelper {

    private static final String[] DIRTY_FIELD_CANDIDATES = {
        "_syncDirty",
        "syncDirty",
    };

    private PlayerVariablesSyncHelper() {}

    /**
     * 标记变量需要同步（兼容不同蛊真人版本）。
     *
     * @param variables 蛊真人玩家变量
     */
    public static void markSyncDirty(
        GuzhenrenModVariables.PlayerVariables variables
    ) {
        if (variables == null) {
            return;
        }

        try {
            variables.markSyncDirty();
            return;
        } catch (NoSuchMethodError ignored) {
            // 旧版本无该方法，走字段回退
        }

        for (String candidate : DIRTY_FIELD_CANDIDATES) {
            if (tryMarkDirtyField(variables, candidate)) {
                return;
            }
        }
    }

    private static boolean tryMarkDirtyField(
        GuzhenrenModVariables.PlayerVariables variables,
        String fieldName
    ) {
        try {
            Field field = variables.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(variables, true);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
