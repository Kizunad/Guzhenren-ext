package com.Kizunad.guzhenrenext.guzhenrenBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

/**
 * 气运数据操作工具类。
 */
public final class QiyunHelper {

    private static final String GUZHENREN_MOD_VARIABLES_CLASS_NAME =
        "net.guzhenren.network.GuzhenrenModVariables";
    private static final String PLAYER_VARIABLES_FIELD_NAME = "PLAYER_VARIABLES";
    private static final String ENTITY_GET_DATA_METHOD_NAME = "getData";
    private static final String FIELD_QIYUN = "qiyun";
    private static final String FIELD_MAX_QIYUN = "qiyun_shangxian";
    private static final String METHOD_MARK_SYNC_DIRTY = "markSyncDirty";
    private static final String[] DIRTY_FIELD_CANDIDATES = {
        "_syncDirty",
        "syncDirty",
    };

    private QiyunHelper() {}

    public static double getAmount(final LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_QIYUN, 0.0);
    }

    public static double getMaxAmount(final LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_MAX_QIYUN, 0.0);
    }

    /**
     * 修改气运，按上限进行截断（允许为负）。
     * @return 修改后的气运值
     */
    public static double modify(final LivingEntity entity, final double amount) {
        if (entity == null) {
            return 0.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0.0;
        }
        final double original = readDoubleField(variables, FIELD_QIYUN, 0.0);
        final double max = readDoubleField(variables, FIELD_MAX_QIYUN, 0.0);
        final double raw = original + amount;
        final double newValue = max > 0.0 ? Math.min(raw, max) : raw;
        if (Double.compare(original, newValue) == 0) {
            return newValue;
        }
        if (!writeDoubleField(variables, FIELD_QIYUN, newValue)) {
            return original;
        }
        markSyncDirty(variables);
        return newValue;
    }

    private static Object getVariables(final LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        try {
            final Class<?> variablesHolderClass = Class.forName(GUZHENREN_MOD_VARIABLES_CLASS_NAME);
            final Field accessorField = variablesHolderClass.getField(PLAYER_VARIABLES_FIELD_NAME);
            final Object accessor = accessorField.get(null);
            final Method getDataMethod = entity.getClass().getMethod(
                ENTITY_GET_DATA_METHOD_NAME,
                EntityDataAccessor.class
            );
            return getDataMethod.invoke(entity, accessor);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }

    private static double readDoubleField(
        final Object variables,
        final String fieldName,
        final double fallback
    ) {
        if (variables == null) {
            return fallback;
        }
        try {
            final Field field = variables.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getDouble(variables);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return fallback;
        }
    }

    private static boolean writeDoubleField(
        final Object variables,
        final String fieldName,
        final double value
    ) {
        if (variables == null) {
            return false;
        }
        try {
            final Field field = variables.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setDouble(variables, value);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    private static void markSyncDirty(final Object variables) {
        if (variables == null) {
            return;
        }
        try {
            final Method markSyncDirtyMethod = variables.getClass().getMethod(METHOD_MARK_SYNC_DIRTY);
            markSyncDirtyMethod.invoke(variables);
            return;
        } catch (ReflectiveOperationException | RuntimeException exception) {
        }

        for (final String candidate : DIRTY_FIELD_CANDIDATES) {
            if (tryMarkDirtyField(variables, candidate)) {
                return;
            }
        }
    }

    private static boolean tryMarkDirtyField(final Object variables, final String fieldName) {
        try {
            final Field field = variables.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(variables, true);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }
}
