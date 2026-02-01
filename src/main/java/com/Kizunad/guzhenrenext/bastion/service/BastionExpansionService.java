package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionMyceliumBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
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

        /** Anchor 采样随机扰动常量（用于与菌毯扩张的随机序列错开）。 */
        static final int ANCHOR_RANDOM_SALT = 31;

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
        BastionTypeConfig.MyceliumConfig myceliumConfig = expansionConfig.mycelium();
        BastionTypeConfig.AnchorConfig anchorConfig = expansionConfig.anchor();

        double expansionCost = calculateExpansionCost(bastion, myceliumConfig);
        if (bastion.resourcePool() < expansionCost) {
            LOGGER.info("基地 {} 扩张跳过: 资源不足, pool={}, cost={}",
                bastion.id(), String.format("%.2f", bastion.resourcePool()),
                String.format("%.2f", expansionCost));
            return 0;
        }

        // 确保 frontier 缓存已初始化
        ensureFrontierCacheInitialized(savedData, bastion);

        // 日志：检查 frontier 状态
        java.util.Set<BlockPos> frontierSet = savedData.getFrontier(bastion.id());
        LOGGER.info("基地 {} 扩张检查: pool={}, cost={}, frontier.size={}",
            bastion.id(), String.format("%.2f", bastion.resourcePool()),
            String.format("%.2f", expansionCost), frontierSet.size());

        int expandedCount = 0;
        BastionData current = bastion;

        // 每次扩张 tick 前尝试生成 Anchor（冷却 + 失败回退，不阻塞菌毯扩张）。
        current = tryPlaceAnchor(level, savedData, current, gameTime, anchorConfig, myceliumConfig);

        for (int i = 0; i < myceliumConfig.maxPerTick(); i++) {
            if (current.resourcePool() < expansionCost) {
                break;
            }

            BlockPos candidate = findExpansionCandidate(level, savedData, current, myceliumConfig);
            if (candidate == null) {
                LOGGER.info("基地 {} 扩张中断: 找不到有效候选位置 (i={})", bastion.id(), i);
                break;
            }

            if (placeMycelium(level, savedData, candidate, current)) {
                // 计算新节点到核心的距离，更新扩张半径
                int distToCore = (int) Math.ceil(Math.sqrt(candidate.distSqr(current.corePos())));
                int newGrowthRadius = Math.max(current.growthRadius(), distToCore);

                // 更新基地数据
                current = current
                    .withResourcePool(current.resourcePool() - expansionCost)
                    .withGrowthCursor(current.growthCursor() + 1)
                    .withGrowthRadius(newGrowthRadius)
                    .withMyceliumCountDelta(1);
                expandedCount++;
            }
        }

        if (expandedCount > 0) {
            // 修剪 frontier 缓存，移除已无邻接可扩张位置的节点
            pruneFrontierCache(level, savedData, current, myceliumConfig);
            savedData.updateBastion(current);
            LOGGER.debug("基地 {} 扩张了 {} 个节点", bastion.id(), expandedCount);

            // 扩张可能改变 totalNodes -> auraRadius（缩圈系数），按阈值同步
            BastionNetworkHandler.syncIfAuraRadiusChanged(level, current);
        }

        return expandedCount;
    }

    /**
     * 尝试放置 Anchor（子核心/支撑节点）。
     * <p>
     * 失败回退原则：
     * <ul>
     *   <li>资源不足 / 达到上限 / 找不到合法位置：直接返回，不影响菌毯扩张</li>
     *   <li>设置冷却，避免下一秒重复失败</li>
     * </ul>
     * </p>
     */
    private static BastionData tryPlaceAnchor(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime,
            BastionTypeConfig.AnchorConfig anchorConfig,
            BastionTypeConfig.MyceliumConfig myceliumConfig) {
        if (bastion.totalAnchors() >= anchorConfig.maxCount()) {
            return bastion;
        }

        long nextAllowed = savedData.getNextAnchorTryTick(bastion.id());
        if (gameTime < nextAllowed) {
            return bastion;
        }

        // 冷却：无论成功与否都先推迟下一次尝试，避免刷屏/卡住。
        savedData.setNextAnchorTryTick(bastion.id(), gameTime + Math.max(1L, anchorConfig.cooldownTicks()));

        // 资源不足：不尝试。
        if (bastion.resourcePool() < anchorConfig.buildCost()) {
            return bastion;
        }

        // 触发条件：当前扩张半径超过阈值。
        if (bastion.growthRadius() < anchorConfig.triggerDistance()) {
            return bastion;
        }

        ensureAnchorCacheInitialized(savedData, bastion);

        BlockPos anchorPos = findAnchorPlacement(level, savedData, bastion, anchorConfig, myceliumConfig);
        if (anchorPos == null) {
            return bastion;
        }

        if (!placeAnchor(level, savedData, anchorPos, bastion)) {
            return bastion;
        }

        BastionData updated = bastion
            .withResourcePool(bastion.resourcePool() - anchorConfig.buildCost())
            .withAnchorCountDelta(1);
        savedData.updateBastion(updated);
        return updated;
    }

    private static void ensureAnchorCacheInitialized(BastionSavedData savedData, BastionData bastion) {
        if (savedData.hasAnchorCache(bastion.id())) {
            return;
        }
        savedData.initializeAnchorCacheFromCore(bastion.id(), bastion.corePos());
    }

    /**
     * 查找 Anchor 放置位置。
     * <p>
     * 策略：从菌毯 frontier 采样，向外偏移一个大步长，满足贴地与最小间距。
     * </p>
     */
    private static BlockPos findAnchorPlacement(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BastionTypeConfig.AnchorConfig anchorConfig,
            BastionTypeConfig.MyceliumConfig myceliumConfig) {
        java.util.Set<BlockPos> frontier = savedData.getFrontier(bastion.id());
        if (frontier.isEmpty()) {
            return null;
        }

        java.util.Set<BlockPos> anchors = savedData.getAnchors(bastion.id());
        List<BlockPos> frontierList = new ArrayList<>(frontier);
        Random random = new Random(
            bastion.id().hashCode() ^ (bastion.growthCursor() + Constants.ANCHOR_RANDOM_SALT));

        int candidateSamples = Math.max(1, Constants.CANDIDATE_SAMPLE_COUNT);

        // spacing 为 Anchor 自动生成的“空间约束”参数：
        // 1) 从 frontier 向外偏移的步长；
        // 2) 与现有 Anchor 的最小间距。
        int spacing = Math.max(1, anchorConfig.spacing());
        for (int i = 0; i < candidateSamples; i++) {
            BlockPos source = frontierList.get(random.nextInt(frontierList.size()));
            Direction dir = Direction.values()[random.nextInt(Constants.DIRECTION_COUNT)];
            BlockPos candidate = source.relative(dir, spacing);

            if (!isValidAnchorTarget(level, bastion, candidate, myceliumConfig.maxRadius())) {
                continue;
            }
            if (!isFarEnoughFromAnchors(anchors, candidate, spacing)) {
                continue;
            }
            return candidate;
        }

        return null;
    }

    private static boolean isFarEnoughFromAnchors(
            java.util.Set<BlockPos> anchors,
            BlockPos candidate,
            int minSpacing) {
        if (anchors == null || anchors.isEmpty()) {
            return true;
        }
        long minSq = (long) minSpacing * minSpacing;
        for (BlockPos pos : anchors) {
            if (pos.distSqr(candidate) < minSq) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidAnchorTarget(
            ServerLevel level,
            BastionData bastion,
            BlockPos pos,
            int maxRadius) {
        if (pos.distSqr(bastion.corePos()) > (long) maxRadius * maxRadius) {
            return false;
        }

        BlockState targetState = level.getBlockState(pos);
        if (!targetState.canBeReplaced()) {
            return false;
        }

        // 贴地约束：下方必须可站立
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean placeMycelium(
            ServerLevel level,
            BastionSavedData savedData,
            BlockPos pos,
            BastionData bastion) {
        Block nodeBlock = BastionBlocks.BASTION_NODE.get();
        if (!(nodeBlock instanceof BastionMyceliumBlock myceliumBlock)) {
            return false;
        }

        BlockState nodeState = myceliumBlock.defaultBlockState();
        boolean placed = level.setBlock(pos, nodeState, Block.UPDATE_ALL);
        if (!placed) {
            return false;
        }

        // 回合2.1.1：写入菌毯归属索引（持久化）。
        // 只在“扩张服务成功放置”时写入，避免误标记玩家随手放置的装饰方块。
        savedData.indexMyceliumOwner(bastion.id(), pos);

        // 将新节点添加到缓存（用于 frontier 追踪，非持久化）。
        savedData.addNodeToCache(bastion.id(), pos);
        return true;
    }

    /**
     * 在指定位置放置 Anchor 方块。
     * <p>
     * 说明：Anchor 是“扩张服务生成”的支点节点，因此需要写入 GENERATED=true，
     * 以便与玩家手动放置的 Anchor 区分（计数/拆除风险等逻辑会使用该标记）。
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param pos       目标位置
     * @param bastion   基地数据
     * @return 是否成功放置
     */
    private static boolean placeAnchor(
            ServerLevel level,
            BastionSavedData savedData,
            BlockPos pos,
            BastionData bastion) {
        Block anchorBlock = BastionBlocks.BASTION_ANCHOR.get();
        if (!(anchorBlock instanceof BastionAnchorBlock bastionAnchorBlock)) {
            return false;
        }

        BlockState anchorState = bastionAnchorBlock.withTierDaoGenerated(
            bastion.tier(), bastion.primaryDao(), true);
        boolean placed = level.setBlock(pos, anchorState, Block.UPDATE_ALL);
        if (!placed) {
            return false;
        }

        // 回合2.1.1：写入 Anchor 归属索引（持久化）。
        savedData.indexAnchorOwner(bastion.id(), pos);

        savedData.addAnchorToCache(bastion.id(), pos);
        return true;
    }

    /**
     * 确保 frontier 缓存已初始化。
     * <p>
     * 服务器重启后首次访问基地时，从核心位置初始化缓存。
     * </p>
     */
    private static void ensureFrontierCacheInitialized(BastionSavedData savedData, BastionData bastion) {
        if (savedData.hasFrontierCache(bastion.id())) {
            return;
        }
        // 初始化 frontier：包含核心位置
        savedData.initializeFrontierFromCore(bastion.id(), bastion.corePos());

        // 将现有 Anchor 也加入 frontier，使扩张可以从 Anchor 周围开始
        java.util.Set<BlockPos> anchors = savedData.getAnchors(bastion.id());
        if (anchors != null && !anchors.isEmpty()) {
            java.util.Set<BlockPos> frontier = savedData.getFrontier(bastion.id());
            frontier.addAll(anchors);
            LOGGER.debug("初始化基地 {} 的 frontier 缓存，包含 {} 个 Anchor",
                bastion.id(), anchors.size());
        } else {
            LOGGER.debug("初始化基地 {} 的 frontier 缓存（仅核心）", bastion.id());
        }
    }

    /**
     * 计算单次扩张的资源消耗。
     *
     * @param bastion         基地数据
     * @param expansionConfig 扩张配置
     * @return 扩张成本
     */
    private static double calculateExpansionCost(BastionData bastion, BastionTypeConfig.MyceliumConfig config) {
        return config.baseCost() * Math.pow(config.tierMultiplier(), bastion.tier() - 1);
    }

    // ===== 候选位置选择 =====

    /**
     * 查找扩张候选位置。
     * <p>
     * 使用 frontier 缓存和确定性随机从边界节点的邻接位置中选择。
     * 步长使用 nodeSpacing 配置，确保节点稀疏分布。
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
            BastionTypeConfig.MyceliumConfig expansionConfig) {
        // 使用 growthCursor 作为伪随机种子
        Random random = new Random(bastion.id().hashCode() ^ bastion.growthCursor());

        // 从 frontier 缓存获取边界节点
        java.util.Set<BlockPos> frontier = savedData.getFrontier(bastion.id());
        if (frontier.isEmpty()) {
            return null;
        }

        // 获取步长配置
        int spacing = Math.max(1, expansionConfig.spacing());
        int ySpacing = Math.max(1, spacing / 2);  // Y方向使用较小步长

        // 转为列表以支持随机访问
        List<BlockPos> frontierList = new ArrayList<>(frontier);

        // 采样候选位置
        List<BlockPos> candidates = new ArrayList<>();
        int rejectedOutOfRadius = 0;
        int rejectedNotReplaceable = 0;
        int rejectedNotGrounded = 0;
        for (int i = 0; i < Constants.CANDIDATE_SAMPLE_COUNT && !frontierList.isEmpty(); i++) {
            BlockPos sourceNode = frontierList.get(random.nextInt(frontierList.size()));
            Direction dir = Direction.values()[random.nextInt(Constants.DIRECTION_COUNT)];

            // 根据方向使用不同步长
            int step = dir.getAxis() == Direction.Axis.Y ? ySpacing : spacing;
            BlockPos candidate = sourceNode.relative(dir, step);

            // 详细检查为什么候选位置无效
            int maxRadius = expansionConfig.maxRadius();
            if (candidate.distSqr(bastion.corePos()) > (long) maxRadius * maxRadius) {
                rejectedOutOfRadius++;
                continue;
            }
            BlockState targetState = level.getBlockState(candidate);
            if (!targetState.canBeReplaced()) {
                rejectedNotReplaceable++;
                continue;
            }
            BlockPos below = candidate.below();
            BlockState belowState = level.getBlockState(below);
            if (!belowState.isFaceSturdy(level, below, Direction.UP)) {
                rejectedNotGrounded++;
                continue;
            }
            candidates.add(candidate);
        }

        if (candidates.isEmpty()) {
            LOGGER.info("基地 {} findExpansionCandidate 失败: "
                + "frontier.size={}, spacing={}, 拒绝原因: 超出半径={}, 不可替换={}, 无地面={}",
                bastion.id(), frontier.size(), spacing,
                rejectedOutOfRadius, rejectedNotReplaceable, rejectedNotGrounded);
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
            BastionTypeConfig.MyceliumConfig expansionConfig) {
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
     * <p>
     * 步长使用 nodeSpacing 配置，与 findExpansionCandidate 保持一致。
     * </p>
     */
    private static boolean hasExpandableNeighbor(
            ServerLevel level,
            BastionData bastion,
            BlockPos pos,
            BastionTypeConfig.MyceliumConfig expansionConfig) {
        int spacing = Math.max(1, expansionConfig.spacing());
        int ySpacing = Math.max(1, spacing / 2);

        for (Direction dir : Direction.values()) {
            int step = dir.getAxis() == Direction.Axis.Y ? ySpacing : spacing;
            BlockPos neighbor = pos.relative(dir, step);
            if (isValidExpansionTarget(level, bastion, neighbor, expansionConfig)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查位置是否是有效的扩张目标。
     * <p>
     * 注意：稀疏化由 findExpansionCandidate 的步长控制，此方法仅检查基本约束。
     * </p>
     */
    private static boolean isValidExpansionTarget(
            ServerLevel level,
            BastionData bastion,
            BlockPos pos,
            BastionTypeConfig.MyceliumConfig expansionConfig) {
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

        // 贴地约束：菌毯扩张必须“落在地面上”。
        // 这里使用 isFaceSturdy(UP) 而不是 isSolidRender：
        // - isSolidRender 更偏向“渲染遮挡/是否看起来是实心方块”，它会把很多可站立的形态（如台阶/半砖等）排除掉；
        // - isFaceSturdy 用于判断某个面是否能提供稳定支撑，更贴近“能否站立/放置在其上”的物理语义；
        // - 扩张逻辑目标是约束菌毯不会生成在空中/可坍塌边缘，而不是做视觉意义上的实心判断。
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (!belowState.isFaceSturdy(level, below, Direction.UP)) {
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
        return state.getBlock() instanceof BastionMyceliumBlock;
    }

    // ===== 节点放置 =====
}
