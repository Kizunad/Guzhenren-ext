package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionMyceliumBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 基地连通性服务（回合2：连通性 v1 + 衰败 v1）。
 * <p>
 * 目标：
 * <ul>
 *   <li>连通性为“非实时”——按周期触发，并按预算分片执行 BFS</li>
 *   <li>当菌毯（bastion_node）与基地网络断连时，进入衰败倒计时</li>
 *   <li>衰败到期后移除方块，并触发可观察反馈（粒子/音效）</li>
 * </ul>
 * </p>
 * <p>
 * 重要约束：
 * <ul>
 *   <li>运行时状态存放在 BastionSavedData 的非持久化字段，禁止修改存档 schema</li>
 *   <li>遍历/衰败时优先信任世界 BlockState，缓存仅用于候选集合，避免“缓存撒谎”</li>
 * </ul>
 * </p>
 */
public final class BastionConnectivityService {

    private BastionConnectivityService() {
    }

    /**
     * 配置常量（不外置，避免 MagicNumber）。
     */
    private static final class Config {
        // 连通性扫描相关参数已配置化：见 BastionTypeConfig.ConnectivityConfig。
        // 这里不再保留 interval/budget 的硬编码常量，避免后续出现“代码常量覆盖配置”的双源真相。

        /** 衰败总倒计时（tick）。 */
        static final int MYCELIUM_DECAY_TOTAL_TICKS = 200;
        /** 衰败处理间隔（tick）。 */
        static final long MYCELIUM_DECAY_TICK_INTERVAL = 20L;
        /** 单次衰败 tick 最大处理节点数（预算）。 */
        static final int MYCELIUM_DECAY_BUDGET_NODES = 16;

        /** 查找归属基地的最大搜索半径（与交互/Anchor 一致）。 */
        static final int MAX_OWNER_SEARCH_RADIUS = 128;

        private Config() {
        }
    }

    /**
     * 在基地 tick 中调用：推进连通性 BFS + 衰败倒计时。
     */
    public static void tick(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        tickConnectivity(level, savedData, bastion, gameTime);
        tickDecay(level, savedData, bastion, gameTime);
    }

    // ===== 连通性（非实时 BFS） =====

    private static void tickConnectivity(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.ConnectivityConfig connectivityConfig = typeConfig.connectivity();
        int spacing = Math.max(1, typeConfig.expansion().mycelium().spacing());
        int spacingY = Math.max(1, spacing / 2);

        BastionSavedData.ConnectivityRuntime runtime =
            savedData.getOrCreateConnectivityRuntime(bastion.id(), bastion.corePos());

        // 每次都更新步长，确保配置变更可生效。
        // 注意：BFS 的“步长”目前并非通过 connectivityConfig 配置，而是复用菌毯扩张的 spacing：
        // - X/Z 方向：typeConfig.expansion().mycelium().spacing()
        // - Y 方向：spacing/2
        // 这意味着：步长已可通过 bastion_type.expansion.mycelium.spacing 间接配置化。
        runtime.updateSpacing(spacing, spacingY);

        // 未到下一次扫描且当前不在扫描中：跳过。
        if (!runtime.isScanning() && gameTime < runtime.getNextScanGameTime()) {
            return;
        }

        // 启动扫描：种子为 core + anchors（anchors 作为“网络源”）。
        if (!runtime.isScanning()) {
            runtime.startScan();
            seedSources(level, savedData, bastion, runtime);
        }

        // 推进 BFS（预算化）。
        int budget = Math.max(1, connectivityConfig.bfsBudgetNodes());
        for (int i = 0; i < budget && runtime.isScanning(); i++) {
            BlockPos current = runtime.pollNext();
            if (current == null) {
                runtime.finishScan();
                break;
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir, runtime.getStepForAxis(dir.getAxis()));
                if (runtime.isVisited(neighbor)) {
                    continue;
                }
                if (!isNetworkNode(level, savedData, bastion, neighbor)) {
                    continue;
                }

                runtime.markVisited(neighbor);
                runtime.offer(neighbor);
            }
        }

