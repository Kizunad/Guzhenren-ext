package com.Kizunad.guzhenrenext.xianqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 离线聚合结算服务。
 * <p>
 * 目标：玩家离线期间不做逐 tick 重演，而是按聚合模型在登录时一次性结算：
 * 1) 资源产出：每 1000 tick 产出 1 个钻石；
 * 2) 灾劫倒计时：按离线 tick 扣减剩余倒计时。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class OfflineSettlementService {

    /** 7 天离线上限（20 分钟/天，24000 tick/天）。 */
    private static final long MAX_OFFLINE_TICKS = 168000L;

    /** 每多少 tick 产出 1 个钻石。 */
    private static final long TICKS_PER_DIAMOND = 1000L;

    /** 钻石最大堆叠。 */
    private static final int MAX_STACK_SIZE = 64;

    private OfflineSettlementService() {
    }

    /**
     * 记录离线时间戳。
     *
     * @param event 玩家下线事件
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel apertureLevel = player.server.getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel == null) {
            return;
        }
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        worldData.setLastLogoutTime(player.getUUID(), apertureLevel.getGameTime());
    }

    /**
     * 玩家登录时执行离线聚合结算。
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel apertureLevel = player.server.getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel == null) {
            return;
        }

        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo apertureInfo = worldData.getAperture(player.getUUID());
        if (apertureInfo == null) {
            return;
        }

        long currentGameTime = apertureLevel.getGameTime();
        long lastLogoutTime = worldData.getLastLogoutTime(player.getUUID());
        if (lastLogoutTime < 0L || currentGameTime <= lastLogoutTime) {
            worldData.setLastLogoutTime(player.getUUID(), currentGameTime);
            return;
        }

        long offlineTicks = Math.min(MAX_OFFLINE_TICKS, currentGameTime - lastLogoutTime);
        if (offlineTicks <= 0L) {
            worldData.setLastLogoutTime(player.getUUID(), currentGameTime);
            return;
        }

        int totalDiamonds = (int) (offlineTicks / TICKS_PER_DIAMOND);
        if (totalDiamonds > 0) {
            giveDiamonds(player, totalDiamonds);
        }

        worldData.reduceTribulationTick(player.getUUID(), offlineTicks);
        worldData.setLastLogoutTime(player.getUUID(), currentGameTime);

        long remainingTribulationTick = Math.max(0L, apertureInfo.nextTribulationTick() - offlineTicks);
        player.sendSystemMessage(
            Component.literal(
                "仙窍离线结算完成：离线 "
                    + offlineTicks
                    + " tick，获得钻石 "
                    + totalDiamonds
                    + "，灾劫剩余 "
                    + remainingTribulationTick
                    + " tick。"
            )
        );
    }

    /**
     * 按堆叠上限向玩家背包发放钻石，若背包已满则掉落到地面。
     *
     * @param player 玩家
     * @param totalDiamonds 总钻石数
     */
    private static void giveDiamonds(ServerPlayer player, int totalDiamonds) {
        int remaining = totalDiamonds;
        while (remaining > 0) {
            int stackSize = Math.min(MAX_STACK_SIZE, remaining);
            ItemStack stack = new ItemStack(Items.DIAMOND, stackSize);
            boolean added = player.getInventory().add(stack);
            if (!added) {
                player.drop(stack, false);
            }
            remaining -= stackSize;
        }
    }
}
