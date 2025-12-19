package com.Kizunad.guzhenrenext.kongqiao.handler;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierOne.XiaoHuiGuFrugalIdentifyEffect;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GuzhenrenExt.MODID)
public class NianTouTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null || !unlocks.isProcessing()) {
            return;
        }

        NianTouUnlocks.UnlockProcess process = unlocks.getCurrentProcess();

        // 1. 计算每 Tick 消耗
        // 如果 totalTicks 是 0 (瞬间完成)，这里不会运行，因为在 Start 时就该处理了。
        // 但为了稳健，如果 totalTicks < 1，强制设为 1
        int effectiveTotalTicks = Math.max(1, process.totalTicks);
        double costPerTick = (double) process.totalCost / effectiveTotalTicks;
        final var actives = KongqiaoAttachments.getActivePassives(player);
        if (actives != null
            && actives.isActive(XiaoHuiGuFrugalIdentifyEffect.PASSIVE_USAGE_ID)) {
            costPerTick *=
                XiaoHuiGuFrugalIdentifyEffect.getIdentifyCostMultiplierFromConfig();
        }

        // 2. 检查念头是否足够
        double currentNianTou = NianTouHelper.getAmount(player);
        
        if (currentNianTou >= costPerTick) {
            // 足够：扣除并推进
            NianTouHelper.modify(player, -costPerTick);
            process.remainingTicks--;

            // 3. 检查完成
            if (process.remainingTicks <= 0) {
                // 完成！
                unlocks.unlock(process.itemId, process.usageId);
                // 额外惊喜：满足条件时尝试推演杀招
                ShazhaoUnlockService.tryUnlockRandom(player.getRandom(), unlocks);
                unlocks.clearProcess();
                
                // 播放音效或提示 (可选)
                // player.sendSystemMessage(Component.literal("鉴定完成！"));
            }
            
            // 4. 同步状态 (为了 UI 进度条平滑，可能需要每 Tick 同步，或者每 N Ticks 同步)
            // 考虑到网络负载，可以每 5-10 ticks 同步一次，或者仅在完成时同步。
            // 但如果 UI 需要实时进度条，最好还是同步。在此扩展模组规模下，每 tick 同步给单个玩家问题不大。
            // 优化：只在整秒或者完成时同步？不，UI 需要平滑。
            PacketDistributor.sendToPlayer(player, new PacketSyncNianTouUnlocks(unlocks));
            
        } else {
            // 不足：暂停进度，不消耗，也不推进。
            // 可以在这里给玩家发一次提示“念头不足，鉴定暂停”，但要避免刷屏。
        }
    }
}
