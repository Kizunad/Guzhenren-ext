package com.Kizunad.guzhenrenext.xianqiao.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.damage.GuzhenrenExtDamageTypes;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 混沌侵蚀边界保护处理器。
 * <p>
 * 每秒检查一次仙窍维度内玩家位置，若超出仙窍边界（min/max chunk 闭区间）加缓冲区则施加高额混沌侵蚀伤害。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ChaosErosionHandler {

    /** 仙窍维度键。 */
    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    /** 检查间隔（tick）。 */
    private static final int CHECK_INTERVAL_TICKS = 20;

    /** 允许越界缓冲（方块）。 */
    private static final int BOUNDARY_BUFFER = 16;

    private static final int WARNING_BUFFER = 8;

    /** 越界伤害值。 */
    private static final float CHAOS_DAMAGE = 5000.0F;

    private ChaosErosionHandler() {
    }

    /**
     * 服务端 Tick 后处理：定期执行混沌侵蚀边界检查。
     *
     * @param event 服务端 Tick 事件
     */
    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event) {
        ServerLevel apertureLevel = event.getServer().getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            return;
        }
        if (apertureLevel.getGameTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        for (ServerPlayer player : apertureLevel.players()) {
            applyErosionIfOutOfBounds(apertureLevel, worldData, player);
        }
    }

    /**
     * 对单个玩家执行越界检测并在需要时施加伤害。
     */
    private static void applyErosionIfOutOfBounds(
        ServerLevel level,
        ApertureWorldData worldData,
        ServerPlayer player
    ) {
        ApertureInfo apertureInfo = worldData.getAperture(player.getUUID());
        if (apertureInfo == null) {
            return;
        }

        long outsideDistanceSquared = ApertureBoundaryService.getOutsideDistanceSquared(
            apertureInfo,
            player.blockPosition()
        );

        int maxDistance = BOUNDARY_BUFFER;
        int warningDistance = Math.max(0, maxDistance - WARNING_BUFFER);
        int maxDistanceSquared = maxDistance * maxDistance;
        int warningDistanceSquared = warningDistance * warningDistance;

        if (outsideDistanceSquared <= warningDistanceSquared) {
            return;
        }

        if (outsideDistanceSquared <= maxDistanceSquared) {
            player.displayClientMessage(
                Component.translatable("warning.guzhenrenext.chaos_erosion_approaching"),
                true
            );
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.HOSTILE, 1.0F, 1.0F);
            return;
        }

        player.hurt(level.damageSources().source(GuzhenrenExtDamageTypes.CHAOS_EROSION), CHAOS_DAMAGE);
    }
}
