package com.Kizunad.guzhenrenext.kongqiao.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncKongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class NianTouSyncEvents {

    private NianTouSyncEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
            if (unlocks != null) {
                PacketDistributor.sendToPlayer(
                    player,
                    new PacketSyncNianTouUnlocks(unlocks)
                );
            }
            PacketDistributor.sendToPlayer(
                player,
                new PacketSyncKongqiaoData(
                    List.copyOf(NianTouDataManager.getAll()),
                    List.copyOf(ShazhaoDataManager.getAll())
                )
            );
        }
    }
}
