package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionNodeBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地扩张服务 - 处理资源驱动的领地扩张。
 * <p>
 * 基于设计文档的"资源池驱动 + 确定性随机"方案：
 * <ul>
 *   <li>扩张消耗资源池中的资源</li>
 *   <li>使用 growthCursor 作为伪随机种子，确保存档一致性</li>
 *   <li>优先向现有节点邻接位置扩张</li>
 * </ul>
 * </p>
 */
public final class BastionExpansionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionExpansionService.class);

    private BastionExpansionService() {
    }

    // ===== 扩张常量 =====

    /**
     * 非配置化的扩张相关常量。
     */
    private static final class Constants {
        /** 扩张候选搜索的采样数。 */
        static final int CANDIDATE_SAMPLE_COUNT = 8;
        /** 六个方向的数量。 */
        static final int DIRECTION_COUNT = 6;

        private Constants() {
        }
    }

    // ===== 主扩张逻辑 =====

    /**
     * 尝试为指定基地执行扩张。
     * <p>
     * 仅在基地处于 ACTIVE 状态且资源池足够时执行。
     * 扩张参数从 BastionTypeConfig 读取。
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param bastion   基地数据
     * @param gameTime  当前游戏时间
     * @return 本次扩张的节点数量
     */
    public static int tryExpand(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        // 仅 ACTIVE 状态可扩张
        if (bastion.getEffectiveState(gameTime) != BastionState.ACTIVE) {
            return 0;
        }

        // 从配置读取扩张参数
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.ExpansionConfig expansionConfig = typeConfig.expansion();

        double expansionCost = calculateExpansionCost(bastion, expansionConfig);
        if (bastion.resourcePool() < expansionCost) {
            return 0;
        }

        // 确保 frontier 缓存已初始化
        ensureFrontierCacheInitialized(savedData, bastion);

        int expandedCount = 0;
        BastionData current = bastion;

        for (int i = 0; i < expansionConfig.maxPerTick(); i++) {
            if (current.resourcePool() < expansionCost) {
                break;
            }

            BlockPos candidate = findExpansionCandidate(level, savedData, current, expansionConfig);
            if (candidate == null) {
                break;
            }

            if (placeNode(level, savedData, candidate, current)) {
                // 计算新节点到核心的距离，更新扩张半径
                int distToCore = (int) Math.ceil(Math.sqrt(candidate.distSqr(current.corePos())));
                int newGrowthRadius = Math.max(current.growthRadius(), distToCore);

                // 更新基地数据
                current = current
                    .withResourcePool(current.resourcePool() - expansionCost)
                    .withGrowthCursor(current.growthCursor() + 1)
                    .withGrowthRadius(newGrowthRadius)
                    .withNodeCountUpdate(current.tier(), 1);
                expandedCount++;
            }
        }

        if (expandedCount > 0) {
            // 修剪 frontier 缓存，移除已无邻接可扩张位置的节点
            pruneFrontierCache(level, savedData, current, expansionConfig);
            savedData.updateBastion(current);
            LOGGER.debug("基地 {} 扩张了 {} 个节点", bastion.id(), expandedCount);
        }

        return expandedCount;
    }

    /**
     * 确保 frontier 缓存已初始化。
     * <p>
     * 服务器重启后首次访问基地时，从核心位置初始化缓存。
     * </p>
     */
    private static void ensureFrontierCacheInitialized(BastionSavedData savedData, BastionData bastion) {
        if (!savedData.hasFrontierCache(bastion.id())) {
            savedData.initializeFrontierFromCore(bastion.id(), bastion.corePos());
            LOGGER.debug("初始化基地 {} 的 frontier 缓存", bastion.id());
        }
    }

    /**
     * 计算单次扩张的资源消耗。
     *
     * @param bastion         基地数据
     * @param expansionConfig 扩张配置
     * @return 扩张成本
     */
    private static double calculateExpansionCost(
            BastionData bastion,
            BastionTypeConfig.ExpansionConfig expansionConfig) {
        return expansionConfig.baseCost()
            * Math.pow(expansionConfig.tierMultiplier(), bastion.tier() - 1);
    }

    // ===== 候选位置选择 =====

    /**
     * 查找扩张候选位置。
     * <p>
     * 使用 frontier 缓存和确定性随机从边界节点的邻接位置中选择。
     * 复杂度从 O(n³) 降至 O(frontier_size)。
     * </p>
     *
     * @param level           服务端世界
     * @param savedData       基地存储数据
     * @param bastion         基地数据
     * @param expansionConfig 扩张配置
     * @return 候选位置，如果没有合适位置则返回 null
     */
    private static BlockPos findExpansionCandidate(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BastionTypeConfig.ExpansionConfig expansionConfig) {
        // 使用 growthCursor 作为伪随机种子
        Random random = new Random(bastion.id().hashCode() ^ bastion.growthCursor());

        // 从 frontier 缓存获取边界节点
        java.util.Set<BlockPos> frontier = savedData.getFrontier(bastion.id());
        if (frontier.isEmpty()) {
            return null;
        }

        // 转为列表以支持随机访问
        List<BlockPos> frontierList = new ArrayList<>(frontier);

        // 采样候选位置
        List<BlockPos> candidates = new ArrayList<>();
        for (int i = 0; i < Constants.CANDIDATE_SAMPLE_COUNT && !frontierList.isEmpty(); i++) {
            BlockPos sourceNode = frontierList.get(random.nextInt(frontierList.size()));
            Direction dir = Direction.values()[random.nextInt(Constants.DIRECTION_COUNT)];
            BlockPos candidate = sourceNode.relative(dir);

            if (isValidExpansionTarget(level, bastion, candidate, expansionConfig)) {
                candidates.add(candidate);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // 从候选中随机选择一个
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * 修剪 frontier 缓存，移除已无邻接可扩张位置的节点。
     * <p>
     * 在扩张完成后调用，保持 frontier 集合精简。
     * </p>
     */
    private static void pruneFrontierCache(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BastionTypeConfig.ExpansionConfig expansionConfig) {
        java.util.Set<BlockPos> frontier = savedData.getFrontier(bastion.id());
        if (frontier.isEmpty()) {
            return;
        }

        // 收集需要移除的非边界节点
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos pos : frontier) {
            if (!hasExpandableNeighbor(level, bastion, pos, expansionConfig)) {
                toRemove.add(pos);
            }
        }

        // 从 frontier 中移除（保留在 nodeCache 中）
        for (BlockPos pos : toRemove) {
            frontier.remove(pos);
        }

        if (!toRemove.isEmpty()) {
            LOGGER.debug("基地 {} frontier 修剪了 {} 个非边界节点", bastion.id(), toRemove.size());
        }
    }

    /**
     * 检查节点是否至少有一个可扩张的邻接位置。
     */
    private static boolean hasExpandableNeighbor(
            ServerLevel level,
            BastionData bastion,
            BlockPos pos,
            BastionTypeConfig.ExpansionConfig expansionConfig) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (isValidExpansionTarget(level, bastion, neighbor, expansionConfig)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查位置是否是有效的扩张目标。
     */
    private static boolean isValidExpansionTarget(
            ServerLevel level,
            BastionData bastion,
            BlockPos pos,
            BastionTypeConfig.ExpansionConfig expansionConfig) {
        // 检查是否在最大半径内
        int maxRadius = expansionConfig.maxRadius();
        if (pos.distSqr(bastion.corePos()) > (long) maxRadius * maxRadius) {
            return false;
        }

        // 检查目标位置是否可替换
        BlockState targetState = level.getBlockState(pos);
        if (!targetState.canBeReplaced()) {
            return false;
        }

        // 检查是否已被其他基地占用
        // （非重叠约束由 BastionSavedData.canPlaceBastion 在创建时保证）
        return true;
    }

    /**
     * 检查位置是否是归属于某基地的节点。
     */
    private static boolean isOwnedNode(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BastionNodeBlock;
    }

    // ===== 节点放置 =====

    /**
     * 在指定位置放置节点方块。
     * <p>
     * 使用 GENERATED=true 标记节点为扩张服务生成，
     * 确保服务器重启后节点计数逻辑仍能正确工作。
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param pos       目标位置
     * @param bastion   基地数据
     * @return 是否成功放置
     */
    private static boolean placeNode(
            ServerLevel level,
            BastionSavedData savedData,
            BlockPos pos,
            BastionData bastion) {
        Block nodeBlock = BastionBlocks.BASTION_NODE.get();
        if (!(nodeBlock instanceof BastionNodeBlock bastionNodeBlock)) {
            return false;
        }

        // 使用 GENERATED=true 标记为扩张服务生成的节点
        BlockState nodeState = bastionNodeBlock.withTierDaoGenerated(
            bastion.tier(), bastion.primaryDao(), true);

        // 放置方块（不触发 onPlace 的节点计数，由此服务直接处理）
        level.setBlock(pos, nodeState, Block.UPDATE_ALL);

        // 将新节点添加到缓存（用于 frontier 追踪，非持久化）
        savedData.addNodeToCache(bastion.id(), pos);

        return true;
    }
}