        // 扫描完成：基于最终可达集合，更新衰败候选。
        if (runtime.isScanJustFinished()) {
            runtime.clearScanJustFinished();
            updateDecayTargets(level, savedData, bastion, runtime.getLastReachableNodes());
            long intervalTicks = Math.max(1L, connectivityConfig.scanIntervalTicks());
            runtime.setNextScanGameTime(gameTime + intervalTicks);
        }
    }

    private static void seedSources(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BastionSavedData.ConnectivityRuntime runtime) {
        // 核心
        runtime.addSource(bastion.corePos());

        // Anchor：作为额外源。
        for (BlockPos pos : savedData.getAnchors(bastion.id())) {
            if (!isChunkLoaded(level, pos)) {
                continue;
            }
            if (level.getBlockState(pos).getBlock() instanceof BastionAnchorBlock) {
                runtime.addSource(pos);
            }
        }
    }

    /**
     * 判断某个位置所在的区块是否已加载。
     * <p>
     * 说明：相比 isLoaded(BlockPos)，hasChunk 的语义更接近“该区块是否在内存中”。
     * 该判断用于 v1 的扫描/衰败逻辑，避免把未加载区块当成可遍历/可修改对象。
     * </p>
     */
    private static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        return level.hasChunk(chunkPos.x, chunkPos.z);
    }

    /**
     * 判断一个位置是否属于“基地网络图”的节点。
     * <p>
     * v1 定义：核心 +（缓存中记录的）菌毯/Anchor。
     * 这里优先信任世界 BlockState，再用缓存做归属约束。
     * </p>
     */
    private static boolean isNetworkNode(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BlockPos pos) {
        if (pos.equals(bastion.corePos())) {
            return true;
        }

        if (!isChunkLoaded(level, pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BastionMyceliumBlock) {
            // 菌毯节点：必须在 nodeCache 中（避免把玩家随手放的覆盖物当成网络）。
            return savedData.isNodeInCache(bastion.id(), pos);
        }

        if (state.getBlock() instanceof BastionAnchorBlock) {
            // Anchor：允许 anchorCache 或 nodeCache（worldgen 兼容）。
            return savedData.isAnchorInCache(bastion.id(), pos)
                || savedData.isNodeInCache(bastion.id(), pos);
        }

        return false;
    }

    /**
     * 在扫描完成后，根据“可达集合”与“节点缓存”更新衰败倒计时。
     */
    private static void updateDecayTargets(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            Set<BlockPos> reachable) {

        Map<BlockPos, Integer> decayMap = savedData.getOrCreateMyceliumDecayMap(bastion.id());

        // 清理已不存在/已不在缓存中的条目，防止内存泄露。
        Iterator<Map.Entry<BlockPos, Integer>> it = decayMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = it.next();
            BlockPos pos = entry.getKey();

            if (!savedData.isNodeInCache(bastion.id(), pos)) {
                it.remove();
                continue;
            }
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!(level.getBlockState(pos).getBlock() instanceof BastionMyceliumBlock)) {
                it.remove();
            }
        }

        // 遍历缓存中的菌毯节点：不可达则进入倒计时，可达则取消倒计时。
        for (BlockPos pos : savedData.getCachedNodes(bastion.id())) {
            if (pos.equals(bastion.corePos())) {
                continue;
            }
            if (!isChunkLoaded(level, pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BastionMyceliumBlock)) {
                // 缓存撒谎：方块已不是菌毯，移除倒计时。
                decayMap.remove(pos);
                continue;
            }

            if (reachable.contains(pos)) {
                decayMap.remove(pos);
                continue;
            }

            // 不可达：若尚未开始衰败，则创建倒计时。
            decayMap.putIfAbsent(pos, Math.max(1, Config.MYCELIUM_DECAY_TOTAL_TICKS));
        }
    }

    // ===== 衰败（可观察） =====

    private static void tickDecay(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        if (gameTime % Config.MYCELIUM_DECAY_TICK_INTERVAL != 0) {
            return;
        }

        Map<BlockPos, Integer> decayMap = savedData.getOrCreateMyceliumDecayMap(bastion.id());
        if (decayMap.isEmpty()) {
            return;
        }

        int budget = Math.max(1, Config.MYCELIUM_DECAY_BUDGET_NODES);
        int processed = 0;

        // 注意：destroyBlock 可能触发方块 onRemove，间接修改 decayMap。
        // 因此这里不能直接用 entrySet().iterator()，否则会触发 ConcurrentModificationException。
        java.util.List<BlockPos> snapshot = new java.util.ArrayList<>(decayMap.keySet());
        for (BlockPos pos : snapshot) {
            if (processed >= budget) {
                break;
            }

            Integer value = decayMap.get(pos);
            if (value == null) {
                continue;
            }

            // chunk 未加载：不推进，避免跨区块强行修改。
            if (!isChunkLoaded(level, pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BastionMyceliumBlock)) {
                // 方块已不存在/被替换：清理倒计时。
                decayMap.remove(pos);
                processed++;
                continue;
            }

            int remaining = value - (int) Config.MYCELIUM_DECAY_TICK_INTERVAL;
            if (remaining > 0) {
                decayMap.put(pos, remaining);
                processed++;
                continue;
            }

            // 到期：播放反馈并移除（不掉落）。
            BastionParticles.spawnNodeDecayParticles(level, pos);
            BastionSoundPlayer.playNodeDecay(level, pos);
            level.destroyBlock(pos, false);

            // 保险：若移除未触发缓存清理（例如外部调用直接替换状态），这里兜底一次。
            BastionData owner = savedData.findOwnerBastion(pos, Config.MAX_OWNER_SEARCH_RADIUS);
            if (owner != null) {
                savedData.removeNodeFromCache(owner.id(), pos);
                savedData.clearMyceliumDecay(owner.id(), pos);
            }

            // 方块 onRemove 会负责清理 nodeCache/计数与衰败状态；这里仍移除 entry 作为保险。
            decayMap.remove(pos);
            processed++;
        }
    }
}
