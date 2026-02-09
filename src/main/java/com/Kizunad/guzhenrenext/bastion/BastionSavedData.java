package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType;
import com.Kizunad.guzhenrenext.bastion.territory.DaoMarkData;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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

    // ===== SavedData NBT 字段名 =====

    /** 基地主数据（bastions）字段。 */
    private static final String TAG_BASTIONS = "bastions";

    /**
     * 菌毯归属索引（posAsLong -> bastionId）。
     * <p>
     * @deprecated 已迁移至 {@link #territory} (TAG_TERRITORY_DATA)。
     * 仅在数据迁移时读取，不再写入。
     * </p>
     */
    @Deprecated
    private static final String TAG_MYCELIUM_OWNER_INDEX = "mycelium_owner_index";

    /** 仙窍领地数据（chunk-based）。 */
    private static final String TAG_TERRITORY_DATA = "territory_data";

    /** Anchor 归属索引（posAsLong -> bastionId）。 */
    private static final String TAG_ANCHOR_OWNER_INDEX = "anchor_owner_index";

    // ===== chunk 坐标计算常量 =====
    /** chunk 坐标位移量（blockPos >> 4 得到 chunkPos）。 */
    private static final int CHUNK_SHIFT = 4;
    /** chunk 坐标掩码（32位无符号）。 */
    private static final long CHUNK_MASK = 0xFFFFFFFFL;
    /** 高位偏移量（z 坐标存储在高 32 位）。 */
    private static final int HIGH_BITS_SHIFT = 32;
    /** 镇地灯保护半径（格）。 */
    private static final int LANTERN_PROTECTION_RADIUS = 6;
    /** 镇地灯保护半径平方（distSqr 比较值）。 */
    private static final long LANTERN_PROTECTION_RADIUS_SQR =
        (long) LANTERN_PROTECTION_RADIUS * LANTERN_PROTECTION_RADIUS;
    /** 镇地灯最小可生效资源。 */
    private static final double LANTERN_MIN_RESOURCE = 1.0D;
    /** 旧存档迁移时，道痕初始值上限。 */
    private static final float MAX_INITIAL_MARK = 1.0F;
    /** 旧存档迁移时，每个旧菌毯方块折算的道痕值。 */
    private static final float MARK_PER_OLD_BLOCK = 0.05F;

    /** 整个数据结构的编解码器。 */
    private static final Codec<Map<UUID, BastionData>> BASTIONS_CODEC =
        Codec.unboundedMap(
            Codec.STRING.xmap(UUID::fromString, UUID::toString),
            BastionData.CODEC
        );

    /** 按 UUID 索引的所有基地。 */
    private final Map<UUID, BastionData> bastions = new HashMap<>();

    // ===== 回合2.1.1：归属索引（持久化） =====

    /** 仙窍领地数据（替代原有的菌毯索引）。 */
    private final com.Kizunad.guzhenrenext.bastion.territory.ApertureTerritory territory =
        new com.Kizunad.guzhenrenext.bastion.territory.ApertureTerritory();

    /**
     * Anchor 归属索引：key=BlockPos.asLong，value=基地 UUID。
     */
    private final Map<Long, UUID> anchorOwnerIndex = new HashMap<>();

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

    /**
     * Anchor 运行时缓存（不持久化）。
     * <p>
     * MVP：先仅用于“最小间距”校验与数量统计辅助，后续再扩展为独立的 anchorFrontier。</p>
     */
    private final Map<UUID, java.util.Set<BlockPos>> anchorCache = new HashMap<>();

    /**
     * 镇地灯运行时缓存（不持久化）。
     * <p>
     * 结构：bastionId -> (chunkPosLong -> 该 chunk 内灯笼坐标集合)。
     * 仅用于扩张期快速过滤，禁止写入 NBT。
     * </p>
     */
    private final Map<UUID, Map<Long, java.util.Set<BlockPos>>> lanternCache = new HashMap<>();

    /**
     * Anchor 自动生成冷却（不持久化）。
     */
    private final Map<UUID, Long> nextAnchorTryTick = new HashMap<>();

    /**
     * Round 4.2：守卫孵化巢（GuardianHatchery）基地级冷却（不持久化）。
     * <p>
     * 语义：记录某个基地下一次允许“尝试孵化”的 gameTime。
     * 说明：本回合不引入 BlockEntity，因此冷却由 SavedData 运行时缓存承担，重启后会重置。
     * 后续回合可迁移为持久化字段（并提供兼容读写）。
     * </p>
     */
    private final Map<UUID, Long> nextHatcheryTryTick = new HashMap<>();

    /**
     * Round 24：炮台节点冷却（基地级，不持久化）。
     */
    private final Map<UUID, Long> nextTurretTryTick = new HashMap<>();

    /**
     * Round 25：陷阱节点冷却（按坐标，不持久化）。
     */
    private final Map<BlockPos, Long> nextTrapTryTick = new HashMap<>();

    // ===== 回合2：连通性/衰败运行时状态（不持久化） =====

    /**
     * 连通性扫描运行时状态（按基地）。
     * <p>
     * 注意：该结构只用于“非实时、预算化”的 BFS 扫描；不写入 NBT。
     * </p>
     */
    private final Map<UUID, ConnectivityRuntime> connectivityRuntimes = new HashMap<>();


    /**
     * 区块级衰败倒计时（按基地）。
     * <p>
     * key=chunkKey（{@link net.minecraft.world.level.ChunkPos#asLong(int, int)}），value=剩余 tick。
     * 运行时缓存，不写入 NBT。
     * </p>
     */
    private final Map<UUID, Map<Long, Integer>> chunkDecayTicks = new HashMap<>();

    /**
     * 断连菌毯的衰败倒计时（按基地）。
     * <p>
     * key=菌毯位置，value=剩余 tick。不写入 NBT。
     * </p>
     *
     * @deprecated 将被区块级衰败缓存替代，保留用于兼容过渡。
     */
    @Deprecated
    private final Map<UUID, Map<BlockPos, Integer>> myceliumDecayTicks = new HashMap<>();

    // ===== 回合3：能源挂载运行时缓存（不持久化） =====

    /**
     * 某个基地的“AnchorPos -> BastionEnergyType”挂载记录（运行时缓存）。
     * <p>
     * 设计说明：
     * <ul>
     *     <li>
     *         该结构 <b>不写入 NBT</b>，不改变存档 schema。
     *         能源类型是可从世界状态（Anchor 周边结构/方块）推导的派生信息，服务器重启后应由后续“预算化扫描/重建流程”重新填充。
     *     </li>
     *     <li>
     *         key 使用 {@link BlockPos}：Anchor 的语义是“固定坐标挂载点”。后续能源扫描/预算化只需要按坐标定位并快速查到类型，
     *         不需要额外的对象封装；同时与现有的 myceliumDecayTicks 等运行时结构保持一致。
     *     </li>
     *     <li>该 Map 仅用于运行期加速（例如 BastionEnergyService / BastionTicker 合并加成时的查询）。</li>
     *     <li>请勿在此处实现扫描逻辑。</li>
     * </ul>
     * </p>
     */
    private final Map<UUID, Map<BlockPos, BastionEnergyType>> anchorEnergyTypes = new HashMap<>();

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

        if (tag.contains(TAG_BASTIONS, Tag.TAG_COMPOUND)) {
            CompoundTag bastionsTag = tag.getCompound(TAG_BASTIONS);
            var result = BASTIONS_CODEC.parse(NbtOps.INSTANCE, bastionsTag);
            result.resultOrPartial(LOGGER::error).ifPresent(map -> {
                data.bastions.putAll(map);
                // 重建 chunk 索引
                for (BastionData bastion : map.values()) {
                    data.rebuildChunkIndex(bastion);
                }
            });
        }

        // 读取领地数据（新格式）
        if (tag.contains(TAG_TERRITORY_DATA, Tag.TAG_COMPOUND)) {
            CompoundTag territoryTag = tag.getCompound(TAG_TERRITORY_DATA);
            com.Kizunad.guzhenrenext.bastion.territory.ApertureTerritory.CODEC
                .parse(NbtOps.INSTANCE, territoryTag)
                .resultOrPartial(LOGGER::error)
                .ifPresent(loadedTerritory -> {
                    loadedTerritory.getAllOwners().forEach(data.territory::setOwner);
                    loadedTerritory.getAllDaoMarks().forEach(data.territory::setDaoMarks);
                });
        } else if (tag.contains(TAG_MYCELIUM_OWNER_INDEX, Tag.TAG_COMPOUND)) {
            LOGGER.info("正在迁移 BastionSavedData：从 BlockPos 索引迁移至 ApertureTerritory...");
            Map<Long, UUID> oldIndex = new HashMap<>();
            readOwnerIndexIfPresent(tag, TAG_MYCELIUM_OWNER_INDEX, oldIndex);

            Map<Long, Map<UUID, Integer>> chunkOwnerCounts = new HashMap<>();
            for (Map.Entry<Long, UUID> entry : oldIndex.entrySet()) {
                long chunkKey = chunkPosToLong(BlockPos.of(entry.getKey()));
                Map<UUID, Integer> ownerCounts = chunkOwnerCounts.computeIfAbsent(chunkKey, key -> new HashMap<>());
                UUID ownerId = entry.getValue();
                ownerCounts.put(ownerId, ownerCounts.getOrDefault(ownerId, 0) + 1);
            }

            int migratedChunkCount = 0;
            for (Map.Entry<Long, Map<UUID, Integer>> chunkEntry : chunkOwnerCounts.entrySet()) {
                long chunkKey = chunkEntry.getKey();
                UUID selectedOwnerId = null;
                int selectedCount = 0;
                for (Map.Entry<UUID, Integer> ownerCountEntry : chunkEntry.getValue().entrySet()) {
                    UUID candidateOwnerId = ownerCountEntry.getKey();
                    int candidateCount = ownerCountEntry.getValue();
                    if (selectedOwnerId == null
                        || candidateCount > selectedCount
                        || (candidateCount == selectedCount
                            && candidateOwnerId.toString().compareTo(selectedOwnerId.toString()) < 0)) {
                        selectedOwnerId = candidateOwnerId;
                        selectedCount = candidateCount;
                    }
                }

                BastionData ownerBastion = data.getBastion(selectedOwnerId);
                if (ownerBastion == null || ownerBastion.state() == BastionState.DESTROYED) {
                    continue;
                }

                data.territory.setOwner(chunkKey, selectedOwnerId);
                float mark = Math.min(MAX_INITIAL_MARK, selectedCount * MARK_PER_OLD_BLOCK);
                DaoMarkData daoMarks;
                switch (ownerBastion.primaryDao()) {
                    case ZHI_DAO -> daoMarks = new DaoMarkData(mark, 0.0F, 0.0F, 0.0F);
                    case HUN_DAO -> daoMarks = new DaoMarkData(0.0F, mark, 0.0F, 0.0F);
                    case MU_DAO -> daoMarks = new DaoMarkData(0.0F, 0.0F, mark, 0.0F);
                    case LI_DAO -> daoMarks = new DaoMarkData(0.0F, 0.0F, 0.0F, mark);
                    default -> daoMarks = DaoMarkData.EMPTY;
                }
                data.territory.setDaoMarks(chunkKey, daoMarks);
                migratedChunkCount++;
            }

            LOGGER.info("迁移完成，共转换 {} 个区块归属（旧记录数：{}）。", migratedChunkCount, oldIndex.size());
            if (migratedChunkCount > 0) {
                data.setDirty();
            }
        }

        // 回合2.1.1：归属索引（旧存档缺失时视为空 map，保持兼容）。
        // myceliumOwnerIndex 已废弃，不再读取到 map 中
        readOwnerIndexIfPresent(tag, TAG_ANCHOR_OWNER_INDEX, data.anchorOwnerIndex);

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var result = BASTIONS_CODEC.encodeStart(NbtOps.INSTANCE, bastions);
        result.resultOrPartial(LOGGER::error).ifPresent(nbt -> {
            tag.put(TAG_BASTIONS, nbt);
        });
        
        // 持久化领地数据
        var territoryResult = com.Kizunad.guzhenrenext.bastion.territory.ApertureTerritory.CODEC
            .encodeStart(NbtOps.INSTANCE, territory);
        territoryResult.resultOrPartial(LOGGER::error).ifPresent(nbt -> {
            tag.put(TAG_TERRITORY_DATA, nbt);
        });

        // 回合2.1.1：仅持久化 owner 索引。
        // TAG_MYCELIUM_OWNER_INDEX 已移除，不再写入。
        writeOwnerIndexIfNotEmpty(tag, TAG_ANCHOR_OWNER_INDEX, anchorOwnerIndex);
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

    /**
     * 获取指定陷阱位置的下一次触发时间。
     *
     * @param pos 陷阱方块位置
     * @return 允许触发的 gameTime，缺省为 0
     */
    public long getNextTrapTryTick(BlockPos pos) {
        return nextTrapTryTick.getOrDefault(pos, 0L);
    }

    /**
     * 更新指定陷阱位置的下一次触发时间。
     *
     * @param pos  陷阱方块位置
     * @param tick 允许触发的 gameTime
     */
    public void setNextTrapTryTick(BlockPos pos, long tick) {
        nextTrapTryTick.put(pos, tick);
    }

    /**
     * 清理陷阱冷却缓存（方块被移除时调用）。
     *
     * @param pos 陷阱方块位置
     */
    public void clearTrapCooldown(BlockPos pos) {
        nextTrapTryTick.remove(pos);
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
        // 回合2.1.1：优先精确索引（菌毯/Anchor）命中，避免全量遍历。
        BastionData byIndex = findOwnerBastionByIndex(pos);
        if (byIndex != null) {
            return byIndex;
        }

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
     * 通过“归属索引”查找该位置的基地。
     * <p>
     * 优先级：Anchor 索引 -> 领地索引。
     * <br>
     * 若索引命中但对应基地已不存在/已销毁，会自动清理该索引项并返回 null，调用方应继续走 fallback。
     * </p>
     */
    @Nullable
    public BastionData findOwnerBastionByIndex(BlockPos pos) {
        BastionData owner = findOwnerFromIndexMap(anchorOwnerIndex, pos);
        if (owner != null) {
            return owner;
        }
        
        // 领地索引查询 (Chunk-based)
        long chunkKey = chunkPosToLong(pos);
        UUID bastionId = territory.getOwner(chunkKey);
        
        if (bastionId == null) {
            return null;
        }
        
        BastionData bastion = bastions.get(bastionId);
        if (bastion == null || bastion.state() == BastionState.DESTROYED) {
            // 索引“撒谎”：基地不存在/已销毁。这里自愈。
            territory.setOwner(chunkKey, null);
            setDirty();
            return null;
        }
        
        return bastion;
    }

    @Nullable
    private BastionData findOwnerFromIndexMap(Map<Long, UUID> index, BlockPos pos) {
        long key = pos.asLong();
        UUID bastionId = index.get(key);
        if (bastionId == null) {
            return null;
        }

        BastionData bastion = bastions.get(bastionId);
        if (bastion == null || bastion.state() == BastionState.DESTROYED) {
            // 索引“撒谎”：基地不存在/已销毁。这里自愈，避免永久污染查询。
            index.remove(key);
            setDirty();
            return null;
        }

        return bastion;
    }

    // ===== 回合2.1.1：索引维护 API =====

    /**
     * 读取指定 chunk 的领土归属。
     * <p>
     * 该方法是对 {@link #territory} 的只读透传，不暴露内部可变结构。
     * </p>
     *
     * @param chunkKey chunk 键（ChunkPos.toLong）
     * @return 归属基地 UUID；若无归属返回 null
     */
    @Nullable
    public UUID getTerritoryOwner(long chunkKey) {
        return territory.getOwner(chunkKey);
    }

    /**
     * 读取指定 chunk 的道痕数据。
     * <p>
     * 无数据时返回 {@link DaoMarkData#EMPTY}，避免调用方处理 null。
     * </p>
     *
     * @param chunkKey chunk 键（ChunkPos.toLong）
     * @return 道痕数据（永不为 null）
     */
    public DaoMarkData getTerritoryDaoMarks(long chunkKey) {
        return territory.getDaoMarks(chunkKey);
    }

    /**
     * 在归属发生变化时更新 chunk 归属。
     * <p>
     * 仅当新旧值不相等时才写入并调用 {@link #setDirty()}。
     * </p>
     *
     * @param chunkKey chunk 键（ChunkPos.toLong）
     * @param owner 新归属（null 表示清除归属）
     * @return 若实际发生变更返回 true，否则返回 false
     */
    public boolean setTerritoryOwnerIfChanged(long chunkKey, @Nullable UUID owner) {
        UUID currentOwner = territory.getOwner(chunkKey);
        if (Objects.equals(currentOwner, owner)) {
            return false;
        }
        territory.setOwner(chunkKey, owner);
        setDirty();
        return true;
    }

    /**
     * 在道痕发生变化时更新 chunk 道痕。
     * <p>
     * 当入参为 null 时按 {@link DaoMarkData#EMPTY} 处理；
     * 仅当新旧值不相等时才写入并调用 {@link #setDirty()}。
     * </p>
     *
     * @param chunkKey chunk 键（ChunkPos.toLong）
     * @param marks 新道痕（null 视为 EMPTY）
     * @return 若实际发生变更返回 true，否则返回 false
     */
    public boolean setTerritoryDaoMarksIfChanged(long chunkKey, DaoMarkData marks) {
        DaoMarkData normalizedMarks = marks == null ? DaoMarkData.EMPTY : marks;
        DaoMarkData currentMarks = territory.getDaoMarks(chunkKey);
        if (Objects.equals(currentMarks, normalizedMarks)) {
            return false;
        }
        territory.setDaoMarks(chunkKey, normalizedMarks);
        setDirty();
        return true;
    }

    /**
     * 写入菌毯归属索引（现已升级为领地归属）。
     * <p>
     * 仅在“扩张成功放置菌毯”时调用。
     * </p>
     */
    public void indexMyceliumOwner(UUID bastionId, BlockPos pos) {
        if (bastionId == null || pos == null) {
            return;
        }
        long chunkKey = chunkPosToLong(pos);
        UUID current = territory.getOwner(chunkKey);
        if (!bastionId.equals(current)) {
            territory.setOwner(chunkKey, bastionId);
            setDirty();
        }
    }

    /** 清理菌毯归属索引（现为清理 chunk 归属）。 */
    public void clearMyceliumOwnerIndex(BlockPos pos) {
        if (pos == null) {
            return;
        }
        long chunkKey = chunkPosToLong(pos);
        if (territory.getOwner(chunkKey) != null) {
            territory.setOwner(chunkKey, null);
            setDirty();
        }
    }

    /** 写入 Anchor 归属索引。 */
    public void indexAnchorOwner(UUID bastionId, BlockPos pos) {
        putOwnerIndex(anchorOwnerIndex, bastionId, pos);
    }

    /** 清理 Anchor 归属索引（方块移除时兜底）。 */
    public void clearAnchorOwnerIndex(BlockPos pos) {
        removeOwnerIndex(anchorOwnerIndex, pos);
    }

    private void putOwnerIndex(Map<Long, UUID> index, UUID bastionId, BlockPos pos) {
        if (bastionId == null || pos == null) {
            return;
        }
        long key = pos.asLong();
        UUID prev = index.put(key, bastionId);
        if (!bastionId.equals(prev)) {
            setDirty();
        }
    }

    private void removeOwnerIndex(Map<Long, UUID> index, BlockPos pos) {
        if (pos == null) {
            return;
        }
        long key = pos.asLong();
        UUID removed = index.remove(key);
        if (removed != null) {
            setDirty();
        }
    }

    private static void readOwnerIndexIfPresent(CompoundTag root, String tagName, Map<Long, UUID> out) {
        if (!root.contains(tagName, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag indexTag = root.getCompound(tagName);
        for (String keyStr : indexTag.getAllKeys()) {
            try {
                long posLong = Long.parseLong(keyStr);
                UUID bastionId = UUID.fromString(indexTag.getString(keyStr));
                out.put(posLong, bastionId);
            } catch (Exception ex) {
                // 兼容与鲁棒性：跳过损坏条目，避免整个 SavedData 无法加载。
                LOGGER.warn("读取 BastionSavedData 索引字段 {} 时遇到损坏条目 key={}，将跳过", tagName, keyStr);
            }
        }
    }

    private static void writeOwnerIndexIfNotEmpty(CompoundTag root, String tagName, Map<Long, UUID> index) {
        if (index == null || index.isEmpty()) {
            return;
        }

        CompoundTag indexTag = new CompoundTag();
        for (Map.Entry<Long, UUID> entry : index.entrySet()) {
            // NBT Compound key 只能是 String，因此把 BlockPos.asLong() 转为十进制字符串。
            indexTag.putString(Long.toString(entry.getKey()), entry.getValue().toString());
        }
        root.put(tagName, indexTag);
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

    private static long chunkPosToLong(int chunkX, int chunkZ) {
        long chunkXLong = (long) chunkX & CHUNK_MASK;
        long chunkZLong = (long) chunkZ & CHUNK_MASK;
        return chunkXLong | (chunkZLong << HIGH_BITS_SHIFT);
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
     * 获取基地的 Anchor 缓存。
     *
     * @param bastionId 基地 ID
     * @return Anchor 位置集合（可能为空）
     */
    public java.util.Set<BlockPos> getAnchorCache(UUID bastionId) {
        return anchorCache.getOrDefault(bastionId, java.util.Collections.emptySet());
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
     * 如果存在，移除指定坐标的节点归属缓存。
     * <p>
     * 用于方块被破坏时清理运行时缓存，避免悬空引用。
     * </p>
     *
     * @param pos 节点位置
     */
    public void removeNodeOwnershipIfPresent(BlockPos pos) {
        if (pos == null) {
            return;
        }
        for (UUID bastionId : nodeCache.keySet()) {
            removeNodeFromCache(bastionId, pos);
        }
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
        anchorCache.remove(bastionId);
        lanternCache.remove(bastionId);
        nextAnchorTryTick.remove(bastionId);

        // Round 4.2：清理孵化巢冷却缓存，避免内存泄露。
        nextHatcheryTryTick.remove(bastionId);
        nextTurretTryTick.remove(bastionId);

        // 回合2：清理运行时连通性/衰败状态，避免内存泄露。
        connectivityRuntimes.remove(bastionId);
        myceliumDecayTicks.remove(bastionId);

        // 回合3：清理能源挂载运行时缓存，避免内存泄露。
        anchorEnergyTypes.remove(bastionId);

        // 回合2.1.1：清理持久化 owner 索引中的“悬空引用”，避免后续查询命中已删除基地。
        // 这里按 value 扫描移除即可：基地删除是低频操作，允许 O(N) 清理。
        territory.removeTerritoryByBastionId(bastionId);
        removeOwnerIndexEntriesByBastionId(anchorOwnerIndex, bastionId);
    }

    private void removeOwnerIndexEntriesByBastionId(Map<Long, UUID> index, UUID bastionId) {
        if (index == null || index.isEmpty() || bastionId == null) {
            return;
        }
        boolean changed = index.entrySet().removeIf(e -> bastionId.equals(e.getValue()));
        if (changed) {
            setDirty();
        }
    }

    /**
     * 将镇地灯加入运行时缓存。
     * <p>
     * 说明：该缓存仅用于扩张候选过滤，不持久化。
     * </p>
     *
     * @param bastionId 镇地灯归属基地 ID
     * @param pos       镇地灯坐标
     */
    public void addLanternToCache(UUID bastionId, BlockPos pos) {
        if (bastionId == null || pos == null) {
            return;
        }
        Map<Long, java.util.Set<BlockPos>> byChunk =
            lanternCache.computeIfAbsent(bastionId, k -> new HashMap<>());
        long chunkKey = chunkPosToLong(pos);
        byChunk.computeIfAbsent(chunkKey, k -> new java.util.HashSet<>()).add(pos.immutable());
    }

    /**
     * 将镇地灯从运行时缓存移除。
     *
     * @param bastionId 镇地灯归属基地 ID
     * @param pos       镇地灯坐标
     */
    public void removeLanternFromCache(UUID bastionId, BlockPos pos) {
        if (bastionId == null || pos == null) {
            return;
        }
        Map<Long, java.util.Set<BlockPos>> byChunk = lanternCache.get(bastionId);
        if (byChunk == null || byChunk.isEmpty()) {
            return;
        }

        long chunkKey = chunkPosToLong(pos);
        java.util.Set<BlockPos> lanterns = byChunk.get(chunkKey);
        if (lanterns != null) {
            lanterns.remove(pos);
            if (lanterns.isEmpty()) {
                byChunk.remove(chunkKey);
            }
        }
        if (byChunk.isEmpty()) {
            lanternCache.remove(bastionId);
        }
    }

    /**
     * 检查目标位置是否会被基地镇地灯保护。
     * <p>
     * 只检查目标位置所在 chunk 及其 3x3 相邻 chunk 内的灯笼；
     * 保护半径固定 6 格（distSqr <= 36）。
     * </p>
     * <p>
     * 当基地资源不足 1.0 时，视为无法触发镇地灯保护并返回 false。
     * 资源扣减由调用方在“成功拦截扩张”后统一处理。
     * </p>
     *
     * @param bastionId 基地 ID
     * @param targetPos 目标扩张坐标
     * @return true 表示存在可生效的镇地灯保护
     */
    public boolean isProtectedByLantern(UUID bastionId, BlockPos targetPos) {
        if (bastionId == null || targetPos == null) {
            return false;
        }

        BastionData bastion = bastions.get(bastionId);
        if (bastion == null
                || bastion.state() == BastionState.DESTROYED
                || bastion.resourcePool() < LANTERN_MIN_RESOURCE) {
            return false;
        }

        Map<Long, java.util.Set<BlockPos>> byChunk = lanternCache.get(bastionId);
        if (byChunk == null || byChunk.isEmpty()) {
            return false;
        }

        int baseChunkX = targetPos.getX() >> CHUNK_SHIFT;
        int baseChunkZ = targetPos.getZ() >> CHUNK_SHIFT;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                long chunkKey = chunkPosToLong(baseChunkX + offsetX, baseChunkZ + offsetZ);
                java.util.Set<BlockPos> lanterns = byChunk.get(chunkKey);
                if (lanterns == null || lanterns.isEmpty()) {
                    continue;
                }
                for (BlockPos lanternPos : lanterns) {
                    if (lanternPos.distSqr(targetPos) <= LANTERN_PROTECTION_RADIUS_SQR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ===== 回合2：连通性/衰败 API（运行时） =====

    /**
     * Anchor 是否在运行时缓存中。
     * <p>
     * 用途：连通性扫描将 Anchor 视为网络节点/潜在源之一。
     * </p>
     */
    public boolean isAnchorInCache(UUID bastionId, BlockPos pos) {
        java.util.Set<BlockPos> anchors = anchorCache.get(bastionId);
        return anchors != null && anchors.contains(pos);
    }

    /**
     * 获取或创建连通性运行时状态。
     */
    public ConnectivityRuntime getOrCreateConnectivityRuntime(UUID bastionId, BlockPos corePos) {
        ConnectivityRuntime runtime = connectivityRuntimes.get(bastionId);
        if (runtime != null) {
            return runtime;
        }
        ConnectivityRuntime created = new ConnectivityRuntime(corePos);
        connectivityRuntimes.put(bastionId, created);
        return created;
    }

    /**
     * 获取或创建菌毯衰败倒计时映射。
     */
    public Map<BlockPos, Integer> getOrCreateMyceliumDecayMap(UUID bastionId) {
        return myceliumDecayTicks.computeIfAbsent(bastionId, k -> new HashMap<>());
    }


    /**
     * 获取或创建区块级衰败倒计时映射。
     */
    public Map<Long, Integer> getOrCreateChunkDecayMap(UUID bastionId) {
        return chunkDecayTicks.computeIfAbsent(bastionId, k -> new HashMap<>());
    }

    /**
     * 清理某个位置的菌毯衰败记录（用于方块移除兜底）。
     */
    public void clearMyceliumDecay(UUID bastionId, BlockPos pos) {
        Map<BlockPos, Integer> map = myceliumDecayTicks.get(bastionId);
        if (map != null) {
            map.remove(pos);
        }
    }


    /**
     * 清理某个区块的衰败记录（用于区块恢复/移除时兜底）。
     */
    public void clearChunkDecay(UUID bastionId, long chunkKey) {
        Map<Long, Integer> map = chunkDecayTicks.get(bastionId);
        if (map != null) {
            map.remove(chunkKey);
        }
    }

    // ===== 回合3：能源挂载 API（运行时） =====

    /**
     * 获取或创建某个基地的 Anchor 能源挂载映射。
     * <p>
     * 注意：该结构仅为运行时缓存，不写入 NBT，不会触发 setDirty。
     * </p>
     */
    public Map<BlockPos, BastionEnergyType> getOrCreateAnchorEnergyMap(UUID bastionId) {
        return anchorEnergyTypes.computeIfAbsent(bastionId, k -> new HashMap<>());
    }

    /**
     * 清理某个 Anchor 位置的能源挂载记录（用于方块移除/失效兜底）。
     */
    public void clearAnchorEnergy(UUID bastionId, BlockPos pos) {
        Map<BlockPos, BastionEnergyType> map = anchorEnergyTypes.get(bastionId);
        if (map != null) {
            map.remove(pos);
        }
    }

    /**
     * 获取某个 Anchor 位置当前缓存的能源类型。
     * <p>
     * 返回 null 表示尚未缓存/已失效，需要由外部扫描或重建逻辑填充。
     * </p>
     */
    @Nullable
    public BastionEnergyType getAnchorEnergyType(UUID bastionId, BlockPos pos) {
        Map<BlockPos, BastionEnergyType> map = anchorEnergyTypes.get(bastionId);
        return map == null ? null : map.get(pos);
    }

    /**
     * 连通性扫描运行时状态（不持久化）。
     */
    public static final class ConnectivityRuntime {

        /** 当前扫描的核心位置（用于在基地搬迁/重建时刷新）。 */
        private BlockPos corePos;

        /** 扫描队列（BFS）。 */
        private final java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        /** 当前扫描已访问集合。 */
        private final java.util.Set<BlockPos> visited = new java.util.HashSet<>();

        /** 最近一次扫描得到的“可达节点集合”。 */
        private java.util.Set<BlockPos> lastReachable = java.util.Collections.emptySet();

        /** 下一次允许启动扫描的游戏时间。 */
        private long nextScanGameTime = 0L;
        /** 是否正在扫描。 */
        private boolean scanning = false;
        /** 本次扫描是否刚刚结束（用于一次性触发后处理）。 */
        private boolean scanJustFinished = false;

        /** 步长：X/Z 方向。 */
        private int spacingXZ = 2;
        /** 步长：Y 方向。 */
        private int spacingY = 1;

        private ConnectivityRuntime(BlockPos corePos) {
            this.corePos = corePos;
        }

        public boolean isScanning() {
            return scanning;
        }

        public long getNextScanGameTime() {
            return nextScanGameTime;
        }

        public void setNextScanGameTime(long nextScanGameTime) {
            this.nextScanGameTime = nextScanGameTime;
        }

        public boolean isScanJustFinished() {
            return scanJustFinished;
        }

        public void clearScanJustFinished() {
            this.scanJustFinished = false;
        }

        public java.util.Set<BlockPos> getLastReachableNodes() {
            return lastReachable;
        }

        public boolean isVisited(BlockPos pos) {
            return visited.contains(pos);
        }

        public void markVisited(BlockPos pos) {
            visited.add(pos);
        }

        public void offer(BlockPos pos) {
            queue.addLast(pos);
        }

        public BlockPos pollNext() {
            return queue.pollFirst();
        }

        public int getStepForAxis(net.minecraft.core.Direction.Axis axis) {
            return axis == net.minecraft.core.Direction.Axis.Y ? spacingY : spacingXZ;
        }

        /**
         * 启动一次新的扫描。
         */
        public void startScan() {
            this.queue.clear();
            this.visited.clear();
            this.scanning = true;
            this.scanJustFinished = false;
        }

        /**
         * 增加一个 BFS 源点。
         */
        public void addSource(BlockPos pos) {
            if (pos == null) {
                return;
            }
            if (visited.add(pos)) {
                queue.addLast(pos);
            }
        }

        /**
         * 完成本次扫描，生成 lastReachable。
         */
        public void finishScan() {
            this.scanning = false;
            this.scanJustFinished = true;

            // lastReachable 只存不可变快照，避免外部误改。
            this.lastReachable = java.util.Set.copyOf(this.visited);

            // 扫描结束后清空临时结构，降低常驻内存。
            this.queue.clear();
            this.visited.clear();
        }

        /**
         * 更新步长（由外部根据配置驱动）。
         */
        public void updateSpacing(int spacingXZ, int spacingY) {
            this.spacingXZ = Math.max(1, spacingXZ);
            this.spacingY = Math.max(1, spacingY);
        }

        /**
         * 更新核心位置（允许在基地重建/迁移时刷新）。
         */
        public void updateCorePos(BlockPos corePos) {
            if (corePos != null) {
                this.corePos = corePos;
            }
        }
    }

    // ===== Anchor 缓存 API（运行时） =====

    public boolean hasAnchorCache(UUID bastionId) {
        java.util.Set<BlockPos> anchors = anchorCache.get(bastionId);
        return anchors != null && !anchors.isEmpty();
    }

    public void initializeAnchorCacheFromCore(UUID bastionId, BlockPos corePos) {
        // 核心不算 Anchor，但作为“已初始化”标记可用。
        anchorCache.computeIfAbsent(bastionId, k -> new java.util.HashSet<>());
    }

    public java.util.Set<BlockPos> getAnchors(UUID bastionId) {
        return anchorCache.getOrDefault(bastionId, java.util.Collections.emptySet());
    }

    public void addAnchorToCache(UUID bastionId, BlockPos pos) {
        anchorCache.computeIfAbsent(bastionId, k -> new java.util.HashSet<>()).add(pos);
    }

    public long getNextAnchorTryTick(UUID bastionId) {
        return nextAnchorTryTick.getOrDefault(bastionId, 0L);
    }

    public void setNextAnchorTryTick(UUID bastionId, long nextTick) {
        nextAnchorTryTick.put(bastionId, nextTick);
    }

    // ===== Round 4.2：孵化巢冷却 API（运行时） =====

    public long getNextHatcheryTryTick(UUID bastionId) {
        return nextHatcheryTryTick.getOrDefault(bastionId, 0L);
    }

    public void setNextHatcheryTryTick(UUID bastionId, long nextTick) {
        nextHatcheryTryTick.put(bastionId, nextTick);
    }

    /**
     * 获取炮台下一次允许攻击的时间（基地级）。
     */
    public long getNextTurretTryTick(UUID bastionId) {
        return nextTurretTryTick.getOrDefault(bastionId, 0L);
    }

    /**
     * 设置炮台下一次允许攻击的时间（基地级）。
     */
    public void setNextTurretTryTick(UUID bastionId, long nextTick) {
        nextTurretTryTick.put(bastionId, nextTick);
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
