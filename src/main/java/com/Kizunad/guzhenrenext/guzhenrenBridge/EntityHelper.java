package com.Kizunad.guzhenrenext.guzhenrenBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊真人实体交互桥接类。
 * <p>
 * 负责处理实体的身份识别、境界判断、空窍状态检查等只读操作。
 */
public final class EntityHelper {

    private static final String GUZHENREN_MOD_VARIABLES_CLASS_NAME =
        "net.guzhenren.network.GuzhenrenModVariables";
    private static final String PLAYER_VARIABLES_FIELD_NAME = "PLAYER_VARIABLES";
    private static final String ENTITY_GET_DATA_METHOD_NAME = "getData";
    private static final String FIELD_JIEDUAN = "jieduan";
    private static final String FIELD_KONGQIAO = "kongqiao";

    private EntityHelper() {}

    /**
     * 判断实体是否为蛊师（已开窍）。
     *
     * @param entity 目标实体
     * @return true 如果已开窍
     */
    public static boolean isGuMaster(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return false;
        }
        return readDoubleField(variables, FIELD_JIEDUAN, 0.0) >= 1.0
            || readDoubleField(variables, FIELD_KONGQIAO, 0.0) > 0.0;
    }

    /**
     * 获取蛊师转数（阶段）。
     *
     * @param entity 目标实体
     * @return 转数 (1-9)，如果不是蛊师或未开窍返回 0
     */
    public static int getGuMasterRank(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        final Object variables = getVariables(entity);
        if (variables == null) {
            return 0;
        }
        return (int) readDoubleField(variables, FIELD_JIEDUAN, 0.0);
    }

    /**
     * 获取空窍完整度。
     *
     * @param entity 目标实体
     * @return 空窍值
     */
    public static double getApertureIntegrity(LivingEntity entity) {
        if (entity == null) {
            return 0.0;
        }
        return readDoubleField(getVariables(entity), FIELD_KONGQIAO, 0.0);
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
}
