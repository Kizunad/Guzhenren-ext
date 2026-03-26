package com.Kizunad.guzhenrenext.xianqiao.data;

import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 仙窍维度全局数据。
 * <p>
 * 本数据对象负责：
 * 1) 记录玩家 UUID 与其仙窍信息映射；
 * 2) 维护 nextIndex 用于分配新的仙窍坐标；
 * 3) 持久化到世界存档。
 * </p>
 */
public class ApertureWorldData extends SavedData {

    private static final String DATA_NAME = "guzhenrenext_aperture_world";

    private static final String KEY_NEXT_INDEX = "nextIndex";

    private static final String KEY_SCHEMA_VERSION = "schemaVersion";

    private static final String KEY_APERTURES = "apertures";

    private static final String KEY_APERTURE_INIT_STATES = "apertureInitStates";

    private static final String KEY_INITIALIZED_APERTURES = "initializedApertures";

    private static final String KEY_INIT_STATE = "initState";

    private static final String KEY_INIT_PHASE = "initPhase";

    private static final String KEY_OPENING_SNAPSHOT = "openingSnapshot";

    private static final String KEY_LAYOUT_VERSION = "layoutVersion";

    private static final String KEY_PLAN_SEED = "planSeed";

    private static final String KEY_LAST_LOGOUT_TIMES = "lastLogoutTimes";

    private static final String KEY_RETURN_POSITIONS = "returnPositions";

    private static final String KEY_OWNER = "owner";

    private static final String KEY_INFO = "info";

    private static final String KEY_LAST_LOGOUT_TIME = "lastLogoutTime";

    private static final String KEY_DIMENSION_KEY = "dimensionKey";

    private static final String KEY_X = "x";

    private static final String KEY_Y = "y";

    private static final String KEY_Z = "z";

    private static final String KEY_Y_ROT = "yRot";

    private static final String KEY_X_ROT = "xRot";

    private static final int TAG_COMPOUND = Tag.TAG_COMPOUND;

    private static final int TAG_LIST = Tag.TAG_LIST;

    private static final int DEFAULT_NEXT_INDEX = 1;

    private static final int CURRENT_SCHEMA_VERSION = 1;

    private static final int APERTURE_SPACING = 100000;

    private static final int DEFAULT_CENTER_Y = 64;

    private static final int CHUNK_BLOCK_SIZE = 16;

    private static final int DEFAULT_INITIAL_RANGE_CHUNKS = 4;

    private static final float DEFAULT_TIME_SPEED = 1.0F;

    private static final float MIN_TIME_SPEED = 0.05F;

    private static final int DAY_TICKS = 24000;

    private static final int TRIBULATION_CYCLE_DAYS = 20;

    private static final long DEFAULT_TRIBULATION_CYCLE = (long) DAY_TICKS * TRIBULATION_CYCLE_DAYS;

    private static final float DEFAULT_FAVORABILITY = 0.0F;

    private static final float MIN_FAVORABILITY = 0.0F;

    private static final float MAX_FAVORABILITY = 100.0F;

    private static final int DEFAULT_TIER = 1;

    private static final long UNRECORDED_LOGOUT_TIME = -1L;

    private int schemaVersion = CURRENT_SCHEMA_VERSION;

    private int nextIndex = DEFAULT_NEXT_INDEX;

    private final Map<UUID, ApertureInfo> apertures = new HashMap<>();

    private final Map<UUID, ApertureInitializationState> initializationStates = new HashMap<>();

    private final Set<UUID> initializedApertures = new HashSet<>();

    private final Map<UUID, Long> lastLogoutTimes = new HashMap<>();

    private final Map<UUID, ReturnPosition> returnPositions = new HashMap<>();

