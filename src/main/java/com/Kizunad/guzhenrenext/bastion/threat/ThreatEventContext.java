package com.Kizunad.guzhenrenext.bastion.threat;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

/**
 * 威胁事件执行上下文。
 * <p>
 * 由 ThreatEventService 构建，包含事件触发所需的所有信息。
 * </p>
 *
 * @param level           服务端世界
 * @param bastion         触发事件的基地数据
 * @param triggerPos      触发位置（通常是被拆除节点的坐标）
 * @param gameTime        当前游戏时间
 * @param nearbyPlayers   附近玩家列表（在光环范围内）
 * @param nodeCountBefore 拆除前的节点数量
 * @param nodeCountAfter  拆除后的节点数量
 * @param random          随机源
 */
public record ThreatEventContext(
        ServerLevel level,
        BastionData bastion,
        BlockPos triggerPos,
        long gameTime,
        List<ServerPlayer> nearbyPlayers,
        int nodeCountBefore,
        int nodeCountAfter,
        RandomSource random
) {

    /**
     * 计算节点损失比例。
     *
     * @return 损失比例（0.0 ~ 1.0）
     */
    public double getNodeLossRatio() {
        if (nodeCountBefore <= 0) {
            return 0.0;
        }
        return 1.0 - ((double) nodeCountAfter / nodeCountBefore);
    }

    /**
     * 检查是否有可影响的玩家。
     *
     * @return true 如果有附近玩家
     */
    public boolean hasNearbyPlayers() {
        return !nearbyPlayers.isEmpty();
    }

    /**
     * 获取基地核心坐标。
     *
     * @return 核心 BlockPos
     */
    public BlockPos getCorePos() {
        return bastion.corePos();
    }

    /**
     * 获取当前有效光环半径。
     *
     * @return 光环半径
     */
    public int getAuraRadius() {
        return bastion.getAuraRadius();
    }
}
