package com.Kizunad.guzhenrenext.guzhenrenBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

/**
 * 真元数据操作工具类。
 */
public final class ZhenYuanHelper {

    private static final String GUZHENREN_MOD_VARIABLES_CLASS_NAME =
        "net.guzhenren.network.GuzhenrenModVariables";
    private static final String PLAYER_VARIABLES_FIELD_NAME = "PLAYER_VARIABLES";
    private static final String ENTITY_GET_DATA_METHOD_NAME = "getData";
    private static final String FIELD_ZHENYUAN = "zhenyuan";
    private static final String FIELD_MAX_ZHENYUAN = "zuida_zhenyuan";
    private static final String FIELD_ZHUANSHU = "zhuanshu";
    private static final String FIELD_JIEDUAN = "jieduan";
    private static final String METHOD_MARK_SYNC_DIRTY = "markSyncDirty";
    private static final String[] DIRTY_FIELD_CANDIDATES = {
        "_syncDirty",
        "syncDirty",
    };

    private ZhenYuanHelper() {}
    private static final double MIN_ZHUANSHU = 1.0;
    private static final double POWER_MULTIPLIER = 4.0;
    private static final double DENOM_MULTIPLIER = 3.0;
    private static final double DENOM_DIVISOR = 96.0;

    public static boolean hasRuntimeVariables(final LivingEntity entity) {
        return getVariables(entity) != null;
    }

    /**
     * 获取当前真元。
     */
    public static double getAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_ZHENYUAN, 0.0);
    }

    /**
     * 获取最大真元。
     */
    public static double getMaxAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_MAX_ZHENYUAN, 0.0);
    }

    /**
     * 修改真元数值。
     * @param amount 变化量 (正数为加，负数为减)
     * @return 修改后的真元值
     */
    public static double modify(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0.0;
        }
        final double original = readDoubleField(variables, FIELD_ZHENYUAN, 0.0);
        final double reflectedMax = readDoubleField(variables, FIELD_MAX_ZHENYUAN, 0.0);
        final double max = reflectedMax > 0.0 ? reflectedMax : Double.MAX_VALUE;

        // 确保真元在 0 到 最大值 之间
        final double newValue = Math.max(0, Math.min(max, original + amount));

        if (Double.compare(original, newValue) != 0 && writeDoubleField(variables, FIELD_ZHENYUAN, newValue)) {
            markSyncDirty(variables);
        }
        return newValue;
    }

    /**
     * 检查真元是否足够。
     */
    public static boolean hasEnough(LivingEntity entity, double cost) {
        return getAmount(entity) >= cost;
    }

    /**
     * 获取当前真元百分比（0-1）。
     */
    public static float getPercentage(LivingEntity entity) {
        double max = getMaxAmount(entity);
        if (max <= 0) {
            return 0.0f;
        }
        return (float) (getAmount(entity) / max);
    }

    /**
     * 计算蛊虫消耗真元（遵循模组标准公式）。
     * Formula: Base / ((2^(Stage + Rank*4) * Rank * 3) / 96)
     * @param entity 使用者
     * @param baseCost 蛊虫的基础消耗值
     * @return 实际消耗的真元量
     */
    public static double calculateGuCost(LivingEntity entity, double baseCost) {
        final double denominator = calculateGuCostDenominator(entity);
        if (denominator <= 0.0) {
            return baseCost;
        }
        return baseCost / denominator;
    }

    /**
     * 计算蛊虫消耗的折算分母（用于“按转数/阶段分层”的统一折算）。
     * <p>
     * 注意：真元消耗必须走 {@link #calculateGuCost(LivingEntity, double)}；
     * 该分母方法用于其他资源（念头/精力/魂魄等）在需要按转数折算时复用同一尺度。
     * </p>
     */
    public static double calculateGuCostDenominator(LivingEntity entity) {
        if (entity == null) {
            return 1.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 1.0;
        }
        double zhuanshu = readDoubleField(variables, FIELD_ZHUANSHU, MIN_ZHUANSHU);
        final double jieduan = readDoubleField(variables, FIELD_JIEDUAN, 0.0);

        // 防止除以零：如果转数小于 1，视为 1。
        if (zhuanshu < 1.0) {
            zhuanshu = MIN_ZHUANSHU;
        }

        final double power = Math.pow(2.0, jieduan + zhuanshu * POWER_MULTIPLIER);
        final double denominator =
            (power * zhuanshu * DENOM_MULTIPLIER) / DENOM_DIVISOR;

        if (denominator <= 0.0) {
            return 1.0;
        }
        return denominator;
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