    /**
     * 分配一个新的仙窍信息；若已有记录，则直接返回既有信息。
     *
     * @param owner 玩家 UUID
     * @return 仙窍信息
     */
    public ApertureInfo allocateAperture(UUID owner) {
        ApertureInfo existing = apertures.get(owner);
        if (existing != null) {
            return existing;
        }

        int centerX = nextIndex * APERTURE_SPACING;
        nextIndex++;
        BlockPos centerPos = new BlockPos(centerX, DEFAULT_CENTER_Y, 0);
        int centerChunkX = Math.floorDiv(centerPos.getX(), CHUNK_BLOCK_SIZE);
        int centerChunkZ = Math.floorDiv(centerPos.getZ(), CHUNK_BLOCK_SIZE);

        ApertureInfo created = new ApertureInfo(
            centerPos,
            centerChunkX - DEFAULT_INITIAL_RANGE_CHUNKS,
            centerChunkX + DEFAULT_INITIAL_RANGE_CHUNKS,
            centerChunkZ - DEFAULT_INITIAL_RANGE_CHUNKS,
            centerChunkZ + DEFAULT_INITIAL_RANGE_CHUNKS,
            DEFAULT_TIME_SPEED,
            DEFAULT_TRIBULATION_CYCLE,
            false,
            DEFAULT_FAVORABILITY,
            DEFAULT_TIER
        );
        apertures.put(owner, created);
        initializationStates.putIfAbsent(owner, ApertureInitializationState.uninitialized());
        setDirty();
        return created;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * 获取指定玩家仙窍信息。
     *
     * @param owner 玩家 UUID
     * @return 仙窍信息，不存在则返回 null
     */
    @Nullable
    public ApertureInfo getAperture(UUID owner) {
        return apertures.get(owner);
    }

    /**
     * 获取或分配仙窍信息。
     *
     * @param owner 玩家 UUID
     * @return 已存在或新分配的仙窍信息
     */
    public ApertureInfo getOrAllocate(UUID owner) {
        ApertureInfo existing = apertures.get(owner);
        if (existing != null) {
            return existing;
        }
        return allocateAperture(owner);
    }

    /**
     * 按“旧半径语义”重算仙窍 chunk 边界（过渡 API）。
     * <p>
     * 该方法仅用于业务层迁移期间兼容旧调用方：
     * 外部仍以“中心点 + 方块半径”思考时，数据层会将其转换为 chunk 边界并作为最终真源存储。
     * 术语契约：range = chunk range；边界 = min/max chunk 闭区间。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newRadius 新半径（方块）
     */
    public void updateBoundaryByRadius(UUID owner, int newRadius) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        int normalizedRadius = Math.max(0, newRadius);
        int chunkRadius = (normalizedRadius + CHUNK_BLOCK_SIZE - 1) / CHUNK_BLOCK_SIZE;
        int centerChunkX = Math.floorDiv(existing.center().getX(), CHUNK_BLOCK_SIZE);
        int centerChunkZ = Math.floorDiv(existing.center().getZ(), CHUNK_BLOCK_SIZE);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            centerChunkX - chunkRadius,
            centerChunkX + chunkRadius,
            centerChunkZ - chunkRadius,
            centerChunkZ + chunkRadius,
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 旧半径更新入口（兼容过渡，非真源）。
     * <p>
     * 仅用于尚未迁移完成的旧调用方；业务判定与存储真源均应使用 chunk 边界语义。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newRadius 新半径（方块）
     */
    public void updateRadius(UUID owner, int newRadius) {
        updateBoundaryByRadius(owner, newRadius);
    }

    /**
     * 按 chunk 增量向四周扩展仙窍边界。
     * <p>
     * 该方法直接操作 min/max chunk 边界，是新的主数据 API。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param chunkIncrement 每个方向扩展的 chunk 数（必须大于 0）
     */
    public void expandBoundaryByChunkDelta(UUID owner, int chunkIncrement) {
        if (chunkIncrement <= 0) {
            return;
        }
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX() - chunkIncrement,
            existing.maxChunkX() + chunkIncrement,
            existing.minChunkZ() - chunkIncrement,
            existing.maxChunkZ() + chunkIncrement,
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    public void expandBoundaryByChunks(UUID owner, int chunkIncrement) {
        expandBoundaryByChunkDelta(owner, chunkIncrement);
    }

    /**
     * 更新指定玩家仙窍中心坐标。
     * <p>
     * 由于 {@link ApertureInfo} 为不可变 record，更新时需要构造新实例并替换映射。
     * 该方法用于在初始化阶段把核心 Y 对齐到采样后地形高度，避免传送点与真实核心位置不一致。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newCenter 新中心坐标
     */
    public void updateCenter(UUID owner, BlockPos newCenter) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            newCenter,
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 更新指定玩家仙窍好感度。
     * <p>
     * 由于 {@link ApertureInfo} 为不可变 record，更新时需要构造新实例并替换映射。
     * 新值会被限制在 [{@value #MIN_FAVORABILITY}, {@value #MAX_FAVORABILITY}] 区间内，
     * 以避免脏数据破坏后续逻辑。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newFavorability 新好感度
     */
    public void updateFavorability(UUID owner, float newFavorability) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        float clampedFavorability = clampFavorability(newFavorability);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            clampedFavorability,
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    public void updateTimeSpeed(UUID owner, float newTimeSpeed) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        float normalizedTimeSpeed = Math.max(MIN_TIME_SPEED, newTimeSpeed);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            normalizedTimeSpeed,
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    public void updateFrozen(UUID owner, boolean frozen) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            frozen,
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 更新指定玩家仙窍层级。
     * <p>
     * 由于 {@link ApertureInfo} 为不可变 record，更新时需要构造新实例并替换映射。
     * 层级最低为 {@value #DEFAULT_TIER}，可防止外部输入非法值导致数据越界。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param newTier 新层级
     */
    public void updateTier(UUID owner, int newTier) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        int normalizedTier = normalizeTier(newTier);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            normalizedTier
        );
        apertures.put(owner, updated);
        setDirty();
    }

    public boolean isApertureInitialized(UUID owner) {
        return getInitializationState(owner).initPhase().isInitialized();
    }

    public ApertureInitializationState getInitializationState(UUID owner) {
        ApertureInitializationState storedState = initializationStates.get(owner);
        if (storedState != null) {
            return storedState;
        }
        if (initializedApertures.contains(owner)) {
            return ApertureInitializationState.legacyCompleted();
        }
        return ApertureInitializationState.uninitialized();
    }

    public void setInitializationState(UUID owner, ApertureInitializationState state) {
        if (owner == null) {
            throw new IllegalArgumentException("owner 不能为空");
        }
        if (state == null) {
            throw new IllegalArgumentException("state 不能为空");
        }
        ApertureInitializationState previousState = initializationStates.put(owner, state);
        boolean changed = !state.equals(previousState);
        if (syncLegacyInitializedProjection(owner, state)) {
            changed = true;
        }
        if (changed) {
            setDirty();
        }
    }

    public InitPhase getInitPhase(UUID owner) {
        return getInitializationState(owner).initPhase();
    }

    @Nullable
    public AscensionConditionSnapshot getOpeningSnapshot(UUID owner) {
        return getInitializationState(owner).openingSnapshot();
    }

    @Nullable
    public Integer getLayoutVersion(UUID owner) {
        return getInitializationState(owner).layoutVersion();
    }

    @Nullable
    public Long getPlanSeed(UUID owner) {
        return getInitializationState(owner).planSeed();
    }

    /**
     * 将指定玩家仙窍标记为已初始化。
     *
     * @param owner 玩家 UUID
     */
    public void markApertureInitialized(UUID owner) {
        ApertureInitializationState currentState = getInitializationState(owner);
        setInitializationState(owner, currentState.withPhase(InitPhase.COMPLETED));
    }

    /**
     * 设置玩家最后离线时间（以维度 gameTime 计）。
     *
     * @param owner 玩家 UUID
     * @param gameTime 离线时游戏刻
     */
    public void setLastLogoutTime(UUID owner, long gameTime) {
        Long previous = lastLogoutTimes.put(owner, gameTime);
        if (previous == null || previous.longValue() != gameTime) {
            setDirty();
        }
    }

    /**
     * 读取玩家最后离线时间。
     *
     * @param owner 玩家 UUID
     * @return 最后离线刻；若无记录返回 {@value #UNRECORDED_LOGOUT_TIME}
     */
    public long getLastLogoutTime(UUID owner) {
        return lastLogoutTimes.getOrDefault(owner, UNRECORDED_LOGOUT_TIME);
    }

    public void setReturnPosition(UUID owner, ReturnPosition position) {
        ReturnPosition previous = returnPositions.put(owner, position);
        if (!position.equals(previous)) {
            setDirty();
        }
    }

    @Nullable
    public ReturnPosition getReturnPosition(UUID owner) {
        return returnPositions.get(owner);
    }

    /**
     * 更新指定玩家仙窍的下次灾劫触发刻（绝对游戏时间）。
     *
     * @param owner 玩家 UUID
     * @param nextTick 下次灾劫触发刻
     */
    public void updateTribulationTick(UUID owner, long nextTick) {
        if (nextTick < 0L) {
            return;
        }
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            existing.timeSpeed(),
            nextTick,
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 按离线时长扣减灾劫剩余时间（向下不低于 0）。
     * <p>
     * 该方法用于兼容离线聚合结算模型：
     * 当存档中的 {@code nextTribulationTick} 仍被作为“剩余刻”使用时，
     * 可通过此方法统一扣减。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param elapsedTicks 流逝 tick
     */
    public void reduceTribulationTick(UUID owner, long elapsedTicks) {
        if (elapsedTicks <= 0L) {
            return;
        }
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        long updatedTribulationTick = Math.max(0L, existing.nextTribulationTick() - elapsedTicks);
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            existing.minChunkX(),
            existing.maxChunkX(),
            existing.minChunkZ(),
            existing.maxChunkZ(),
            existing.timeSpeed(),
            updatedTribulationTick,
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        apertures.put(owner, updated);
        setDirty();
    }

    /**
     * 归一化层级值，确保层级至少为 1。
     *
     * @param tier 原始层级
     * @return 归一化后的合法层级
     */
    private static int normalizeTier(int tier) {
        return Math.max(DEFAULT_TIER, tier);
    }

    /**
     * 限制好感度区间，确保数值位于 [0, 100]。
     *
     * @param favorability 原始好感度
     * @return 限幅后的好感度
     */
    private static float clampFavorability(float favorability) {
        return Math.max(MIN_FAVORABILITY, Math.min(MAX_FAVORABILITY, favorability));
    }

    /**
     * 获取全部仙窍数据快照。
     *
     * @return 不可变快照（key 为 owner UUID，value 为仙窍信息）
     */
    public Map<UUID, ApertureInfo> getAllApertures() {
        return Collections.unmodifiableMap(new HashMap<>(apertures));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        schemaVersion = CURRENT_SCHEMA_VERSION;
        tag.putInt(KEY_SCHEMA_VERSION, schemaVersion);
        tag.putInt(KEY_NEXT_INDEX, nextIndex);

        ListTag list = new ListTag();
        for (Map.Entry<UUID, ApertureInfo> entry : apertures.entrySet()) {
            CompoundTag apertureTag = new CompoundTag();
            apertureTag.putUUID(KEY_OWNER, entry.getKey());
            apertureTag.put(KEY_INFO, entry.getValue().save());
            list.add(apertureTag);
        }
        tag.put(KEY_APERTURES, list);

        resyncLegacyInitializedProjection();

        ListTag initStateList = new ListTag();
        Set<UUID> persistedInitOwners = new HashSet<>(apertures.keySet());
        persistedInitOwners.addAll(initializationStates.keySet());
        for (UUID owner : persistedInitOwners) {
            CompoundTag initStateTag = new CompoundTag();
            initStateTag.putUUID(KEY_OWNER, owner);
            initStateTag.put(KEY_INIT_STATE, getInitializationState(owner).save());
            initStateList.add(initStateTag);
        }
        tag.put(KEY_APERTURE_INIT_STATES, initStateList);

        ListTag initializedList = new ListTag();
        for (UUID owner : initializedApertures) {
            CompoundTag initializedTag = new CompoundTag();
            initializedTag.putUUID(KEY_OWNER, owner);
            initializedList.add(initializedTag);
        }
        tag.put(KEY_INITIALIZED_APERTURES, initializedList);

        ListTag logoutList = new ListTag();
        for (Map.Entry<UUID, Long> entry : lastLogoutTimes.entrySet()) {
            CompoundTag logoutTag = new CompoundTag();
            logoutTag.putUUID(KEY_OWNER, entry.getKey());
            logoutTag.putLong(KEY_LAST_LOGOUT_TIME, entry.getValue());
            logoutList.add(logoutTag);
        }
        tag.put(KEY_LAST_LOGOUT_TIMES, logoutList);

        ListTag returnPositionList = new ListTag();
        for (Map.Entry<UUID, ReturnPosition> entry : returnPositions.entrySet()) {
            CompoundTag returnTag = new CompoundTag();
            returnTag.putUUID(KEY_OWNER, entry.getKey());
            returnTag.put(KEY_INFO, entry.getValue().save());
            returnPositionList.add(returnTag);
        }
        tag.put(KEY_RETURN_POSITIONS, returnPositionList);
        return tag;
    }

    static ApertureWorldData loadFromTag(CompoundTag tag) {
        ApertureWorldData data = new ApertureWorldData();
        data.schemaVersion = CURRENT_SCHEMA_VERSION;
        data.nextIndex = Math.max(DEFAULT_NEXT_INDEX, tag.getInt(KEY_NEXT_INDEX));

        if (tag.contains(KEY_APERTURES, TAG_LIST)) {
            ListTag list = tag.getList(KEY_APERTURES, TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag apertureTag = list.getCompound(i);
                if (!apertureTag.hasUUID(KEY_OWNER) || !apertureTag.contains(KEY_INFO, TAG_COMPOUND)) {
                    continue;
                }
                UUID owner = apertureTag.getUUID(KEY_OWNER);
                CompoundTag infoTag = apertureTag.getCompound(KEY_INFO);
                data.apertures.put(owner, ApertureInfo.load(infoTag));
            }
        }

        if (tag.contains(KEY_APERTURE_INIT_STATES, TAG_LIST)) {
            ListTag initStateList = tag.getList(KEY_APERTURE_INIT_STATES, TAG_COMPOUND);
            for (int i = 0; i < initStateList.size(); i++) {
                CompoundTag initStateEntryTag = initStateList.getCompound(i);
                if (
                    !initStateEntryTag.hasUUID(KEY_OWNER)
                        || !initStateEntryTag.contains(KEY_INIT_STATE, TAG_COMPOUND)
                ) {
                    continue;
                }
                UUID owner = initStateEntryTag.getUUID(KEY_OWNER);
                ApertureInitializationState state = ApertureInitializationState.load(
                    initStateEntryTag.getCompound(KEY_INIT_STATE)
                );
                data.initializationStates.put(owner, state);
            }
        }

        if (tag.contains(KEY_INITIALIZED_APERTURES, TAG_LIST)) {
            ListTag initializedList = tag.getList(KEY_INITIALIZED_APERTURES, TAG_COMPOUND);
            for (int i = 0; i < initializedList.size(); i++) {
                CompoundTag initializedTag = initializedList.getCompound(i);
                if (!initializedTag.hasUUID(KEY_OWNER)) {
                    continue;
                }
                data.initializedApertures.add(initializedTag.getUUID(KEY_OWNER));
            }
        }

        if (tag.contains(KEY_LAST_LOGOUT_TIMES, TAG_LIST)) {
            ListTag logoutList = tag.getList(KEY_LAST_LOGOUT_TIMES, TAG_COMPOUND);
            for (int i = 0; i < logoutList.size(); i++) {
                CompoundTag logoutTag = logoutList.getCompound(i);
                if (!logoutTag.hasUUID(KEY_OWNER) || !logoutTag.contains(KEY_LAST_LOGOUT_TIME)) {
                    continue;
                }
                UUID owner = logoutTag.getUUID(KEY_OWNER);
                long logoutTick = logoutTag.getLong(KEY_LAST_LOGOUT_TIME);
                data.lastLogoutTimes.put(owner, logoutTick);
            }
        }

        if (tag.contains(KEY_RETURN_POSITIONS, TAG_LIST)) {
            ListTag returnPositionList = tag.getList(KEY_RETURN_POSITIONS, TAG_COMPOUND);
            for (int i = 0; i < returnPositionList.size(); i++) {
                CompoundTag returnTag = returnPositionList.getCompound(i);
                if (!returnTag.hasUUID(KEY_OWNER) || !returnTag.contains(KEY_INFO, TAG_COMPOUND)) {
                    continue;
                }
                UUID owner = returnTag.getUUID(KEY_OWNER);
                ReturnPosition position = ReturnPosition.load(returnTag.getCompound(KEY_INFO));
                if (position != null) {
                    data.returnPositions.put(owner, position);
                }
            }
        }
        data.migrateInitializationStates();
        return data;
    }

    private void migrateInitializationStates() {
        for (UUID owner : initializedApertures) {
            initializationStates.putIfAbsent(owner, ApertureInitializationState.legacyCompleted());
        }
        for (UUID owner : apertures.keySet()) {
            initializationStates.putIfAbsent(owner, ApertureInitializationState.uninitialized());
        }
        resyncLegacyInitializedProjection();
    }

    private boolean syncLegacyInitializedProjection(UUID owner, ApertureInitializationState state) {
        if (state.initPhase().isInitialized()) {
            return initializedApertures.add(owner);
        }
        return initializedApertures.remove(owner);
    }

    private void resyncLegacyInitializedProjection() {
        initializedApertures.clear();
        for (Map.Entry<UUID, ApertureInitializationState> entry : initializationStates.entrySet()) {
            if (entry.getValue().initPhase().isInitialized()) {
                initializedApertures.add(entry.getKey());
            }
        }
    }

    /**
     * 创建 SavedData 工厂。
     *
     * @return SavedData Factory
     */
    public static SavedData.Factory<ApertureWorldData> factory() {
        return new SavedData.Factory<>(ApertureWorldData::new, (tag, provider) -> loadFromTag(tag), null);
    }

    /**
     * 从指定维度读取或创建仙窍世界数据。
     *
     * @param level 服务器维度实例
     * @return 仙窍世界数据
     */
    public static ApertureWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public enum InitPhase {
        UNINITIALIZED,
        PLANNED,
        EXECUTING,
        COMPLETED;

        public boolean isInitialized() {
            return this == COMPLETED;
        }
    }

    public record ApertureInitializationState(
        InitPhase initPhase,
        @Nullable AscensionConditionSnapshot openingSnapshot,
        @Nullable Integer layoutVersion,
        @Nullable Long planSeed
    ) {

        public ApertureInitializationState {
            initPhase = Objects.requireNonNull(initPhase, "initPhase");
        }

        public static ApertureInitializationState uninitialized() {
            return new ApertureInitializationState(InitPhase.UNINITIALIZED, null, null, null);
        }

        public static ApertureInitializationState legacyCompleted() {
            return new ApertureInitializationState(InitPhase.COMPLETED, null, null, null);
        }

        public ApertureInitializationState withPhase(InitPhase newPhase) {
            return new ApertureInitializationState(newPhase, openingSnapshot, layoutVersion, planSeed);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(KEY_INIT_PHASE, initPhase.name());
            if (openingSnapshot != null) {
                tag.put(KEY_OPENING_SNAPSHOT, OpeningSnapshotTagCodec.save(openingSnapshot));
            }
            if (layoutVersion != null) {
                tag.putInt(KEY_LAYOUT_VERSION, layoutVersion.intValue());
            }
            if (planSeed != null) {
                tag.putLong(KEY_PLAN_SEED, planSeed.longValue());
            }
            return tag;
        }

        private static ApertureInitializationState load(CompoundTag tag) {
            InitPhase phase = parseEnumOrDefault(
                tag.getString(KEY_INIT_PHASE),
                InitPhase.class,
                InitPhase.UNINITIALIZED
            );
            AscensionConditionSnapshot snapshot = null;
            if (tag.contains(KEY_OPENING_SNAPSHOT, TAG_COMPOUND)) {
                snapshot = OpeningSnapshotTagCodec.load(tag.getCompound(KEY_OPENING_SNAPSHOT));
            }
            Integer storedLayoutVersion = tag.contains(KEY_LAYOUT_VERSION)
                ? Integer.valueOf(tag.getInt(KEY_LAYOUT_VERSION))
                : null;
            Long storedPlanSeed = tag.contains(KEY_PLAN_SEED)
                ? Long.valueOf(tag.getLong(KEY_PLAN_SEED))
                : null;
            return new ApertureInitializationState(phase, snapshot, storedLayoutVersion, storedPlanSeed);
        }
    }

    private static final class OpeningSnapshotTagCodec {

        private static final String KEY_BENMING_GU_RAW_VALUE = "benmingGuRawValue";

        private static final String KEY_BENMING_GU_FALLBACK_STATE = "benmingGuFallbackState";

        private static final String KEY_BENMING_GU_TOKEN = "benmingGuToken";

        private static final String KEY_DAO_MARKS = "daoMarks";

        private static final String KEY_DAO_MARK_COVERAGE_STATE = "daoMarkCoverageState";

        private static final String KEY_DAO_MARK_TOTAL_FROM_PLAYER = "daoMarkTotalFromPlayer";

        private static final String KEY_DAO_MARK_RESOLVED_TOTAL = "daoMarkResolvedTotal";

        private static final String KEY_APTITUDE_RESOURCE_STATE = "aptitudeResourceState";

        private static final String KEY_MAX_ZHENYUAN = "maxZhenyuan";

        private static final String KEY_SHOUYUAN = "shouyuan";

        private static final String KEY_JINGLI = "jingli";

        private static final String KEY_MAX_JINGLI = "maxJingli";

        private static final String KEY_HUNPO = "hunpo";

        private static final String KEY_MAX_HUNPO = "maxHunpo";

        private static final String KEY_TIZHI = "tizhi";

        private static final String KEY_ZHUANSHU = "zhuanshu";

        private static final String KEY_JIEDUAN = "jieduan";

        private static final String KEY_KONGQIAO = "kongqiao";

        private static final String KEY_QIYUN = "qiyun";

        private static final String KEY_QIYUN_MAX = "qiyunMax";

        private static final String KEY_RENQI = "renqi";

        private static final String KEY_FALLBACK_HUMAN_QI = "fallbackHumanQi";

        private static final String KEY_EARTH_QI = "earthQi";

        private static final String KEY_HUMAN_QI_TARGET = "humanQiTarget";

        private static final String KEY_EARTH_QI_TARGET = "earthQiTarget";

        private static final String KEY_PLAYER_INITIATED = "playerInitiated";

        private OpeningSnapshotTagCodec() {
        }

        private static CompoundTag save(AscensionConditionSnapshot snapshot) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble(KEY_BENMING_GU_RAW_VALUE, snapshot.benmingGuRawValue());
            tag.putString(KEY_BENMING_GU_FALLBACK_STATE, snapshot.benmingGuFallbackState().name());
            tag.putString(KEY_BENMING_GU_TOKEN, snapshot.benmingGuToken());
            tag.put(KEY_DAO_MARKS, saveDaoMarks(snapshot.daoMarks()));
            tag.putString(KEY_DAO_MARK_COVERAGE_STATE, snapshot.daoMarkCoverageState().name());
            tag.putDouble(KEY_DAO_MARK_TOTAL_FROM_PLAYER, snapshot.daoMarkTotalFromPlayer());
            tag.putDouble(KEY_DAO_MARK_RESOLVED_TOTAL, snapshot.daoMarkResolvedTotal());
            tag.putString(KEY_APTITUDE_RESOURCE_STATE, snapshot.aptitudeResourceState().name());
            tag.putDouble(KEY_MAX_ZHENYUAN, snapshot.maxZhenyuan());
            tag.putDouble(KEY_SHOUYUAN, snapshot.shouyuan());
            tag.putDouble(KEY_JINGLI, snapshot.jingli());
            tag.putDouble(KEY_MAX_JINGLI, snapshot.maxJingli());
            tag.putDouble(KEY_HUNPO, snapshot.hunpo());
            tag.putDouble(KEY_MAX_HUNPO, snapshot.maxHunpo());
            tag.putDouble(KEY_TIZHI, snapshot.tizhi());
            tag.putDouble(KEY_ZHUANSHU, snapshot.zhuanshu());
            tag.putDouble(KEY_JIEDUAN, snapshot.jieduan());
            tag.putDouble(KEY_KONGQIAO, snapshot.kongqiao());
            tag.putDouble(KEY_QIYUN, snapshot.qiyun());
            tag.putDouble(KEY_QIYUN_MAX, snapshot.qiyunMax());
            tag.putDouble(KEY_RENQI, snapshot.renqi());
            tag.putDouble(KEY_FALLBACK_HUMAN_QI, snapshot.fallbackHumanQi());
            tag.putDouble(KEY_EARTH_QI, snapshot.earthQi());
            tag.putDouble(KEY_HUMAN_QI_TARGET, snapshot.humanQiTarget());
            tag.putDouble(KEY_EARTH_QI_TARGET, snapshot.earthQiTarget());
            tag.putBoolean(KEY_PLAYER_INITIATED, snapshot.playerInitiated());
            return tag;
        }

        private static CompoundTag saveDaoMarks(Map<String, Double> daoMarks) {
            CompoundTag daoMarksTag = new CompoundTag();
            TreeMap<String, Double> orderedDaoMarks = new TreeMap<>(daoMarks);
            for (Map.Entry<String, Double> entry : orderedDaoMarks.entrySet()) {
                daoMarksTag.putDouble(entry.getKey(), entry.getValue());
            }
            return daoMarksTag;
        }

        private static AscensionConditionSnapshot load(CompoundTag tag) {
            CompoundTag daoMarksTag = tag.getCompound(KEY_DAO_MARKS);
            Map<String, Double> daoMarks = new HashMap<>();
            for (String key : daoMarksTag.getAllKeys()) {
                daoMarks.put(key, daoMarksTag.getDouble(key));
            }
            return new AscensionConditionSnapshot(
                tag.getDouble(KEY_BENMING_GU_RAW_VALUE),
                parseEnumOrDefault(
                    tag.getString(KEY_BENMING_GU_FALLBACK_STATE),
                    AscensionConditionSnapshot.BenmingGuFallbackState.class,
                    AscensionConditionSnapshot.BenmingGuFallbackState.UNKNOWN
                ),
                tag.contains(KEY_BENMING_GU_TOKEN) ? tag.getString(KEY_BENMING_GU_TOKEN) : "unknown",
                daoMarks,
                parseEnumOrDefault(
                    tag.getString(KEY_DAO_MARK_COVERAGE_STATE),
                    AscensionConditionSnapshot.DaoMarkCoverageState.class,
                    AscensionConditionSnapshot.DaoMarkCoverageState.MISSING
                ),
                tag.getDouble(KEY_DAO_MARK_TOTAL_FROM_PLAYER),
                tag.getDouble(KEY_DAO_MARK_RESOLVED_TOTAL),
                parseEnumOrDefault(
                    tag.getString(KEY_APTITUDE_RESOURCE_STATE),
                    AscensionConditionSnapshot.AptitudeResourceState.class,
                    AscensionConditionSnapshot.AptitudeResourceState.ALL_ZERO_OR_MISSING
                ),
                tag.getDouble(KEY_MAX_ZHENYUAN),
                tag.getDouble(KEY_SHOUYUAN),
                tag.getDouble(KEY_JINGLI),
                tag.getDouble(KEY_MAX_JINGLI),
                tag.getDouble(KEY_HUNPO),
                tag.getDouble(KEY_MAX_HUNPO),
                tag.getDouble(KEY_TIZHI),
                tag.getDouble(KEY_ZHUANSHU),
                tag.getDouble(KEY_JIEDUAN),
                tag.getDouble(KEY_KONGQIAO),
                tag.getDouble(KEY_QIYUN),
                tag.getDouble(KEY_QIYUN_MAX),
                tag.getDouble(KEY_RENQI),
                tag.getDouble(KEY_FALLBACK_HUMAN_QI),
                tag.getDouble(KEY_EARTH_QI),
                tag.getDouble(KEY_HUMAN_QI_TARGET),
                tag.getDouble(KEY_EARTH_QI_TARGET),
                tag.getBoolean(KEY_PLAYER_INITIATED)
            );
        }
    }

    private static <E extends Enum<E>> E parseEnumOrDefault(String storedName, Class<E> enumType, E fallback) {
        if (storedName == null || storedName.isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumType, storedName);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    /**
     * 单个仙窍记录。
     */
    public record ApertureInfo(
        BlockPos center,
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        float timeSpeed,
        long nextTribulationTick,
        boolean isFrozen,
        float favorability,
        int tier
    ) {

        private static final String KEY_CENTER_X = "centerX";

        private static final String KEY_CENTER_Y = "centerY";

        private static final String KEY_CENTER_Z = "centerZ";

        private static final String KEY_MIN_CHUNK_X = "minChunkX";

        private static final String KEY_MAX_CHUNK_X = "maxChunkX";

        private static final String KEY_MIN_CHUNK_Z = "minChunkZ";

        private static final String KEY_MAX_CHUNK_Z = "maxChunkZ";

        private static final String KEY_TIME_SPEED = "timeSpeed";

        private static final String KEY_NEXT_TRIBULATION_TICK = "nextTribulationTick";

        private static final String KEY_IS_FROZEN = "isFrozen";

        private static final String KEY_FAVORABILITY = "favorability";

        private static final String KEY_TIER = "tier";

        public ApertureInfo {
            int normalizedMinChunkX = Math.min(minChunkX, maxChunkX);
            int normalizedMaxChunkX = Math.max(minChunkX, maxChunkX);
            int normalizedMinChunkZ = Math.min(minChunkZ, maxChunkZ);
            int normalizedMaxChunkZ = Math.max(minChunkZ, maxChunkZ);
            minChunkX = normalizedMinChunkX;
            maxChunkX = normalizedMaxChunkX;
            minChunkZ = normalizedMinChunkZ;
            maxChunkZ = normalizedMaxChunkZ;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(KEY_CENTER_X, center.getX());
            tag.putInt(KEY_CENTER_Y, center.getY());
            tag.putInt(KEY_CENTER_Z, center.getZ());
            tag.putInt(KEY_MIN_CHUNK_X, minChunkX);
            tag.putInt(KEY_MAX_CHUNK_X, maxChunkX);
            tag.putInt(KEY_MIN_CHUNK_Z, minChunkZ);
            tag.putInt(KEY_MAX_CHUNK_Z, maxChunkZ);
            tag.putFloat(KEY_TIME_SPEED, timeSpeed);
            tag.putLong(KEY_NEXT_TRIBULATION_TICK, nextTribulationTick);
            tag.putBoolean(KEY_IS_FROZEN, isFrozen);
            tag.putFloat(KEY_FAVORABILITY, favorability);
            tag.putInt(KEY_TIER, tier);
            return tag;
        }

        /**
         * 读取当前仙窍“兼容半径”（方块）。
         * <p>
         * 兼容过渡，非真源：真实数据源已切换为 min/max chunk 闭区间边界。
         * 术语契约：range = chunk range；边界 = min/max chunk 闭区间。
         * 当调用方全部迁移到边界语义后，应移除该方法。
         * </p>
         *
         * @return 基于中心 chunk 到边界 chunk 估算得到的兼容半径（方块）
         */
        public int currentRadius() {
            int centerChunkX = Math.floorDiv(center.getX(), CHUNK_BLOCK_SIZE);
            int centerChunkZ = Math.floorDiv(center.getZ(), CHUNK_BLOCK_SIZE);
            int xHalfRange = Math.min(centerChunkX - minChunkX, maxChunkX - centerChunkX);
            int zHalfRange = Math.min(centerChunkZ - minChunkZ, maxChunkZ - centerChunkZ);
            int chunkHalfRange = Math.max(0, Math.min(xHalfRange, zHalfRange));
            return chunkHalfRange * CHUNK_BLOCK_SIZE;
        }

        public static ApertureInfo load(CompoundTag tag) {
            BlockPos centerPos = new BlockPos(
                tag.getInt(KEY_CENTER_X),
                tag.getInt(KEY_CENTER_Y),
                tag.getInt(KEY_CENTER_Z)
            );
            int storedMinChunkX = tag.getInt(KEY_MIN_CHUNK_X);
            int storedMaxChunkX = tag.getInt(KEY_MAX_CHUNK_X);
            int storedMinChunkZ = tag.getInt(KEY_MIN_CHUNK_Z);
            int storedMaxChunkZ = tag.getInt(KEY_MAX_CHUNK_Z);
            float speed = tag.getFloat(KEY_TIME_SPEED);
            long tribulationTick = tag.getLong(KEY_NEXT_TRIBULATION_TICK);
            boolean frozen = tag.getBoolean(KEY_IS_FROZEN);

            // 兼容旧存档：缺少新字段时回退到默认值。
            float storedFavorability = tag.contains(KEY_FAVORABILITY)
                ? tag.getFloat(KEY_FAVORABILITY)
                : DEFAULT_FAVORABILITY;
            float normalizedFavorability = clampFavorability(storedFavorability);

            int storedTier = tag.contains(KEY_TIER) ? tag.getInt(KEY_TIER) : DEFAULT_TIER;
            int normalizedTier = normalizeTier(storedTier);

            return new ApertureInfo(
                centerPos,
                storedMinChunkX,
                storedMaxChunkX,
                storedMinChunkZ,
                storedMaxChunkZ,
                speed,
                tribulationTick,
                frozen,
                normalizedFavorability,
                normalizedTier
            );
        }
    }

    public record ReturnPosition(String dimensionKey, double x, double y, double z, float yRot, float xRot) {

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(KEY_DIMENSION_KEY, dimensionKey);
            tag.putDouble(KEY_X, x);
            tag.putDouble(KEY_Y, y);
            tag.putDouble(KEY_Z, z);
            tag.putFloat(KEY_Y_ROT, yRot);
            tag.putFloat(KEY_X_ROT, xRot);
            return tag;
        }

        @Nullable
        public static ReturnPosition load(CompoundTag tag) {
            if (!tag.contains(KEY_DIMENSION_KEY) || !tag.contains(KEY_X) || !tag.contains(KEY_Y)
                || !tag.contains(KEY_Z) || !tag.contains(KEY_Y_ROT) || !tag.contains(KEY_X_ROT)) {
                return null;
            }
            String storedDimensionKey = tag.getString(KEY_DIMENSION_KEY);
            if (storedDimensionKey.isEmpty()) {
                return null;
            }
            return new ReturnPosition(
                storedDimensionKey,
                tag.getDouble(KEY_X),
                tag.getDouble(KEY_Y),
                tag.getDouble(KEY_Z),
                tag.getFloat(KEY_Y_ROT),
                tag.getFloat(KEY_X_ROT)
            );
        }
    }
}
