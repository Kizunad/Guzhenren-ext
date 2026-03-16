package com.Kizunad.guzhenrenext.xianqiao.alchemy.effect;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class DeepPillEffectTickHandler {

    private DeepPillEffectTickHandler() {
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            DeepPillEffectState.tickPlayer(player, player.level().getGameTime());
        }
        ServerLevel apertureLevel = event.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel != null) {
            DeepPillEffectState.tickApertureWorld(apertureLevel);
        }
    }
}
