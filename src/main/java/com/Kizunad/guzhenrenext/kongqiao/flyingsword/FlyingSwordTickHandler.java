package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordPreferencesAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordRuntimeAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 飞剑 Tick 驱动（Phase 2 最小版）。
 * <p>
 * 目标：
 * <ul>
 *     <li>确保飞剑附件每 tick 可维护（冷却递减等）。</li>
 *     <li>为后续 AI/运动/网络同步提供集中入口。</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(
    modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID,
    bus = EventBusSubscriber.Bus.GAME
)
public final class FlyingSwordTickHandler {

    private FlyingSwordTickHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 冷却递减
        FlyingSwordCooldownAttachment cooldowns = KongqiaoAttachments.getFlyingSwordCooldowns(player);
        if (cooldowns != null) {
            cooldowns.tickAll();
        }

        // runtime/state 占位：为后续 AI/网络同步预留，当前确保附件存在即可。
        FlyingSwordRuntimeAttachment runtime = KongqiaoAttachments.getFlyingSwordRuntime(player);
        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        if (state != null && !state.isInitialized()) {
            state.setInitialized(true);
        }

        // 偏好/选择存活性检查（目前仅确保附件存在）。
        FlyingSwordPreferencesAttachment preferences = KongqiaoAttachments.getFlyingSwordPreferences(player);
        FlyingSwordSelectionAttachment selection = KongqiaoAttachments.getFlyingSwordSelection(player);
        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        if (
            preferences == null
                || selection == null
                || storage == null
                || runtime == null
                || cooldowns == null
                || state == null
        ) {
            return;
        }
        // Phase 2：后续在此调用 AI/同步/战斗模块。
    }
}
