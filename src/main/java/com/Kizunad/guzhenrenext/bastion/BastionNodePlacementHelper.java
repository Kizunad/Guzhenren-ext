package com.Kizunad.guzhenrenext.bastion;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

/**
 * 节点放置通用连通性校验工具。
 * <p>
 * 需求：玩家放置任意节点时，必须保证放置位置与基地的菌毯/Anchor 网络连通。
 * 这里采用“缓存 + 近邻”双重兜底：
 * <ul>
 *     <li>优先信任 {@link BastionSavedData} 的 anchorCache/nodeCache（扩张产物）。</li>
 *     <li>若缓存缺失，则在半径 {@link #CONNECTIVITY_CHECK_RADIUS} 范围内寻找同基地的
 *     Anchor/菌毯（来自缓存集合），作为“与网络连通”的近似条件。</li>
 * </ul>
 * <p>
 * 这样既满足“必须连通”的约束，又避免对完整 BFS 的实时开销。
 * </p>
 */
public final class BastionNodePlacementHelper {

    /** 连通性近邻半径（包含 Y 方向，使用方块中心距离）。 */
    private static final int CONNECTIVITY_CHECK_RADIUS = 8;

    private BastionNodePlacementHelper() {
    }

    /**
     * 校验 Anchor 是否与基地菌毯网络连通，不通过时提示玩家。
     *
     * @param level      服务器世界
     * @param savedData  基地存储
     * @param bastion    归属基地
     * @param anchorPos  Anchor 坐标（节点放置位置的下方一格）
     * @param player     放置的玩家（用于提示）
     * @return true 表示连通；false 表示不连通且已提示
     */
    public static boolean ensureConnected(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BlockPos anchorPos,
            ServerPlayer player) {

        if (level == null || savedData == null || bastion == null || anchorPos == null || player == null) {
            return false;
        }

        if (isConnectedToNetwork(savedData, bastion, anchorPos)) {
            return true;
        }

        player.sendSystemMessage(Component.literal("§c该 Anchor 未与基地的菌毯网络连通，无法建造节点"));
        return false;
    }

    /**
     * 判断指定 Anchor 是否可视为与基地网络连通。
     * <p>
     * 判定顺序：
     * <ol>
     *     <li>Anchor 本身在 anchorCache 中。</li>
     *     <li>Anchor 位置已记入 nodeCache（兼容旧逻辑）。</li>
     *     <li>附近 {@link #CONNECTIVITY_CHECK_RADIUS} 内存在同基地的 Anchor/菌毯缓存。</li>
     *     <li>核心在该半径内。</li>
     * </ol>
     * </p>
     */
    private static boolean isConnectedToNetwork(
            BastionSavedData savedData,
            BastionData bastion,
            BlockPos anchorPos) {

        if (savedData.isAnchorInCache(bastion.id(), anchorPos)) {
            return true;
        }
        if (savedData.isNodeInCache(bastion.id(), anchorPos)) {
            return true;
        }

        double radiusSq = CONNECTIVITY_CHECK_RADIUS * (double) CONNECTIVITY_CHECK_RADIUS;

        if (isWithinRadius(anchorPos, bastion.corePos(), radiusSq)) {
            return true;
        }

        Set<BlockPos> anchors = savedData.getAnchorCache(bastion.id());
        for (BlockPos pos : anchors) {
            if (isWithinRadius(anchorPos, pos, radiusSq)) {
                return true;
            }
        }

        Set<BlockPos> nodes = savedData.getCachedNodes(bastion.id());
        for (BlockPos pos : nodes) {
            if (isWithinRadius(anchorPos, pos, radiusSq)) {
                return true;
            }
        }

        return false;
    }

    /** 判断两点是否在给定半径平方内。 */
    private static boolean isWithinRadius(BlockPos a, BlockPos b, double radiusSq) {
        if (a == null || b == null) {
            return false;
        }
        return a.distSqr(b) <= radiusSq;
    }
}
