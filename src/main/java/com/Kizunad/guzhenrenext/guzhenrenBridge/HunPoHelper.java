package com.Kizunad.guzhenrenext.guzhenrenBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人魂魄交互桥接类。
 * <p>
 * 负责魂魄的读取、修改与同步。
 * </p>
 */
public final class HunPoHelper {

    private static final String GUZHENREN_MOD_VARIABLES_CLASS_NAME =
        "net.guzhenren.network.GuzhenrenModVariables";
    private static final String PLAYER_VARIABLES_FIELD_NAME = "PLAYER_VARIABLES";
    private static final String ENTITY_GET_DATA_METHOD_NAME = "getData";
    private static final String FIELD_HUNPO = "hunpo";
    private static final String FIELD_MAX_HUNPO = "zuida_hunpo";
    private static final String FIELD_RESISTANCE = "hunpo_kangxing";
    private static final String FIELD_MAX_RESISTANCE = "hunpo_kangxing_shangxian";
    private static final String METHOD_MARK_SYNC_DIRTY = "markSyncDirty";
    private static final String[] DIRTY_FIELD_CANDIDATES = {
        "_syncDirty",
        "syncDirty",
    };

    private HunPoHelper() {}

    private static final float DAMAGE = 1f;

    /**
     * 获取当前魂魄值。
     */
    public static double getAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_HUNPO, 0.0);
    }

    /**
     * 获取最大魂魄值。
     */
    public static double getMaxAmount(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_MAX_HUNPO, 0.0);
    }

    /**
     * 修改魂魄值。
     *
     * @param entity 目标实体
     * @param amount 变化量（正数为增加，负数为减少）
     * @return 修改后的魂魄值
     */
    public static double modify(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0.0;
        }
        final double original = readDoubleField(variables, FIELD_HUNPO, 0.0);
        final double max = readDoubleField(variables, FIELD_MAX_HUNPO, 0.0);

        final double newValue = Math.max(0, Math.min(max, original + amount));
        if (Double.compare(original, newValue) == 0) {
            return newValue;
        }
        if (!writeDoubleField(variables, FIELD_HUNPO, newValue)) {
            return original;
        }
        markSyncDirty(variables);
        return newValue;
    }

    /**
     * 获取当前魂魄抗性。
     */
    public static double getResistance(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_RESISTANCE, 0.0);
    }

    /**
     * 获取最大魂魄抗性。
     */
    public static double getMaxResistance(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_MAX_RESISTANCE, 0.0);
    }

    /**
     * 设置最大魂魄抗性上限。
     * <p>
     * 注意：原模组里该字段有时可能为 0（未启用/未初始化），因此使用方应自行决定如何解释 0 的含义。
     * 本方法仅负责安全写入并触发同步。
     * </p>
     *
     * @return 写入后的上限值
     */
    public static double setMaxResistance(LivingEntity entity, double value) {
        if (entity == null) {
            return 0.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0.0;
        }
        final double original = readDoubleField(variables, FIELD_MAX_RESISTANCE, 0.0);
        final double next = Math.max(0.0, value);
        if (Double.compare(original, next) == 0) {
            return next;
        }
        if (!writeDoubleField(variables, FIELD_MAX_RESISTANCE, next)) {
            return original;
        }
        markSyncDirty(variables);
        return next;
    }

    /**
     * 修改最大魂魄抗性上限。
     *
     * @return 修改后的上限值
     */
    public static double modifyMaxResistance(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0.0;
        }
        final double currentMax = readDoubleField(variables, FIELD_MAX_RESISTANCE, 0.0);
        return setMaxResistance(entity, currentMax + amount);
    }

    /**
     * 修改魂魄抗性值。
     * @return 修改后的抗性值
     */
    public static double modifyResistance(LivingEntity entity, double amount) {
        if (entity == null) {
            return 0.0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0.0;
        }
        final double original = readDoubleField(variables, FIELD_RESISTANCE, 0.0);
        final double maxResistance = readDoubleField(variables, FIELD_MAX_RESISTANCE, 0.0);
        final double max = maxResistance > 0 ? maxResistance : Double.MAX_VALUE;

        final double newValue = Math.max(0, Math.min(max, original + amount));
        if (Double.compare(original, newValue) == 0) {
            return newValue;
        }
        if (!writeDoubleField(variables, FIELD_RESISTANCE, newValue)) {
            return original;
        }
        markSyncDirty(variables);
        return newValue;
    }

    /**
     * 检查实体魂魄是否耗尽，若耗尽则执行处死逻辑（模仿原模组行为）。
     */
    public static void checkAndKill(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return;
        }
        final Double currentAmount = readOptionalDoubleField(variables, FIELD_HUNPO);
        if (currentAmount == null || currentAmount > 0.0) {
            return;
        }
        entity.hurt(
            new DamageSource(
                entity
                    .level()
                    .registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(
                        ResourceKey.create(
                            Registries.DAMAGE_TYPE,
                            ResourceLocation.parse(
                                "guzhenren:hunpoxiaosuan"
                            )
                        )
                    )
            ),
            DAMAGE
        );
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
        final Double value = readOptionalDoubleField(variables, fieldName);
        return value != null ? value : fallback;
    }

    private static Double readOptionalDoubleField(final Object variables, final String fieldName) {
        if (variables == null) {
            return null;
        }
        try {
            final Field field = variables.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getDouble(variables);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
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
