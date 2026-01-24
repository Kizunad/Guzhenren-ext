package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionNodeBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地清理服务 - 处理 DESTROYED 状态基地的节点衰减。
 * <p>
 * 当核心被破坏后，节点会逐渐衰减消失：
 * <ul>
 *   <li>缓冲期（30秒）：无衰减，给玩家反应时间</li>
 *   <li>衰减期（30秒）：节点逐渐移除</li>
 *   <li>清理期（30秒）：移除残余节点和基地记录</li>
 * </ul>
 * </p>
 */
public final class BastionCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionCleanupService.class);

    private BastionCleanupService() {
    }

    // ===== 清理配置常量 =====

    /**
     * 清理相关常量。
     */
    private static final class CleanupConfig {
        /** 销毁后开始衰减前的缓冲刻数（30 秒）。 */
        static final long BUFFER_TICKS = 600L;
        /** 衰减窗口刻数（从销毁后 30 秒开始，持续 30 秒）。 */
        static final long DECAY_WINDOW_END_TICKS = 1200L;
        /** 每次刻最大清理节点数。 */
        static final int MAX_CLEANUPS_PER_TICK = 5;
        /** 清理搜索半径。 */
        static final int CLEANUP_SEARCH_RADIUS = 64;
        /** 节点搜索采样步长。 */
        static final int SEARCH_STEP = 2;

        private CleanupConfig() {
        }
    }

    // ===== 主清理逻辑 =====

    /**
     * 为 DESTROYED 状态的基地执行节点清理。
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据（当前未使用，预留扩展）
     * @param bastion   基地数据
     * @param gameTime  当前游戏时间
     * @return 本次清理的节点数量
     */
    public static int processCleanup(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        // 仅处理 DESTROYED 状态
        if (bastion.state() != BastionState.DESTROYED) {
            return 0;
        }

        long elapsed = gameTime - bastion.destroyedAtGameTime();

        // 缓冲期内不清理
        if (elapsed < CleanupConfig.BUFFER_TICKS) {
            return 0;
        }

        // 超过衰减窗口后由 BastionTicker 移除记录
        if (elapsed >= CleanupConfig.DECAY_WINDOW_END_TICKS) {
            return 0;
        }

        // 在衰减窗口内执行节点清理
        return cleanupNodes(level, savedData, bastion, gameTime);
    }

    /**
     * 清理基地范围内的节点。
     * <p>
     * 使用 BastionSavedData 的缓存随机采样，避免 O(n³) 遍历。
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param bastion   基地数据
     * @param gameTime  当前游戏时间
     * @return 清理的节点数量
     */
    private static int cleanupNodes(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        // 使用游戏时间作为随机种子确保确定性
        java.util.Random random = new java.util.Random(bastion.id().hashCode() ^ gameTime);

        // 从缓存随机采样节点
        List<BlockPos> nodesToCleanup = savedData.sampleNodesFromCache(
            bastion.id(),
            CleanupConfig.MAX_CLEANUPS_PER_TICK,
            random
        );

        if (nodesToCleanup.isEmpty()) {
            // 缓存为空时回退到遍历搜索（服务器重启后首次清理）
            List<BlockPos> nodes = findOwnedNodes(level, bastion);
            if (nodes.isEmpty()) {
                return 0;
            }
            int toCleanup = Math.min(CleanupConfig.MAX_CLEANUPS_PER_TICK, nodes.size());
            for (int i = 0; i < toCleanup; i++) {
                int index = random.nextInt(nodes.size());
                nodesToCleanup.add(nodes.remove(index));
            }
        }

        int cleanedCount = 0;
        for (BlockPos nodePos : nodesToCleanup) {
            if (destroyNode(level, savedData, bastion.id(), nodePos)) {
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            LOGGER.debug("基地 {} 衰减清理了 {} 个节点", bastion.id(), cleanedCount);
        }

        return cleanedCount;
    }

    /**
     * 查找基地范围内的归属节点。
     */
    private static List<BlockPos> findOwnedNodes(ServerLevel level, BastionData bastion) {
        List<BlockPos> nodes = new ArrayList<>();
        BlockPos core = bastion.corePos();
        int radius = CleanupConfig.CLEANUP_SEARCH_RADIUS;

        // 遍历搜索范围内的节点
        for (int dx = -radius; dx <= radius; dx += CleanupConfig.SEARCH_STEP) {
            for (int dy = -radius; dy <= radius; dy += CleanupConfig.SEARCH_STEP) {
                for (int dz = -radius; dz <= radius; dz += CleanupConfig.SEARCH_STEP) {
                    BlockPos pos = core.offset(dx, dy, dz);

                    // 检查 chunk 是否已加载
                    if (!level.isLoaded(pos)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof BastionNodeBlock) {
                        nodes.add(pos);
                    }
                }
            }
        }

        return nodes;
    }

    /**
     * 销毁指定位置的节点并更新缓存。
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param bastionId 基地 ID
     * @param pos       节点位置
     * @return 是否成功销毁
     */
    private static boolean destroyNode(
            ServerLevel level,
            BastionSavedData savedData,
            java.util.UUID bastionId,
            BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BastionNodeBlock)) {
            return false;
        }

        // 销毁方块，不掉落物品
        level.destroyBlock(pos, false);

        // 从缓存中移除节点
        savedData.removeNodeFromCache(bastionId, pos);

        return true;
    }

    /**
     * 强制清理基地所有节点（用于立即移除场景）。
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param bastion   基地数据
     * @return 清理的节点数量
     */
    public static int forceCleanupAll(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion) {
        // 优先使用缓存中的节点
        java.util.Set<BlockPos> cachedNodes = savedData.getCachedNodes(bastion.id());
        List<BlockPos> nodes;

        if (!cachedNodes.isEmpty()) {
            nodes = new ArrayList<>(cachedNodes);
        } else {
            // 缓存为空时回退到遍历搜索
            nodes = findOwnedNodes(level, bastion);
        }

        int cleanedCount = 0;

        for (BlockPos pos : nodes) {
            if (destroyNode(level, savedData, bastion.id(), pos)) {
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            LOGGER.info("基地 {} 强制清理了 {} 个节点", bastion.id(), cleanedCount);
        }

        return cleanedCount;
    }

    // ===== 孤儿节点调度 =====

    /**
     * 孤儿检查调度延迟刻数。
     */
    private static final int ORPHAN_CHECK_DELAY_TICKS = 100;

    /**
     * 对指定区域内的所有节点方块调度孤儿检查。
     * <p>
     * 作为安全网机制，确保基地被移除后遗漏的节点能够自清理。
     * 节点在延迟后执行 tick()，若仍无法找到归属基地则自行销毁。
     * </p>
     *
     * @param level   服务端世界
     * @param corePos 原基地核心位置
     * @param radius  扫描半径
     * @return 调度的节点数量
     */
    public static int scheduleOrphanChecks(ServerLevel level, BlockPos corePos, int radius) {
        BastionNodeBlock nodeBlock = BastionBlocks.BASTION_NODE.get();
        int scheduledCount = 0;

        for (int dx = -radius; dx <= radius; dx += CleanupConfig.SEARCH_STEP) {
            for (int dy = -radius; dy <= radius; dy += CleanupConfig.SEARCH_STEP) {
                for (int dz = -radius; dz <= radius; dz += CleanupConfig.SEARCH_STEP) {
                    BlockPos pos = corePos.offset(dx, dy, dz);

                    if (!level.isLoaded(pos)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof BastionNodeBlock) {
                        nodeBlock.scheduleOrphanCheck(level, pos, ORPHAN_CHECK_DELAY_TICKS);
                        scheduledCount++;
                    }
                }
            }
        }

        if (scheduledCount > 0) {
            LOGGER.debug("调度了 {} 个节点的孤儿检查（核心 {}）", scheduledCount, corePos);
        }

        return scheduledCount;
    }
}
