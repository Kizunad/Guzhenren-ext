package com.Kizunad.guzhenrenext.xianqiao.alchemy.effect;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 护体丹效果服务端 Tick 处理器。
 * <p>
 * 仅在服务端推进：
 * 1) 激活期间确保防御修饰符与状态值一致；
 * 2) 到期后自动移除修饰符并清理状态。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class HuTiDanEffectTickHandler {

    private HuTiDanEffectTickHandler() {
    }

    /**
     * 服务端 Tick 后处理：维护护体丹临时防御状态。
     *
     * @param event 服务端 Tick 事件
     */
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            syncPlayerBodyDefenseState(player);
        }
    }

    private static void syncPlayerBodyDefenseState(ServerPlayer player) {
        long currentGameTime = player.level().getGameTime();
        if (PillEffectState.isBodyDefenseActive(player, currentGameTime)) {
            double amount = PillEffectState.readBodyDefenseAmount(player);
            PillEffectState.applyBodyDefenseModifier(player, amount);
            return;
        }
        PillEffectState.removeBodyDefenseModifier(player);
        PillEffectState.clearBodyDefenseState(player);
    }
}
