package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.kongqiao.attachment.GuzhenrenVariableModifiers;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

/**
 * Guzhenren 变量（PlayerVariables）临时加成服务。
 * <p>
 * 目标：为“高转被动效果”提供对部分字段上限/容量的临时提升，并支持多效果叠加与撤销。
 * </p>
 * <p>
 * 关键点：不缓存固定基线，而是在每次写入时用 {@code current - oldSum} 推导“当前基线”，
 * 从而避免玩家成长/其他系统改动导致的基线过期。
 * </p>
 */
public final class GuzhenrenVariableModifierService {

    private static final String GUZHENREN_MOD_VARIABLES_CLASS_NAME =
        "net.guzhenren.network.GuzhenrenModVariables";
    private static final String PLAYER_VARIABLES_FIELD_NAME = "PLAYER_VARIABLES";
    private static final String ENTITY_GET_DATA_METHOD_NAME = "getData";
    private static final String METHOD_MARK_SYNC_DIRTY = "markSyncDirty";
    private static final String[] DIRTY_FIELD_CANDIDATES = {
        "_syncDirty",
        "syncDirty",
    };

    public static final String VAR_MAX_ZHENYUAN = "zuida_zhenyuan";
    public static final String VAR_MAX_JINGLI = "zuida_jingli";
    public static final String VAR_MAX_HUNPO = "zuida_hunpo";
    public static final String VAR_MAX_HUNPO_RESISTANCE = "hunpo_kangxing_shangxian";
    public static final String VAR_NIANTOU_CAPACITY = "niantou_rongliang";

    private GuzhenrenVariableModifierService() {}

    public static void setAdditiveModifier(
        final LivingEntity entity,
        final String variableKey,
        final String usageId,
        final double amount
    ) {
        if (entity == null || variableKey == null || usageId == null) {
            return;
        }
        final Object vars = getVariables(entity);
        if (vars == null) {
            return;
        }
        final GuzhenrenVariableModifiers modifiers =
            KongqiaoAttachments.getGuzhenrenVariableModifiers(entity);
        if (modifiers == null) {
            return;
        }

        final double oldSum = modifiers.getSum(variableKey);
        modifiers.setModifier(variableKey, usageId, amount);
        final double newSum = modifiers.getSum(variableKey);

        applyWithDerivedBaseline(vars, variableKey, oldSum, newSum);
    }

    public static void removeModifier(
        final LivingEntity entity,
        final String variableKey,
        final String usageId
    ) {
        if (entity == null || variableKey == null || usageId == null) {
            return;
        }
        final Object vars = getVariables(entity);
        if (vars == null) {
            return;
        }
        final GuzhenrenVariableModifiers modifiers =
            KongqiaoAttachments.getGuzhenrenVariableModifiers(entity);
        if (modifiers == null) {
            return;
        }

        final double oldSum = modifiers.getSum(variableKey);
        modifiers.removeModifier(variableKey, usageId);
        final double newSum = modifiers.getSum(variableKey);

        applyWithDerivedBaseline(vars, variableKey, oldSum, newSum);
    }

    private static void applyWithDerivedBaseline(
        final Object vars,
        final String variableKey,
        final double oldSum,
        final double newSum
    ) {
        final double current = Math.max(0.0, getValue(vars, variableKey));
        final double baseline = Math.max(0.0, current - Math.max(0.0, oldSum));
        final double next = Math.max(0.0, baseline + Math.max(0.0, newSum));
        if (Double.compare(current, next) != 0 && setValue(vars, variableKey, next)) {
            markSyncDirty(vars);
        }
    }

    private static double getValue(
        final Object vars,
        final String variableKey
    ) {
        return switch (variableKey) {
            case VAR_MAX_ZHENYUAN -> readDoubleField(vars, VAR_MAX_ZHENYUAN, 0.0);
            case VAR_MAX_JINGLI -> readDoubleField(vars, VAR_MAX_JINGLI, 0.0);
            case VAR_MAX_HUNPO -> readDoubleField(vars, VAR_MAX_HUNPO, 0.0);
            case VAR_MAX_HUNPO_RESISTANCE -> readDoubleField(vars, VAR_MAX_HUNPO_RESISTANCE, 0.0);
            case VAR_NIANTOU_CAPACITY -> readDoubleField(vars, VAR_NIANTOU_CAPACITY, 0.0);
            default -> 0.0;
        };
    }

    private static boolean setValue(
        final Object vars,
        final String variableKey,
        final double value
    ) {
        return switch (variableKey) {
            case VAR_MAX_ZHENYUAN -> writeDoubleField(vars, VAR_MAX_ZHENYUAN, value);
            case VAR_MAX_JINGLI -> writeDoubleField(vars, VAR_MAX_JINGLI, value);
            case VAR_MAX_HUNPO -> writeDoubleField(vars, VAR_MAX_HUNPO, value);
            case VAR_MAX_HUNPO_RESISTANCE -> writeDoubleField(vars, VAR_MAX_HUNPO_RESISTANCE, value);
            case VAR_NIANTOU_CAPACITY -> writeDoubleField(vars, VAR_NIANTOU_CAPACITY, value);
            default -> false;
        };
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
