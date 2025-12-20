package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.guzhenrenBridge.PlayerVariablesSyncHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.GuzhenrenVariableModifiers;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import net.guzhenren.network.GuzhenrenModVariables;
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
        final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
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
        final GuzhenrenModVariables.PlayerVariables vars = getVariables(entity);
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
        final GuzhenrenModVariables.PlayerVariables vars,
        final String variableKey,
        final double oldSum,
        final double newSum
    ) {
        final double current = Math.max(0.0, getValue(vars, variableKey));
        final double baseline = Math.max(0.0, current - Math.max(0.0, oldSum));
        final double next = Math.max(0.0, baseline + Math.max(0.0, newSum));
        if (Double.compare(current, next) != 0) {
            setValue(vars, variableKey, next);
            PlayerVariablesSyncHelper.markSyncDirty(vars);
        }
    }

    private static double getValue(
        final GuzhenrenModVariables.PlayerVariables vars,
        final String variableKey
    ) {
        return switch (variableKey) {
            case VAR_MAX_ZHENYUAN -> vars.zuida_zhenyuan;
            case VAR_MAX_JINGLI -> vars.zuida_jingli;
            case VAR_MAX_HUNPO -> vars.zuida_hunpo;
            case VAR_MAX_HUNPO_RESISTANCE -> vars.hunpo_kangxing_shangxian;
            case VAR_NIANTOU_CAPACITY -> vars.niantou_rongliang;
            default -> 0.0;
        };
    }

    private static void setValue(
        final GuzhenrenModVariables.PlayerVariables vars,
        final String variableKey,
        final double value
    ) {
        switch (variableKey) {
            case VAR_MAX_ZHENYUAN -> vars.zuida_zhenyuan = value;
            case VAR_MAX_JINGLI -> vars.zuida_jingli = value;
            case VAR_MAX_HUNPO -> vars.zuida_hunpo = value;
            case VAR_MAX_HUNPO_RESISTANCE -> vars.hunpo_kangxing_shangxian = value;
            case VAR_NIANTOU_CAPACITY -> vars.niantou_rongliang = value;
            default -> {
                // 未支持的字段，忽略
            }
        }
    }

    private static GuzhenrenModVariables.PlayerVariables getVariables(
        final LivingEntity entity
    ) {
        try {
            return entity.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        } catch (Exception e) {
            return null;
        }
    }
}

