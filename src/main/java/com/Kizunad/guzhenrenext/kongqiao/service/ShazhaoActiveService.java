package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 杀招主动触发服务。
 */
public final class ShazhaoActiveService {

    private static final double SHAZHAO_WHEEL_PRELOAD_PRESSURE = 2.0D;
    private static final double SHAZHAO_SUCCESS_BURST_PRESSURE = 4.0D;

    private ShazhaoActiveService() {}

    public static ActivationResult activate(
        final ServerPlayer player,
        final String shazhaoId
    ) {
        if (player == null || shazhaoId == null || shazhaoId.isBlank()) {
            return new ActivationResult(false, ActivationFailureReason.INVALID_INPUT);
        }
        if (!ShazhaoId.isActive(shazhaoId)) {
            return new ActivationResult(false, ActivationFailureReason.INVALID_INPUT);
        }
        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(shazhaoId);
        } catch (Exception e) {
            return new ActivationResult(false, ActivationFailureReason.INVALID_INPUT);
        }

        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null || !unlocks.isShazhaoUnlocked(id)) {
            return new ActivationResult(false, ActivationFailureReason.NOT_UNLOCKED);
        }

        final ShazhaoData data = ShazhaoDataManager.get(id);
        if (data == null) {
            return new ActivationResult(false, ActivationFailureReason.NO_DATA);
        }

        final IShazhaoEffect effect = ShazhaoEffectRegistry.get(id);
        if (!(effect instanceof IShazhaoActiveEffect activeEffect)) {
            return new ActivationResult(
                false,
                ActivationFailureReason.NOT_IMPLEMENTED
            );
        }

        final KongqiaoData kongqiaoData = KongqiaoAttachments.getData(player);
        final KongqiaoInventory inventory =
            kongqiaoData == null ? null : kongqiaoData.getKongqiaoInventory();
        if (
            !ShazhaoRequirementService.hasAllRequiredItems(
                ShazhaoRequirementService.collectPresentItemIds(inventory),
                data
            )
        ) {
            return new ActivationResult(
                false,
                ActivationFailureReason.CONDITION_NOT_MET
            );
        }

        final KongqiaoData.StabilityState stabilityState = kongqiaoData == null
            ? null
            : kongqiaoData.getStabilityState();
        final double currentEffectivePressure;
        final double pressureCap;
        if (kongqiaoData != null) {
            final KongqiaoPressureProjection projection =
                KongqiaoPressureProjectionService.assemblePressureProjection(
                    kongqiaoData,
                    player
                );
            currentEffectivePressure = projection.effectivePressure();
            pressureCap = projection.pressureCap();
        } else {
            currentEffectivePressure = 0.0D;
            pressureCap = Double.POSITIVE_INFINITY;
        }
        return activateResolvedEffect(
            player,
            data,
            activeEffect,
            stabilityState,
            currentEffectivePressure,
            pressureCap
        );
    }

    static ActivationResult activateResolvedEffect(
        final ServerPlayer player,
        final ShazhaoData data,
        final IShazhaoActiveEffect activeEffect,
        final KongqiaoData.StabilityState stabilityState,
        final double currentEffectivePressure,
        final double pressureCap
    ) {
        if (activeEffect == null) {
            return new ActivationResult(false, ActivationFailureReason.NOT_IMPLEMENTED);
        }
        if (
            KongqiaoPressureProjectionService.wouldProjectedPressureReachOverload(
                currentEffectivePressure,
                pressureCap,
                SHAZHAO_WHEEL_PRELOAD_PRESSURE + SHAZHAO_SUCCESS_BURST_PRESSURE
            )
        ) {
            return new ActivationResult(false, ActivationFailureReason.PRESSURE_LIMIT);
        }

        final boolean success = activeEffect.onActivate(player, data);
        if (!success) {
            return new ActivationResult(
                false,
                ActivationFailureReason.CONDITION_NOT_MET
            );
        }
        if (stabilityState != null) {
            stabilityState.setBurstPressure(
                stabilityState.getBurstPressure() + SHAZHAO_SUCCESS_BURST_PRESSURE
            );
        }
        return new ActivationResult(true, ActivationFailureReason.NONE);
    }

    static ActivationResult activateResolvedEffectForTests(
        final ShazhaoData data,
        final IShazhaoActiveEffect activeEffect,
        final KongqiaoData.StabilityState stabilityState,
        final double currentEffectivePressure,
        final double pressureCap
    ) {
        return activateResolvedEffect(
            null,
            data,
            activeEffect,
            stabilityState,
            currentEffectivePressure,
            pressureCap
        );
    }

    public enum ActivationFailureReason {
        NONE,
        INVALID_INPUT,
        NOT_UNLOCKED,
        NO_DATA,
        NOT_IMPLEMENTED,
        PRESSURE_LIMIT,
        CONDITION_NOT_MET,
    }

    public record ActivationResult(
        boolean success,
        ActivationFailureReason failureReason
    ) {}
}
