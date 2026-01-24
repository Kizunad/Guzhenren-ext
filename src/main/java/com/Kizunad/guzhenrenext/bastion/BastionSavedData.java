package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 按维度存储的 Bastion 系统 SavedData。
 * <p>
 * 存储给定维度中的所有基地，仅持久化最小状态。
 * 运行时缓存（frontier、刷怪计数）不存储在此处。
 * </p>
 */
public class BastionSavedData extends SavedData {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionSavedData.class);
    private static final String DATA_NAME = GuzhenrenExt.MODID + "_bastions";

    // ===== chunk 坐标计算常量 =====
    /** chunk 坐标位移量（blockPos >> 4 得到 chunkPos）。 */
    private static final int CHUNK_SHIFT = 4;
    /** chunk 坐标掩码（32位无符号）。 */
    private static final long CHUNK_MASK = 0xFFFFFFFFL;
    /** 高位偏移量（z 坐标存储在高 32 位）。 */
    private static final int HIGH_BITS_SHIFT = 32;

    /** 整个数据结构的编解码器。 */
    private static final Codec<Map<UUID, BastionData>> BASTIONS_CODEC =
        Codec.unboundedMap(
            Codec.STRING.xmap(UUID::fromString, UUID::toString),
            BastionData.CODEC
        );

    /** 按 UUID 索引的所有基地。 */
    private final Map<UUID, BastionData> bastions = new HashMap<>();

    /**
     * 基于 chunk 的空间索引，用于快速查找。
     * <p>
     * 键：ChunkPos.toLong()，值：基地 UUID。
     * 注意：此索引假设每个 chunk 最多只有一个核心。
     * 这要求 canPlaceBastion 的最小距离足够大（至少大于同 chunk 最大间距 ≈ 21 格）。
     * </p>
     */
    private final Map<Long, UUID> chunkIndex = new HashMap<>();

    // ===== 运行时 Frontier 缓存（不持久化） =====

    /**
     * 边界节点缓存 - 仅保留"边界"位置用于扩张候选查询。
     * <p>
     * 边界节点：至少有一个相邻位置可扩张的节点。
     * 此缓存为运行时状态，不持久化到 NBT。服务器重启后从世界方块重建。
     * </p>
     */
    private final Map<UUID, java.util.Set<BlockPos>> frontierCache = new HashMap<>();

    /**
     * 所有已放置节点的位置缓存（用于清理时的随机采样）。
     * <p>
     * 此缓存为运行时状态，不持久化。
     * </p>
     */
    private final Map<UUID, java.util.Set<BlockPos>> nodeCache = new HashMap<>();

    public BastionSavedData() {
        // 默认构造函数
    }

    /**
     * 获取指定 ServerLevel 的 BastionSavedData。
     *
     * @param level 服务器世界
     * @return BastionSavedData 实例
     */
    public static BastionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(
                BastionSavedData::new,
                BastionSavedData::load
            ),
            DATA_NAME
        );
    }

    /**
     * 从 NBT 加载数据。
     *
     * @param tag      NBT 标签
     * @param provider holder 查找提供者
     * @return 加载的 BastionSavedData
     */
    public static BastionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BastionSavedData data = new BastionSavedData();

        if (tag.contains("bastions")) {
            CompoundTag bastionsTag = tag.getCompound("bastions");
            var result = BASTIONS_CODEC.parse(NbtOps.INSTANCE, bastionsTag);
            result.resultOrPartial(LOGGER::error).ifPresent(map -> {
                data.bastions.putAll(map);
                // 重建 chunk 索引
                for (BastionData bastion : map.values()) {
                    data.rebuildChunkIndex(bastion);
                }
            });
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var result = BASTIONS_CODEC.encodeStart(NbtOps.INSTANCE, bastions);
        result.resultOrPartial(LOGGER::error).ifPresent(nbt -> {
            tag.put("bastions", nbt);
        });
        return tag;
    }

    // ===== 基地 CRUD 操作 =====

    /**
     * 添加新基地到数据存储。
     *
     * @param bastion 要添加的基地
     */
    public void addBastion(BastionData bastion) {
        bastions.put(bastion.id(), bastion);
        rebuildChunkIndex(bastion);
        setDirty();
    }

    /**
     * 更新现有基地。
     *
     * @param bastion 更新后的基地数据
     */
    public void updateBastion(BastionData bastion) {
        bastions.put(bastion.id(), bastion);
        setDirty();
    }

    /**
     * 按 ID 移除基地。
     *
     * @param bastionId 基地 UUID
     * @return 被移除的基地，如果未找到则返回 null
     */
    @Nullable
    public BastionData removeBastion(UUID bastionId) {
        BastionData removed = bastions.remove(bastionId);
        if (removed != null) {
            removeFromChunkIndex(removed);
            clearCaches(bastionId);
            setDirty();
        }
        return removed;
    }

    /**
     * 按 ID 获取基地。
     *
     * @param bastionId 基地 UUID
     * @return 基地，如果未找到则返回 null
     */
    @Nullable
    public BastionData getBastion(UUID bastionId) {
        return bastions.get(bastionId);
    }

    /**
     * 获取所有基地。
     *
     * @return 所有基地的集合
     */
    public Collection<BastionData> getAllBastions() {
        return bastions.values();
    }

    /**
     * 获取基地数量。
     *
     * @return 基地计数
     */
    public int getBastionCount() {
        return bastions.size();
    }

    // ===== 空间查询 =====

    /**
     * 查找拥有指定位置的基地。
     * <p>
     * 使用不重叠约束：位置属于 maxRadius 范围内最近的核心。
     * 当前实现为全量遍历；后续可优化为基于 chunk 索引的候选查询。
     * </p>
     *
     * @param pos       世界坐标
     * @param maxRadius 最大搜索半径
     * @return 拥有该位置的基地，如果未找到则返回 null
     */
    @Nullable
    public BastionData findOwnerBastion(BlockPos pos, int maxRadius) {
        BastionData nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (BastionData bastion : bastions.values()) {
            if (bastion.state() == BastionState.DESTROYED) {
                continue;
            }
            double distSq = bastion.corePos().distSqr(pos);
            if (distSq <= (long) maxRadius * maxRadius && distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = bastion;
            }
        }

        return nearest;
    }

    /**
     * 按核心坐标查找基地。
     *
     * @param corePos 核心方块坐标
     * @return 基地，如果未找到则返回 null
     */
    @Nullable
    public BastionData findByCorePos(BlockPos corePos) {
        long chunkKey = chunkPosToLong(corePos);
        UUID bastionId = chunkIndex.get(chunkKey);
        if (bastionId != null) {
            BastionData bastion = bastions.get(bastionId);
            if (bastion != null && bastion.corePos().equals(corePos)) {
                return bastion;
            }
        }
        // 后备：线性搜索（索引正确时不应频繁发生）
        for (BastionData bastion : bastions.values()) {
            if (bastion.corePos().equals(corePos)) {
                return bastion;
            }
        }
        return null;
    }

    /**
     * 检查是否可以在指定位置放置新基地。
     * <p>
     * 强制不重叠约束：距离 >= 2 * maxRadius + buffer。
     * </p>
     *
     * @param corePos   建议的核心坐标
     * @param maxRadius 最大扩张半径
     * @param buffer    最小缓冲距离
     * @return 如果放置有效则返回 true
     */
    public boolean canPlaceBastion(BlockPos corePos, int maxRadius, int buffer) {
        int minDistance = 2 * maxRadius + buffer;
        long minDistSq = (long) minDistance * minDistance;

        for (BastionData bastion : bastions.values()) {
            if (bastion.state() == BastionState.DESTROYED) {
                continue;
            }
            if (bastion.corePos().distSqr(corePos) < minDistSq) {
                return false;
            }
        }
        return true;
    }

    // ===== 辅助方法 =====

    private void rebuildChunkIndex(BastionData bastion) {
        long chunkKey = chunkPosToLong(bastion.corePos());
        chunkIndex.put(chunkKey, bastion.id());
    }

    private void removeFromChunkIndex(BastionData bastion) {
        long chunkKey = chunkPosToLong(bastion.corePos());
        chunkIndex.remove(chunkKey);
    }

    /**
     * 将方块坐标转换为 chunk 的 long 键。
     *
     * @param pos 方块坐标
     * @return chunk 位置的 long 表示
     */
    private static long chunkPosToLong(BlockPos pos) {
        long chunkX = (long) (pos.getX() >> CHUNK_SHIFT) & CHUNK_MASK;
        long chunkZ = (long) (pos.getZ() >> CHUNK_SHIFT) & CHUNK_MASK;
        return chunkX | (chunkZ << HIGH_BITS_SHIFT);
    }

    /**
     * 将基地标记为已销毁并安排清理。
     *
     * @param bastionId 基地 ID
     * @param gameTime  当前游戏时间
     * @return 如果找到并标记了基地则返回 true
     */
    public boolean markDestroyed(UUID bastionId, long gameTime) {
        BastionData bastion = bastions.get(bastionId);
        if (bastion == null) {
            return false;
        }
        if (bastion.state() == BastionState.DESTROYED) {
            return false; // 已销毁
        }
        BastionData destroyed = bastion.withDestroyed(gameTime);
        bastions.put(bastionId, destroyed);
        setDirty();
        return true;
    }

    /**
     * 对基地应用封印。
     *
     * @param bastionId  基地 ID
     * @param sealUntil  封印截止的游戏时间
     * @return 如果找到并封印了基地则返回 true
     */
    public boolean applySeal(UUID bastionId, long sealUntil) {
        BastionData bastion = bastions.get(bastionId);
        if (bastion == null) {
            return false;
        }
        if (bastion.state() == BastionState.DESTROYED) {
            return false; // 无法封印已销毁的基地
        }
        BastionData sealed = bastion.withSealed(sealUntil);
        bastions.put(bastionId, sealed);
        setDirty();
        return true;
    }

    // ===== Frontier 缓存 API =====

    /**
     * 记录新放置的节点到缓存。
     * <p>
     * 由扩张服务在成功放置节点后调用。
     * </p>
     *
     * @param bastionId 基地 ID
     * @param pos       新节点位置
     */
    public void addNodeToCache(UUID bastionId, BlockPos pos) {
        nodeCache.computeIfAbsent(bastionId, k -> new java.util.HashSet<>()).add(pos);
        // 新节点始终添加到 frontier（稍后由 pruneFrontier 移除非边界节点）
        frontierCache.computeIfAbsent(bastionId, k -> new java.util.HashSet<>()).add(pos);
    }

    /**
     * 从缓存中移除节点（节点被销毁时调用）。
     *
     * @param bastionId 基地 ID
     * @param pos       节点位置
     */
    public void removeNodeFromCache(UUID bastionId, BlockPos pos) {
        java.util.Set<BlockPos> nodes = nodeCache.get(bastionId);
        if (nodes != null) {
            nodes.remove(pos);
        }
        java.util.Set<BlockPos> frontier = frontierCache.get(bastionId);
        if (frontier != null) {
            frontier.remove(pos);
        }
    }

    /**
     * 获取基地的边界节点缓存（用于扩张候选查询）。
     *
     * @param bastionId 基地 ID
     * @return 边界节点集合（可能为空）
     */
    public java.util.Set<BlockPos> getFrontier(UUID bastionId) {
        return frontierCache.getOrDefault(bastionId, java.util.Collections.emptySet());
    }

    /**
     * 获取基地的所有已缓存节点位置。
     *
     * @param bastionId 基地 ID
     * @return 节点位置集合（可能为空）
     */
    public java.util.Set<BlockPos> getCachedNodes(UUID bastionId) {
        return nodeCache.getOrDefault(bastionId, java.util.Collections.emptySet());
    }

    /**
     * 检查节点是否在指定基地的缓存中。
     * <p>
     * 用于区分由扩张服务放置的节点和玩家手动放置的节点。
     * </p>
     *
     * @param bastionId 基地 ID
     * @param pos       节点位置
     * @return 节点是否在缓存中
     */
    public boolean isNodeInCache(UUID bastionId, BlockPos pos) {
        java.util.Set<BlockPos> nodes = nodeCache.get(bastionId);
        return nodes != null && nodes.contains(pos);
    }

    /**
     * 检查指定基地是否已有 frontier 缓存。
     *
     * @param bastionId 基地 ID
     * @return 是否已初始化缓存
     */
    public boolean hasFrontierCache(UUID bastionId) {
        java.util.Set<BlockPos> frontier = frontierCache.get(bastionId);
        return frontier != null && !frontier.isEmpty();
    }

    /**
     * 初始化基地的 frontier 缓存（从核心位置开始）。
     * <p>
     * 在服务器重启后首次访问基地时调用。
     * </p>
     *
     * @param bastionId 基地 ID
     * @param corePos   核心位置
     */
    public void initializeFrontierFromCore(UUID bastionId, BlockPos corePos) {
        java.util.Set<BlockPos> frontier = frontierCache.computeIfAbsent(
            bastionId, k -> new java.util.HashSet<>());
        frontier.add(corePos);
        nodeCache.computeIfAbsent(bastionId, k -> new java.util.HashSet<>()).add(corePos);
    }

    /**
     * 清除指定基地的所有运行时缓存。
     * <p>
     * 在基地被移除时调用。
     * </p>
     *
     * @param bastionId 基地 ID
     */
    public void clearCaches(UUID bastionId) {
        frontierCache.remove(bastionId);
        nodeCache.remove(bastionId);
    }

    /**
     * 从缓存中随机采样指定数量的节点位置。
     * <p>
     * 用于清理服务的随机衰减选择。
     * </p>
     *
     * @param bastionId 基地 ID
     * @param count     采样数量
     * @param random    随机源
     * @return 采样的节点位置列表
     */
    public java.util.List<BlockPos> sampleNodesFromCache(UUID bastionId, int count, java.util.Random random) {
        java.util.Set<BlockPos> nodes = nodeCache.get(bastionId);
        if (nodes == null || nodes.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.List<BlockPos> nodeList = new java.util.ArrayList<>(nodes);
        if (nodeList.size() <= count) {
            return nodeList;
        }

        // Fisher-Yates 部分洗牌取前 count 个
        java.util.List<BlockPos> sampled = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int idx = i + random.nextInt(nodeList.size() - i);
            // 交换
            BlockPos temp = nodeList.get(i);
            nodeList.set(i, nodeList.get(idx));
            nodeList.set(idx, temp);
            sampled.add(nodeList.get(i));
        }
        return sampled;
    }
}
