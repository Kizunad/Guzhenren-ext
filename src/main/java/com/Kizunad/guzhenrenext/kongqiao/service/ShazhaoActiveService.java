package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
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

        final boolean success = activeEffect.onActivate(player, data);
        if (success) {
            return new ActivationResult(true, ActivationFailureReason.NONE);
        }
        return new ActivationResult(
            false,
            ActivationFailureReason.CONDITION_NOT_MET
        );
    }

    public enum ActivationFailureReason {
        NONE,
        INVALID_INPUT,
        NOT_UNLOCKED,
        NO_DATA,
        NOT_IMPLEMENTED,
        CONDITION_NOT_MET,
    }

    public record ActivationResult(
        boolean success,
        ActivationFailureReason failureReason
    ) {}
}
