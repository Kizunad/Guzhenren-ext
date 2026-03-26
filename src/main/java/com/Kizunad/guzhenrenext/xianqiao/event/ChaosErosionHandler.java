package com.Kizunad.guzhenrenext.xianqiao.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.damage.GuzhenrenExtDamageTypes;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.runtime.ApertureRuntimeBoundaryService;
import com.Kizunad.guzhenrenext.xianqiao.runtime.ChaosZoneModel;
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

        ChaosZoneModel zoneModel = ApertureRuntimeBoundaryService.resolveModel(
            apertureInfo.minChunkX(),
            apertureInfo.maxChunkX(),
            apertureInfo.minChunkZ(),
            apertureInfo.maxChunkZ(),
            ApertureRuntimeBoundaryService.DEFAULT_ZONE_CONFIG
        );
        ChaosZoneModel.ChaosBand chaosBand = zoneModel.resolveChaosBand(
            player.blockPosition().getX(),
            player.blockPosition().getZ()
        );

        if (chaosBand == ChaosZoneModel.ChaosBand.WARNING) {
            player.displayClientMessage(
                Component.translatable("warning.guzhenrenext.chaos_erosion_approaching"),
                true
            );
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.HOSTILE, 1.0F, 1.0F);
            return;
        }

        if (chaosBand != ChaosZoneModel.ChaosBand.LETHAL) {
            return;
        }

        player.hurt(level.damageSources().source(GuzhenrenExtDamageTypes.CHAOS_EROSION), CHAOS_DAMAGE);
    }
}
