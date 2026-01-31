package com.Kizunad.guzhenrenext.bastion.network;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    /** 同步缓冲区（避免玩家刚进入边缘时延迟收到同步）。 */
    private static final int SYNC_BUFFER = 64;

    /** 光环半径同步阈值（半径变化超过此值才触发同步）。 */
    private static final int AURA_SYNC_THRESHOLD = 16;

    // ===== 运行时缓存（不持久化） =====

    /**
     * 上次同步的光环半径缓存。
     * <p>
     * Key: 基地 UUID，Value: 上次同步时的 auraRadius。
     * 用于避免频繁同步小幅变化（如节点缓慢拆除）。
     * 服务器重启后自动清空，触发首次全量同步。
     * </p>
     */
    private static final Map<UUID, Integer> LAST_SYNCED_AURA_RADIUS = new ConcurrentHashMap<>();

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
        updateLastSyncedRadius(bastion);
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
        boolean synced = false;

        for (ServerPlayer player : level.players()) {
            if (isPlayerInRange(player, bastion)) {
                PacketDistributor.sendToPlayer(player, payload);
                synced = true;
            }
        }

        if (synced) {
            updateLastSyncedRadius(bastion);
        }
    }

    /**
     * 基于阈值检查的条件同步 - 仅在光环半径变化超过阈值时触发同步。
     * <p>
     * 用于 BastionTicker 等高频调用场景，避免每 tick 都发送网络包。
     * 适用于节点缓慢拆除导致的渐进缩圈。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     * @return true 如果触发了同步，false 如果被阈值过滤
     */
    public static boolean syncIfAuraRadiusChanged(ServerLevel level, BastionData bastion) {
        if (!shouldSyncAuraRadius(bastion)) {
            return false;
        }
        syncToNearbyPlayers(level, bastion);
        return true;
    }

    /**
     * 强制同步（忽略阈值检查）。
     * <p>
     * 用于状态变化、升级等需要立即同步的场景。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 基地数据
     */
    public static void forceSyncToNearbyPlayers(ServerLevel level, BastionData bastion) {
        syncToNearbyPlayers(level, bastion);
    }

    /**
     * 通知所有在范围内的玩家移除基地缓存。
     *
     * @param level      服务端世界
     * @param bastionId  基地 ID
     * @param coreX      核心 X 坐标（用于范围判断）
     * @param coreZ      核心 Z 坐标（用于范围判断）
     * @param auraRadius 基地光环半径（用于确定通知范围）
     */
    public static void notifyRemoveToNearbyPlayers(
            ServerLevel level, UUID bastionId, int coreX, int coreZ, int auraRadius) {
        ClientboundBastionRemovePayload payload = new ClientboundBastionRemovePayload(bastionId);
        int effectiveRadius = auraRadius + SYNC_BUFFER;
        long effectiveRadiusSq = (long) effectiveRadius * effectiveRadius;

        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - coreX;
            double dz = player.getZ() - coreZ;
            if (dx * dx + dz * dz <= effectiveRadiusSq) {
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
            bastion.getAuraRadius(),
            bastion.state().ordinal(),
            bastion.sealedUntilGameTime()
        );
    }

    /**
     * 检查玩家是否在基地同步范围内。
     * <p>
     * 使用 auraRadius 作为同步范围，确保玩家能收到所有可能影响他的基地信息。
     * </p>
     */
    private static boolean isPlayerInRange(ServerPlayer player, BastionData bastion) {
        double dx = player.getX() - bastion.corePos().getX();
        double dz = player.getZ() - bastion.corePos().getZ();
        // 使用 auraRadius 而非固定半径，确保大光环基地也能正确同步
        int auraRadius = bastion.getAuraRadius();
        // 添加缓冲区，避免玩家刚进入边缘时延迟收到同步
        int effectiveRadius = auraRadius + SYNC_BUFFER;
        return dx * dx + dz * dz <= (long) effectiveRadius * effectiveRadius;
    }

    // ===== 光环半径同步阈值检查 =====

    /**
     * 检查是否需要同步光环半径变化。
     * <p>
     * 同步条件（满足任一即触发）：
     * <ul>
     *   <li>首次同步（缓存中无记录）</li>
     *   <li>半径变化超过 AURA_SYNC_THRESHOLD</li>
     * </ul>
     * </p>
     *
     * @param bastion 基地数据
     * @return true 如果应该同步
     */
    private static boolean shouldSyncAuraRadius(BastionData bastion) {
        Integer lastRadius = LAST_SYNCED_AURA_RADIUS.get(bastion.id());
        if (lastRadius == null) {
            return true;  // 首次同步
        }

        int currentRadius = bastion.getAuraRadius();
        return Math.abs(currentRadius - lastRadius) >= AURA_SYNC_THRESHOLD;
    }

    /**
     * 更新上次同步的光环半径缓存。
     *
     * @param bastion 基地数据
     */
    private static void updateLastSyncedRadius(BastionData bastion) {
        LAST_SYNCED_AURA_RADIUS.put(bastion.id(), bastion.getAuraRadius());
    }

    /**
     * 清除指定基地的同步缓存。
     * <p>
     * 应在基地被移除时调用。
     * </p>
     *
     * @param bastionId 基地 UUID
     */
    public static void clearSyncCache(UUID bastionId) {
        LAST_SYNCED_AURA_RADIUS.remove(bastionId);
    }

    /**
     * 清除所有同步缓存。
     * <p>
     * 用于世界卸载或调试场景。
     * </p>
     */
    public static void clearAllSyncCache() {
        LAST_SYNCED_AURA_RADIUS.clear();
    }
}
