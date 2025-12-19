package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 杀招运行服务：在玩家 Tick 中驱动已解锁的杀招被动效果。
 */
public final class ShazhaoRunningService {

    private ShazhaoRunningService() {}

    /**
     * 每秒尝试触发已解锁的杀招被动效果。
     *
     * @param player   玩家
     * @param isSecond 是否整秒 Tick
     */
    public static void tickUnlockedEffects(
        ServerPlayer player,
        boolean isSecond
    ) {
        if (!isSecond) {
            return;
        }

        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null) {
            return;
        }
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(player);

        Map<ResourceLocation, IShazhaoEffect> effects =
            ShazhaoEffectRegistry.getAll();
        if (effects.isEmpty()) {
            return;
        }

        for (Map.Entry<ResourceLocation, IShazhaoEffect> entry : effects
            .entrySet()) {
            ResourceLocation id = entry.getKey();
            IShazhaoEffect effect = entry.getValue();
            final String idText = id.toString();

            if (ShazhaoId.isActive(idText)) {
                effect.onInactive(player);
                continue;
            }
            if (config != null && !config.isPassiveEnabled(idText)) {
                effect.onInactive(player);
                continue;
            }

            if (unlocks.isShazhaoUnlocked(id)) {
                ShazhaoData data = ShazhaoDataManager.get(id);
                if (data != null) {
                    effect.onSecond(player, data);
                } else {
                    effect.onInactive(player);
                }
            } else {
                effect.onInactive(player);
            }
        }
    }
}
