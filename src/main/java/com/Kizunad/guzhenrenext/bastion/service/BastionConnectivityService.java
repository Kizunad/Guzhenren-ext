package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.territory.DaoMarkData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class BastionConnectivityService {

    /**
     * 连通性计算常量集合。
     * <p>
     * 将区块位运算和中心偏移中的常量集中管理，避免 MagicNumber，
     * 同时使“区块尺寸相关语义”更清晰可读。
     * </p>
     */
    private static final class Config {

        /** 每个区块边长使用 2^4 表示，因此位移量为 4。 */
        private static final int CHUNK_SHIFT = 4;

        /** 区块中心偏移（16x16 区块中心点坐标偏移）。 */
        private static final int CHUNK_CENTER_OFFSET = 8;

        /**
         * 取整到区块时使用的“满区块补偿值”。
         * <p>
         * 等价于在右移前加上 (2^4 - 1) = 15，实现正数向上取整到区块单位。
         * </p>
         */
        private static final int CHUNK_FULL_BLOCKS = 15;

        /**
         * 区块几何重叠半径补偿。
         * <p>
         * 该值近似区块半对角线，用于让“圆形覆盖”在区块尺度下更稳定。
         * </p>
         */
        private static final int CHUNK_OVERLAP_OFFSET = 12;

        private Config() {
        }
    }

    private BastionConnectivityService() {
    }

    /**
     * 在基地 tick 中调用：推进几何连通性判定 + 衰败倒计时。
     */
    public static void tick(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        tickConnectivity(level, savedData, bastion, gameTime);
        tickDecay(level, savedData, bastion, gameTime);
    }

    private static void tickConnectivity(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.ConnectivityConfig connectivityConfig = typeConfig.connectivity();

        long intervalTicks = Math.max(1L, connectivityConfig.scanIntervalTicks());
        if (gameTime % intervalTicks != 0L) {
            return;
        }

        // 几何重叠判定：distSqr(chunkCenter, core/anchor) <= (auraRadius + offset)^2。
        int auraRadius = Math.max(1, typeConfig.aura().baseRadius());
        int overlapRadius = auraRadius + Config.CHUNK_OVERLAP_OFFSET;
        long overlapRadiusSqr = (long) overlapRadius * overlapRadius;
        int decayTotalTicks = Math.max(1, typeConfig.decay().totalTicks());

        java.util.List<BlockPos> activeAnchors = new java.util.ArrayList<>();
        for (BlockPos anchorPos : savedData.getAnchors(bastion.id())) {
            if (!isChunkLoaded(level, anchorPos)) {
                continue;
            }
            BlockState anchorState = level.getBlockState(anchorPos);
            if (anchorState.getBlock() instanceof BastionAnchorBlock) {
                activeAnchors.add(anchorPos);
            }
        }

        // 仅扫描核心附近潜在领土窗口，owner 判定由 ApertureTerritory API（savedData.getTerritoryOwner）提供。
        int maxTerritoryRadius = Math.max(1, typeConfig.expansion().mycelium().maxRadius()) + overlapRadius;
        int chunkRadius = (maxTerritoryRadius + Config.CHUNK_FULL_BLOCKS) >> Config.CHUNK_SHIFT;
        int coreChunkX = bastion.corePos().getX() >> Config.CHUNK_SHIFT;
        int coreChunkZ = bastion.corePos().getZ() >> Config.CHUNK_SHIFT;

        Map<Long, Integer> chunkDecayMap = savedData.getOrCreateChunkDecayMap(bastion.id());
        for (int offsetX = -chunkRadius; offsetX <= chunkRadius; offsetX++) {
            int chunkX = coreChunkX + offsetX;
            for (int offsetZ = -chunkRadius; offsetZ <= chunkRadius; offsetZ++) {
                int chunkZ = coreChunkZ + offsetZ;
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                long chunkKey = new ChunkPos(chunkX, chunkZ).toLong();
                if (!bastion.id().equals(savedData.getTerritoryOwner(chunkKey))) {
                    continue;
                }

                BlockPos chunkCenter = new BlockPos(
                    (chunkX << Config.CHUNK_SHIFT) + Config.CHUNK_CENTER_OFFSET,
                    bastion.corePos().getY(),
                    (chunkZ << Config.CHUNK_SHIFT) + Config.CHUNK_CENTER_OFFSET);

                boolean connected = chunkCenter.distSqr(bastion.corePos()) <= overlapRadiusSqr;
                if (!connected) {
                    for (BlockPos anchorPos : activeAnchors) {
                        if (chunkCenter.distSqr(anchorPos) <= overlapRadiusSqr) {
                            connected = true;
                            break;
                        }
                    }
                }

                if (connected) {
                    chunkDecayMap.remove(chunkKey);
                } else {
                    chunkDecayMap.putIfAbsent(chunkKey, decayTotalTicks);
                }
            }
        }
    }

    /**
     * 判断某个位置所在的区块是否已加载。
     * <p>
     * 说明：相比 isLoaded(BlockPos)，hasChunk 的语义更接近“该区块是否在内存中”。
     * 该判断用于几何连通性/衰败逻辑，避免把未加载区块当成可遍历/可修改对象。
     * </p>
     */
    private static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        return level.hasChunk(chunkPos.x, chunkPos.z);
    }

    /**
     * 兼容保留：历史 BFS 网络节点判定。
     * <p>
     * 当前版本连通性已切换为“区块几何重叠”策略，本方法仅用于保留旧逻辑语义，
     * 便于后续逐步清理 BFS 相关调用，不再参与主流程。
     * </p>
     *
     * @deprecated 已被几何覆盖判定替代，暂时保留避免一次性删除旧语义。
     */
    @Deprecated
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
        if (state.getBlock() instanceof BastionAnchorBlock) {
            return savedData.isAnchorInCache(bastion.id(), pos)
                || savedData.isNodeInCache(bastion.id(), pos);
        }

        return savedData.isNodeInCache(bastion.id(), pos);
    }

    private static void tickDecay(ServerLevel level, BastionSavedData savedData, BastionData bastion, long gameTime) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.DecayConfig decayConfig = typeConfig.decay();

        long tickInterval = Math.max(1L, decayConfig.tickInterval());
        if (gameTime % tickInterval != 0L) {
            return;
        }

        Map<Long, Integer> chunkDecayMap = savedData.getOrCreateChunkDecayMap(bastion.id());
        if (chunkDecayMap.isEmpty()) {
            return;
        }

        int budget = Math.max(1, decayConfig.budgetNodes());
        int decrement = (int) Math.min(Integer.MAX_VALUE, tickInterval);
        int processed = 0;

        // 使用快照遍历，避免在处理到期项时直接移除造成并发修改异常。
        java.util.List<Map.Entry<Long, Integer>> snapshot = new java.util.ArrayList<>(chunkDecayMap.entrySet());
        for (Map.Entry<Long, Integer> entry : snapshot) {
            if (processed >= budget) {
                break;
            }

            long chunkKey = entry.getKey();
            ChunkPos chunkPos = new ChunkPos(chunkKey);
            if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                continue;
            }

            if (!bastion.id().equals(savedData.getTerritoryOwner(chunkKey))) {
                chunkDecayMap.remove(chunkKey);
                processed++;
                continue;
            }

            int remaining = entry.getValue() - decrement;
            if (remaining > 0) {
                chunkDecayMap.put(chunkKey, remaining);
                processed++;
                continue;
            }

            // 倒计时结束：移除领土归属并清空道痕，再播放衰败反馈。
            savedData.setTerritoryOwnerIfChanged(chunkKey, null);
            savedData.setTerritoryDaoMarksIfChanged(chunkKey, DaoMarkData.EMPTY);

            BlockPos feedbackPos = new BlockPos(
                    (chunkPos.x << Config.CHUNK_SHIFT) + Config.CHUNK_CENTER_OFFSET,
                    bastion.corePos().getY(),
                    (chunkPos.z << Config.CHUNK_SHIFT) + Config.CHUNK_CENTER_OFFSET);
            BastionParticles.spawnNodeDecayParticles(level, feedbackPos);
            BastionSoundPlayer.playNodeDecay(level, feedbackPos);

            chunkDecayMap.remove(chunkKey);
            processed++;
        }
    }
}
