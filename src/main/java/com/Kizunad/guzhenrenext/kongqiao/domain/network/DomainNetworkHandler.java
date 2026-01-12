package com.Kizunad.guzhenrenext.kongqiao.domain.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 领域网络处理工具。
 * <p>
 * 用于将领域同步/移除包发送给领域附近玩家，确保多人服可见。
 * </p>
 */
public final class DomainNetworkHandler {

    private static final double SYNC_RANGE = 128.0;

    private DomainNetworkHandler() {}

    public static void sendDomainSync(
        ClientboundDomainSyncPayload payload,
        Vec3 center,
        ServerLevel level
    ) {
        if (payload == null || center == null || level == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= SYNC_RANGE * SYNC_RANGE) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    public static void sendDomainRemove(
        ClientboundDomainRemovePayload payload,
        Vec3 center,
        ServerLevel level
    ) {
        if (payload == null || center == null || level == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= SYNC_RANGE * SYNC_RANGE) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
