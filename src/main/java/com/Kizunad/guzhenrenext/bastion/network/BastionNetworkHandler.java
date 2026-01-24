package com.Kizunad.guzhenrenext.bastion.network;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地网络同步处理器 - 管理服务端到客户端的基地数据同步。
 * <p>
 * 同步时机：
 * <ul>
 *   <li>玩家进入基地范围时</li>
 *   <li>基地状态变化时（封印/销毁/升级）</li>
 *   <li>玩家登录时（同步附近基地）</li>
 * </ul>
 * </p>
 */
public final class BastionNetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionNetworkHandler.class);

    private BastionNetworkHandler() {
        // 工具类
    }

    // ===== 配置常量 =====

    /** 同步半径（方块）。 */
    private static final int SYNC_RADIUS = 256;

    /** 同步半径的平方（用于距离比较）。 */
    private static final int SYNC_RADIUS_SQUARED = SYNC_RADIUS * SYNC_RADIUS;

    // ===== 公开 API =====

    /**
     * 同步单个基地到指定玩家。
     *
     * @param player  目标玩家
     * @param bastion 基地数据
     */
    public static void syncToPlayer(ServerPlayer player, BastionData bastion) {
        ClientboundBastionSyncPayload payload = createSyncPayload(bastion);
        PacketDistributor.sendToPlayer(player, payload);
        LOGGER.debug("同步基地 {} 到玩家 {}", bastion.id(), player.getName().getString());
    }

    /**
     * 同步单个基地到所有在范围内的玩家。
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     */
    public static void syncToNearbyPlayers(ServerLevel level, BastionData bastion) {
        ClientboundBastionSyncPayload payload = createSyncPayload(bastion);

        for (ServerPlayer player : level.players()) {
            if (isPlayerInRange(player, bastion)) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    /**
     * 通知所有在范围内的玩家移除基地缓存。
     *
     * @param level     服务端世界
     * @param bastionId 基地 ID
     * @param coreX     核心 X 坐标（用于范围判断）
     * @param coreZ     核心 Z 坐标（用于范围判断）
     */
    public static void notifyRemoveToNearbyPlayers(
            ServerLevel level, UUID bastionId, int coreX, int coreZ) {
        ClientboundBastionRemovePayload payload = new ClientboundBastionRemovePayload(bastionId);

        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - coreX;
            double dz = player.getZ() - coreZ;
            if (dx * dx + dz * dz <= SYNC_RADIUS_SQUARED) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    /**
     * 同步玩家附近所有基地（用于玩家登录或传送后）。
     *
     * @param player 目标玩家
     */
    public static void syncNearbyBastionsToPlayer(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        Collection<BastionData> bastions = savedData.getAllBastions();

        int syncCount = 0;
        for (BastionData bastion : bastions) {
            if (isPlayerInRange(player, bastion)) {
                syncToPlayer(player, bastion);
                syncCount++;
            }
        }

        if (syncCount > 0) {
            LOGGER.debug("同步 {} 个基地到玩家 {}", syncCount, player.getName().getString());
        }
    }

    /**
     * 广播基地状态更新到所有在线玩家（跨维度）。
     * <p>
     * 用于重大状态变化（如基地被摧毁）的全服通知。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     */
    public static void broadcastStateChange(ServerLevel level, BastionData bastion) {
        syncToNearbyPlayers(level, bastion);
    }

    // ===== 内部方法 =====

    /**
     * 创建同步数据包。
     */
    private static ClientboundBastionSyncPayload createSyncPayload(BastionData bastion) {
        return new ClientboundBastionSyncPayload(
            bastion.id(),
            bastion.corePos().getX(),
            bastion.corePos().getY(),
            bastion.corePos().getZ(),
            bastion.primaryDao().ordinal(),
            bastion.tier(),
            bastion.growthRadius(),
            bastion.state().ordinal(),
            bastion.sealedUntilGameTime()
        );
    }

    /**
     * 检查玩家是否在基地同步范围内。
     */
    private static boolean isPlayerInRange(ServerPlayer player, BastionData bastion) {
        double dx = player.getX() - bastion.corePos().getX();
        double dz = player.getZ() - bastion.corePos().getZ();
        return dx * dx + dz * dz <= SYNC_RADIUS_SQUARED;
    }
}
