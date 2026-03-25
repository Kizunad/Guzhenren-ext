package com.Kizunad.guzhenrenext.xianqiao.data;

import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeInferenceService;
import com.Kizunad.guzhenrenext.xianqiao.opening.InitialTerrainPlan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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

    private static final String KEY_APERTURES = "apertures";

    private static final String KEY_INITIALIZED_APERTURES = "initializedApertures";

    private static final String KEY_LAST_LOGOUT_TIMES = "lastLogoutTimes";

    private static final String KEY_RETURN_POSITIONS = "returnPositions";

    private static final String KEY_OWNER = "owner";

    private static final String KEY_INFO = "info";

    private static final String KEY_SCHEMA_VERSION = "schemaVersion";

    private static final String KEY_INITIALIZATION_STATES = "initializationStates";

    private static final String KEY_RESOLVED_INITIAL_TERRAIN_PLANS = "resolvedInitialTerrainPlans";

    private static final String KEY_INIT_PHASE = "initPhase";

    private static final String KEY_OPENING_SNAPSHOT = "openingSnapshot";

    private static final String KEY_LAYOUT_VERSION = "layoutVersion";

    private static final String KEY_PLAN_SEED = "planSeed";

    private static final String KEY_PLAN = "plan";

    private static final String KEY_LAYOUT_TIER = "layoutTier";

    private static final String KEY_LAYOUT_SIZE = "layoutSize";

    private static final String KEY_CELL_COUNT = "cellCount";

    private static final String KEY_CORE_ANCHOR = "coreAnchor";

    private static final String KEY_SEAM_CENTER_CHUNK_X = "seamCenterChunkX";

    private static final String KEY_SEAM_CENTER_CHUNK_Z = "seamCenterChunkZ";

    private static final String KEY_CORE_ANCHOR_SEMANTICS = "coreAnchorSemantics";

    private static final String KEY_TELEPORT_ANCHOR = "teleportAnchor";

    private static final String KEY_ANCHOR_CELL_X = "anchorCellX";

    private static final String KEY_ANCHOR_CELL_Z = "anchorCellZ";

    private static final String KEY_ANCHOR_CHUNK_X = "anchorChunkX";

    private static final String KEY_ANCHOR_CHUNK_Z = "anchorChunkZ";

    private static final String KEY_ANCHOR_CHUNK_CENTER_X = "anchorChunkCenterX";

    private static final String KEY_ANCHOR_CHUNK_CENTER_Z = "anchorChunkCenterZ";

    private static final String KEY_TELEPORT_ANCHOR_SEMANTICS = "teleportAnchorSemantics";

    private static final String KEY_LAYOUT_ORIGIN = "layoutOrigin";

    private static final String KEY_ORIGIN_CHUNK_X = "originChunkX";

    private static final String KEY_ORIGIN_CHUNK_Z = "originChunkZ";

    private static final String KEY_LAYOUT_ORIGIN_SEMANTICS = "layoutOriginSemantics";

    private static final String KEY_INITIAL_CHUNK_BOUNDARY = "initialChunkBoundary";

    private static final String KEY_PLAN_MIN_CHUNK_X = "minChunkX";

    private static final String KEY_PLAN_MAX_CHUNK_X = "maxChunkX";

    private static final String KEY_PLAN_MIN_CHUNK_Z = "minChunkZ";

    private static final String KEY_PLAN_MAX_CHUNK_Z = "maxChunkZ";

    private static final String KEY_RING_PARAMETERS = "ringParameters";

    private static final String KEY_MAX_RESERVED_CHAOS_BLOCKS = "maxReservedChaosBlocks";

    private static final String KEY_SAFE_ZONE_MAX_OUTSIDE_DISTANCE_BLOCKS = "safeZoneMaxOutsideDistanceBlocks";

    private static final String KEY_WARNING_ZONE_MAX_OUTSIDE_DISTANCE_BLOCKS = "warningZoneMaxOutsideDistanceBlocks";

    private static final String KEY_LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS = "lethalZoneStartOutsideDistanceBlocks";

    private static final String KEY_BIOME_FALLBACK_POLICY = "biomeFallbackPolicy";

    private static final String KEY_PLANNED_CELLS = "plannedCells";

    private static final String KEY_CELL_X = "cellX";

    private static final String KEY_CELL_Z = "cellZ";

    private static final String KEY_CHUNK_X = "chunkX";

    private static final String KEY_CHUNK_Z = "chunkZ";

    private static final String KEY_GENERATION_ORDER = "generationOrder";

    private static final String KEY_RING_INDEX = "ringIndex";

    private static final String KEY_CENTER_KERNEL = "centerKernel";

    private static final String KEY_PRIMARY_BIOME_ID = "primaryBiomeId";

    private static final String KEY_BIOME_CANDIDATES = "biomeCandidates";

    private static final String KEY_ZHUANSHU = "zhuanshu";

    private static final String KEY_JIEDUAN = "jieduan";

    private static final String KEY_HEAVEN_SCORE = "heavenScore";

    private static final String KEY_EARTH_SCORE = "earthScore";

    private static final String KEY_HUMAN_SCORE = "humanScore";

    private static final String KEY_BALANCE_SCORE = "balanceScore";

    private static final String KEY_ASCENSION_ATTEMPT_INITIATED = "ascensionAttemptInitiated";

    private static final String KEY_SNAPSHOT_FROZEN = "snapshotFrozen";

    private static final String KEY_LAST_LOGOUT_TIME = "lastLogoutTime";

    private static final String KEY_DIMENSION_KEY = "dimensionKey";

    private static final String KEY_X = "x";

    private static final String KEY_Y = "y";

    private static final String KEY_Z = "z";

    private static final String KEY_Y_ROT = "yRot";

    private static final String KEY_X_ROT = "xRot";

    private static final int TAG_COMPOUND = Tag.TAG_COMPOUND;

    private static final int TAG_LIST = Tag.TAG_LIST;

    private static final int TAG_STRING = Tag.TAG_STRING;

    private static final int DEFAULT_NEXT_INDEX = 1;

    private static final int LEGACY_SCHEMA_VERSION = ApertureWorldDataSchema.LEGACY_SCHEMA_VERSION;

    private static final int CURRENT_SCHEMA_VERSION = ApertureWorldDataSchema.CURRENT_SCHEMA_VERSION;

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

    private final Map<ResolvedInitialTerrainPlanKey, InitialTerrainPlan> resolvedInitialTerrainPlans = new HashMap<>();

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
        initializationStates.put(owner, ApertureInitializationState.uninitialized());
        setDirty();
        return created;
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
     * 直接按世界 chunk 坐标回写仙窍边界。
     * <p>
     * 该方法用于初始化运行时在规划完成后，把 planner 输出的局部 chunk 边界
     * （例如 {@code InitialTerrainPlan.initialChunkBoundary}）映射到玩家真实世界坐标，
     * 并作为 {@link ApertureInfo} 的 min/maxChunk 真源持久化。
     * </p>
     * <p>
     * 设计约束：
     * 1) 只更新边界，不改 center；
     * 2) 允许传入顺序无序坐标，最终由 {@link ApertureInfo} 构造器归一化；
     * 3) 若回写结果与当前值一致，不重复标记脏。
     * </p>
     *
     * @param owner 玩家 UUID
     * @param minChunkX 世界坐标最小 chunkX
     * @param maxChunkX 世界坐标最大 chunkX
     * @param minChunkZ 世界坐标最小 chunkZ
     * @param maxChunkZ 世界坐标最大 chunkZ
     */
    public void updateChunkBoundary(UUID owner, int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        ApertureInfo existing = apertures.get(owner);
        if (existing == null) {
            return;
        }
        ApertureInfo updated = new ApertureInfo(
            existing.center(),
            minChunkX,
            maxChunkX,
            minChunkZ,
            maxChunkZ,
            existing.timeSpeed(),
            existing.nextTribulationTick(),
            existing.isFrozen(),
            existing.favorability(),
            existing.tier()
        );
        if (updated.equals(existing)) {
            return;
        }
        apertures.put(owner, updated);
        setDirty();
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

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public ApertureInitializationState getInitializationState(UUID owner) {
        return resolveInitializationState(owner);
    }

    public ApertureInitPhase getInitPhase(UUID owner) {
        return resolveInitializationState(owner).initPhase();
    }

    @Nullable
    public ApertureOpeningSnapshot getOpeningSnapshot(UUID owner) {
        return resolveInitializationState(owner).openingSnapshot();
    }

    @Nullable
    public Integer getLayoutVersion(UUID owner) {
        return resolveInitializationState(owner).layoutVersion();
    }

    @Nullable
    public Long getPlanSeed(UUID owner) {
        return resolveInitializationState(owner).planSeed();
    }

    public void saveResolvedInitialTerrainPlan(UUID owner, int layoutVersion, long planSeed, InitialTerrainPlan plan) {
        Objects.requireNonNull(plan, "plan");
        ResolvedInitialTerrainPlanKey key = resolvedInitialTerrainPlanKey(owner, layoutVersion, planSeed);
        InitialTerrainPlan previous = resolvedInitialTerrainPlans.put(key, plan);
        if (!plan.equals(previous)) {
            setDirty();
        }
    }

    public Optional<InitialTerrainPlan> loadResolvedInitialTerrainPlan(UUID owner, int layoutVersion, long planSeed) {
        ResolvedInitialTerrainPlanKey key = resolvedInitialTerrainPlanKey(owner, layoutVersion, planSeed);
        return Optional.ofNullable(resolvedInitialTerrainPlans.get(key));
    }

    public void setInitializationState(UUID owner, ApertureInitializationState initializationState) {
        ApertureInitializationState normalizedState = ApertureWorldDataSchema.normalizeInitializationState(
            initializationState
        );
        initializationStates.put(owner, normalizedState);
        syncLegacyInitializedState(owner, normalizedState);
        schemaVersion = CURRENT_SCHEMA_VERSION;
        setDirty();
    }

    public void setInitPhase(UUID owner, ApertureInitPhase initPhase) {
        ApertureInitializationState existingState = resolveInitializationState(owner);
        setInitializationState(
            owner,
            new ApertureInitializationState(
                initPhase,
                existingState.openingSnapshot(),
                existingState.layoutVersion(),
                existingState.planSeed()
            )
        );
    }

    /**
     * 判断指定玩家的仙窍是否已经执行过首次初始化。
     *
     * @param owner 玩家 UUID
     * @return 已初始化返回 true，否则 false
     */
    public boolean isApertureInitialized(UUID owner) {
        return resolveInitializationState(owner).isInitializedEquivalent();
    }

    /**
     * 将指定玩家仙窍标记为已初始化。
     *
     * @param owner 玩家 UUID
     */
    public void markApertureInitialized(UUID owner) {
        ApertureInitializationState existingState = resolveInitializationState(owner);
        setInitializationState(
            owner,
            new ApertureInitializationState(
                ApertureInitPhase.COMPLETED,
                existingState.openingSnapshot(),
                existingState.layoutVersion(),
                existingState.planSeed()
            )
        );
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
        ApertureSerializedWorldState normalizedWorldState = buildNormalizedWorldState();
        applyNormalizedWorldState(normalizedWorldState);

        tag.putInt(KEY_SCHEMA_VERSION, normalizedWorldState.schemaVersion());
        tag.putInt(KEY_NEXT_INDEX, normalizedWorldState.nextIndex());

        ListTag list = new ListTag();
        for (Map.Entry<UUID, ApertureInfo> entry : apertures.entrySet()) {
            CompoundTag apertureTag = new CompoundTag();
            apertureTag.putUUID(KEY_OWNER, entry.getKey());
            apertureTag.put(KEY_INFO, entry.getValue().save());
            list.add(apertureTag);
        }
        tag.put(KEY_APERTURES, list);

        ListTag initializationStateList = new ListTag();
        for (
            Map.Entry<UUID, ApertureInitializationState> entry : normalizedWorldState.initializationStates().entrySet()
        ) {
            CompoundTag initializationStateTag = new CompoundTag();
            initializationStateTag.putUUID(KEY_OWNER, entry.getKey());
            initializationStateTag.put(KEY_INFO, saveInitializationState(entry.getValue()));
            initializationStateList.add(initializationStateTag);
        }
        tag.put(KEY_INITIALIZATION_STATES, initializationStateList);

        ListTag resolvedPlanList = new ListTag();
        for (
            Map.Entry<ResolvedInitialTerrainPlanKey, InitialTerrainPlan> entry
                : resolvedInitialTerrainPlans.entrySet()
        ) {
            CompoundTag resolvedPlanTag = new CompoundTag();
            resolvedPlanTag.putUUID(KEY_OWNER, entry.getKey().owner());
            resolvedPlanTag.putInt(KEY_LAYOUT_VERSION, entry.getKey().layoutVersion());
            resolvedPlanTag.putLong(KEY_PLAN_SEED, entry.getKey().planSeed());
            resolvedPlanTag.put(KEY_PLAN, saveResolvedInitialTerrainPlan(entry.getValue()));
            resolvedPlanList.add(resolvedPlanTag);
        }
        tag.put(KEY_RESOLVED_INITIAL_TERRAIN_PLANS, resolvedPlanList);

        ListTag initializedList = new ListTag();
        for (UUID owner : normalizedWorldState.initializedApertures()) {
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

    private static ApertureWorldData load(CompoundTag tag) {
        ApertureWorldData data = new ApertureWorldData();
        data.schemaVersion = tag.contains(KEY_SCHEMA_VERSION)
            ? Math.max(LEGACY_SCHEMA_VERSION, tag.getInt(KEY_SCHEMA_VERSION))
            : LEGACY_SCHEMA_VERSION;
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

        if (tag.contains(KEY_INITIALIZATION_STATES, TAG_LIST)) {
            ListTag initializationStateList = tag.getList(KEY_INITIALIZATION_STATES, TAG_COMPOUND);
            for (int i = 0; i < initializationStateList.size(); i++) {
                CompoundTag initializationStateTag = initializationStateList.getCompound(i);
                if (!initializationStateTag.hasUUID(KEY_OWNER)
                    || !initializationStateTag.contains(KEY_INFO, TAG_COMPOUND)) {
                    continue;
                }
                data.initializationStates.put(
                    initializationStateTag.getUUID(KEY_OWNER),
                    loadInitializationState(initializationStateTag.getCompound(KEY_INFO))
                );
            }
        }

        if (tag.contains(KEY_RESOLVED_INITIAL_TERRAIN_PLANS, TAG_LIST)) {
            ListTag resolvedPlanList = tag.getList(KEY_RESOLVED_INITIAL_TERRAIN_PLANS, TAG_COMPOUND);
            for (int i = 0; i < resolvedPlanList.size(); i++) {
                CompoundTag resolvedPlanTag = resolvedPlanList.getCompound(i);
                if (!resolvedPlanTag.hasUUID(KEY_OWNER)
                    || !resolvedPlanTag.contains(KEY_LAYOUT_VERSION)
                    || !resolvedPlanTag.contains(KEY_PLAN_SEED)
                    || !resolvedPlanTag.contains(KEY_PLAN, TAG_COMPOUND)) {
                    continue;
                }
                UUID owner = resolvedPlanTag.getUUID(KEY_OWNER);
                int layoutVersion = resolvedPlanTag.getInt(KEY_LAYOUT_VERSION);
                long planSeed = resolvedPlanTag.getLong(KEY_PLAN_SEED);
                try {
                    ResolvedInitialTerrainPlanKey key = resolvedInitialTerrainPlanKey(owner, layoutVersion, planSeed);
                    InitialTerrainPlan plan = loadResolvedInitialTerrainPlan(resolvedPlanTag.getCompound(KEY_PLAN));
                    data.resolvedInitialTerrainPlans.put(key, plan);
                } catch (IllegalArgumentException exception) {
                    continue;
                }
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

        data.applyNormalizedWorldState(data.buildNormalizedWorldState());
        return data;
    }

    private ApertureInitializationState resolveInitializationState(UUID owner) {
        ApertureInitializationState initializationState = initializationStates.get(owner);
        if (initializationState != null) {
            return ApertureWorldDataSchema.normalizeInitializationState(initializationState);
        }
        return ApertureWorldDataSchema.legacyInitializationState(initializedApertures.contains(owner));
    }

    private void syncLegacyInitializedState(UUID owner, ApertureInitializationState initializationState) {
        if (initializationState.isInitializedEquivalent()) {
            initializedApertures.add(owner);
            return;
        }
        initializedApertures.remove(owner);
    }

    private ApertureSerializedWorldState buildNormalizedWorldState() {
        return ApertureWorldDataSchema.normalizeWorldState(
            new ApertureSerializedWorldState(
                schemaVersion,
                nextIndex,
                serializeApertures(),
                new HashMap<>(initializationStates),
                new HashSet<>(initializedApertures)
            )
        );
    }

    private void applyNormalizedWorldState(ApertureSerializedWorldState normalizedWorldState) {
        schemaVersion = normalizedWorldState.schemaVersion();
        nextIndex = normalizedWorldState.nextIndex();
        initializationStates.clear();
        initializationStates.putAll(normalizedWorldState.initializationStates());
        initializedApertures.clear();
        initializedApertures.addAll(normalizedWorldState.initializedApertures());
    }

    private Map<UUID, ApertureSerializedApertureInfo> serializeApertures() {
        Map<UUID, ApertureSerializedApertureInfo> serializedApertures = new HashMap<>();
        for (Map.Entry<UUID, ApertureInfo> entry : apertures.entrySet()) {
            ApertureInfo apertureInfo = entry.getValue();
            serializedApertures.put(
                entry.getKey(),
                new ApertureSerializedApertureInfo(
                    apertureInfo.center().getX(),
                    apertureInfo.center().getY(),
                    apertureInfo.center().getZ(),
                    apertureInfo.minChunkX(),
                    apertureInfo.maxChunkX(),
                    apertureInfo.minChunkZ(),
                    apertureInfo.maxChunkZ(),
                    apertureInfo.timeSpeed(),
                    apertureInfo.nextTribulationTick(),
                    apertureInfo.isFrozen(),
                    apertureInfo.favorability(),
                    apertureInfo.tier()
                )
            );
        }
        return serializedApertures;
    }

    private static CompoundTag saveInitializationState(ApertureInitializationState initializationState) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_INIT_PHASE, initializationState.initPhase().name());
        if (initializationState.openingSnapshot() != null) {
            tag.put(KEY_OPENING_SNAPSHOT, saveOpeningSnapshot(initializationState.openingSnapshot()));
        }
        if (initializationState.layoutVersion() != null) {
            tag.putInt(KEY_LAYOUT_VERSION, initializationState.layoutVersion());
        }
        if (initializationState.planSeed() != null) {
            tag.putLong(KEY_PLAN_SEED, initializationState.planSeed());
        }
        return tag;
    }

    private static ApertureInitializationState loadInitializationState(CompoundTag tag) {
        ApertureInitPhase initPhase = ApertureWorldDataSchema.parseInitPhase(tag.getString(KEY_INIT_PHASE));
        ApertureOpeningSnapshot openingSnapshot = tag.contains(KEY_OPENING_SNAPSHOT, TAG_COMPOUND)
            ? loadOpeningSnapshot(tag.getCompound(KEY_OPENING_SNAPSHOT))
            : null;
        Integer layoutVersion = tag.contains(KEY_LAYOUT_VERSION)
            ? Integer.valueOf(tag.getInt(KEY_LAYOUT_VERSION))
            : null;
        Long planSeed = tag.contains(KEY_PLAN_SEED) ? Long.valueOf(tag.getLong(KEY_PLAN_SEED)) : null;
        return ApertureWorldDataSchema.normalizeInitializationState(
            new ApertureInitializationState(initPhase, openingSnapshot, layoutVersion, planSeed)
        );
    }

    private static CompoundTag saveResolvedInitialTerrainPlan(InitialTerrainPlan plan) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_LAYOUT_TIER, plan.layoutTier().name());
        tag.putInt(KEY_LAYOUT_SIZE, plan.layoutSize());
        tag.putInt(KEY_CELL_COUNT, plan.cellCount());
        tag.put(KEY_CORE_ANCHOR, saveCoreAnchor(plan.coreAnchor()));
        tag.put(KEY_TELEPORT_ANCHOR, saveTeleportAnchor(plan.teleportAnchor()));
        tag.put(KEY_LAYOUT_ORIGIN, saveLayoutOrigin(plan.layoutOrigin()));
        tag.put(KEY_INITIAL_CHUNK_BOUNDARY, saveInitialChunkBoundary(plan.initialChunkBoundary()));
        tag.put(KEY_RING_PARAMETERS, saveRingParameters(plan.ringParameters()));
        tag.putString(KEY_BIOME_FALLBACK_POLICY, plan.biomeFallbackPolicy().name());
        ListTag plannedCells = new ListTag();
        for (InitialTerrainPlan.PlannedTerrainCell plannedCell : plan.plannedCells()) {
            plannedCells.add(savePlannedTerrainCell(plannedCell));
        }
        tag.put(KEY_PLANNED_CELLS, plannedCells);
        return tag;
    }

    private static InitialTerrainPlan loadResolvedInitialTerrainPlan(CompoundTag tag) {
        InitialTerrainPlan.LayoutTier layoutTier = InitialTerrainPlan.LayoutTier.valueOf(
            tag.getString(KEY_LAYOUT_TIER)
        );
        int layoutSize = tag.getInt(KEY_LAYOUT_SIZE);
        int cellCount = tag.getInt(KEY_CELL_COUNT);
        InitialTerrainPlan.CoreAnchor coreAnchor = loadCoreAnchor(tag.getCompound(KEY_CORE_ANCHOR));
        InitialTerrainPlan.TeleportAnchor teleportAnchor = loadTeleportAnchor(tag.getCompound(KEY_TELEPORT_ANCHOR));
        InitialTerrainPlan.LayoutOrigin layoutOrigin = loadLayoutOrigin(tag.getCompound(KEY_LAYOUT_ORIGIN));
        InitialTerrainPlan.InitialChunkBoundary initialChunkBoundary = loadInitialChunkBoundary(
            tag.getCompound(KEY_INITIAL_CHUNK_BOUNDARY)
        );
        InitialTerrainPlan.RingParameters ringParameters = loadRingParameters(tag.getCompound(KEY_RING_PARAMETERS));
        BiomeInferenceService.BiomeFallbackPolicy biomeFallbackPolicy =
            BiomeInferenceService.BiomeFallbackPolicy.valueOf(tag.getString(KEY_BIOME_FALLBACK_POLICY));
        List<InitialTerrainPlan.PlannedTerrainCell> plannedCells = loadPlannedTerrainCells(tag);
        return new InitialTerrainPlan(
            layoutTier,
            layoutSize,
            cellCount,
            coreAnchor,
            teleportAnchor,
            layoutOrigin,
            initialChunkBoundary,
            ringParameters,
            biomeFallbackPolicy,
            plannedCells
        );
    }

    private static CompoundTag saveCoreAnchor(InitialTerrainPlan.CoreAnchor coreAnchor) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(KEY_SEAM_CENTER_CHUNK_X, coreAnchor.seamCenterChunkX());
        tag.putDouble(KEY_SEAM_CENTER_CHUNK_Z, coreAnchor.seamCenterChunkZ());
        tag.putString(KEY_CORE_ANCHOR_SEMANTICS, coreAnchor.semantics().name());
        return tag;
    }

    private static InitialTerrainPlan.CoreAnchor loadCoreAnchor(CompoundTag tag) {
        return new InitialTerrainPlan.CoreAnchor(
            tag.getDouble(KEY_SEAM_CENTER_CHUNK_X),
            tag.getDouble(KEY_SEAM_CENTER_CHUNK_Z),
            InitialTerrainPlan.CoreAnchorSemantics.valueOf(tag.getString(KEY_CORE_ANCHOR_SEMANTICS))
        );
    }

    private static CompoundTag saveTeleportAnchor(InitialTerrainPlan.TeleportAnchor teleportAnchor) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_ANCHOR_CELL_X, teleportAnchor.anchorCellX());
        tag.putInt(KEY_ANCHOR_CELL_Z, teleportAnchor.anchorCellZ());
        tag.putInt(KEY_ANCHOR_CHUNK_X, teleportAnchor.anchorChunkX());
        tag.putInt(KEY_ANCHOR_CHUNK_Z, teleportAnchor.anchorChunkZ());
        tag.putDouble(KEY_ANCHOR_CHUNK_CENTER_X, teleportAnchor.anchorChunkCenterX());
        tag.putDouble(KEY_ANCHOR_CHUNK_CENTER_Z, teleportAnchor.anchorChunkCenterZ());
        tag.putString(KEY_TELEPORT_ANCHOR_SEMANTICS, teleportAnchor.semantics().name());
        return tag;
    }

    private static InitialTerrainPlan.TeleportAnchor loadTeleportAnchor(CompoundTag tag) {
        return new InitialTerrainPlan.TeleportAnchor(
            tag.getInt(KEY_ANCHOR_CELL_X),
            tag.getInt(KEY_ANCHOR_CELL_Z),
            tag.getInt(KEY_ANCHOR_CHUNK_X),
            tag.getInt(KEY_ANCHOR_CHUNK_Z),
            tag.getDouble(KEY_ANCHOR_CHUNK_CENTER_X),
            tag.getDouble(KEY_ANCHOR_CHUNK_CENTER_Z),
            InitialTerrainPlan.TeleportAnchorSemantics.valueOf(tag.getString(KEY_TELEPORT_ANCHOR_SEMANTICS))
        );
    }

    private static CompoundTag saveLayoutOrigin(InitialTerrainPlan.LayoutOrigin layoutOrigin) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_ORIGIN_CHUNK_X, layoutOrigin.originChunkX());
        tag.putInt(KEY_ORIGIN_CHUNK_Z, layoutOrigin.originChunkZ());
        tag.putString(KEY_LAYOUT_ORIGIN_SEMANTICS, layoutOrigin.semantics().name());
        return tag;
    }

    private static InitialTerrainPlan.LayoutOrigin loadLayoutOrigin(CompoundTag tag) {
        return new InitialTerrainPlan.LayoutOrigin(
            tag.getInt(KEY_ORIGIN_CHUNK_X),
            tag.getInt(KEY_ORIGIN_CHUNK_Z),
            InitialTerrainPlan.LayoutOriginSemantics.valueOf(tag.getString(KEY_LAYOUT_ORIGIN_SEMANTICS))
        );
    }

    private static CompoundTag saveInitialChunkBoundary(InitialTerrainPlan.InitialChunkBoundary initialChunkBoundary) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_PLAN_MIN_CHUNK_X, initialChunkBoundary.minChunkX());
        tag.putInt(KEY_PLAN_MAX_CHUNK_X, initialChunkBoundary.maxChunkX());
        tag.putInt(KEY_PLAN_MIN_CHUNK_Z, initialChunkBoundary.minChunkZ());
        tag.putInt(KEY_PLAN_MAX_CHUNK_Z, initialChunkBoundary.maxChunkZ());
        return tag;
    }

    private static InitialTerrainPlan.InitialChunkBoundary loadInitialChunkBoundary(CompoundTag tag) {
        return new InitialTerrainPlan.InitialChunkBoundary(
            tag.getInt(KEY_PLAN_MIN_CHUNK_X),
            tag.getInt(KEY_PLAN_MAX_CHUNK_X),
            tag.getInt(KEY_PLAN_MIN_CHUNK_Z),
            tag.getInt(KEY_PLAN_MAX_CHUNK_Z)
        );
    }

    private static CompoundTag saveRingParameters(InitialTerrainPlan.RingParameters ringParameters) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_MAX_RESERVED_CHAOS_BLOCKS, ringParameters.maxReservedChaosBlocks());
        tag.putInt(
            KEY_SAFE_ZONE_MAX_OUTSIDE_DISTANCE_BLOCKS,
            ringParameters.safeZoneMaxOutsideDistanceBlocks()
        );
        tag.putInt(
            KEY_WARNING_ZONE_MAX_OUTSIDE_DISTANCE_BLOCKS,
            ringParameters.warningZoneMaxOutsideDistanceBlocks()
        );
        tag.putInt(
            KEY_LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS,
            ringParameters.lethalZoneStartOutsideDistanceBlocks()
        );
        return tag;
    }

    private static InitialTerrainPlan.RingParameters loadRingParameters(CompoundTag tag) {
        return new InitialTerrainPlan.RingParameters(
            tag.getInt(KEY_MAX_RESERVED_CHAOS_BLOCKS),
            tag.getInt(KEY_SAFE_ZONE_MAX_OUTSIDE_DISTANCE_BLOCKS),
            tag.getInt(KEY_WARNING_ZONE_MAX_OUTSIDE_DISTANCE_BLOCKS),
            tag.getInt(KEY_LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS)
        );
    }

    private static CompoundTag savePlannedTerrainCell(InitialTerrainPlan.PlannedTerrainCell plannedCell) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_CELL_X, plannedCell.cellX());
        tag.putInt(KEY_CELL_Z, plannedCell.cellZ());
        tag.putInt(KEY_CHUNK_X, plannedCell.chunkX());
        tag.putInt(KEY_CHUNK_Z, plannedCell.chunkZ());
        tag.putInt(KEY_GENERATION_ORDER, plannedCell.generationOrder());
        tag.putInt(KEY_RING_INDEX, plannedCell.ringIndex());
        tag.putBoolean(KEY_CENTER_KERNEL, plannedCell.centerKernel());
        tag.putString(KEY_PRIMARY_BIOME_ID, plannedCell.primaryBiomeId());
        ListTag biomeCandidates = new ListTag();
        for (String biomeCandidate : plannedCell.biomeCandidates()) {
            biomeCandidates.add(StringTag.valueOf(biomeCandidate));
        }
        tag.put(KEY_BIOME_CANDIDATES, biomeCandidates);
        return tag;
    }

    private static List<InitialTerrainPlan.PlannedTerrainCell> loadPlannedTerrainCells(CompoundTag tag) {
        List<InitialTerrainPlan.PlannedTerrainCell> plannedCells = new ArrayList<>();
        if (!tag.contains(KEY_PLANNED_CELLS, TAG_LIST)) {
            return plannedCells;
        }
        ListTag cellList = tag.getList(KEY_PLANNED_CELLS, TAG_COMPOUND);
        for (int i = 0; i < cellList.size(); i++) {
            CompoundTag cellTag = cellList.getCompound(i);
            plannedCells.add(
                new InitialTerrainPlan.PlannedTerrainCell(
                    cellTag.getInt(KEY_CELL_X),
                    cellTag.getInt(KEY_CELL_Z),
                    cellTag.getInt(KEY_CHUNK_X),
                    cellTag.getInt(KEY_CHUNK_Z),
                    cellTag.getInt(KEY_GENERATION_ORDER),
                    cellTag.getInt(KEY_RING_INDEX),
                    cellTag.getBoolean(KEY_CENTER_KERNEL),
                    cellTag.getString(KEY_PRIMARY_BIOME_ID),
                    loadBiomeCandidates(cellTag)
                )
            );
        }
        return plannedCells;
    }

    private static List<String> loadBiomeCandidates(CompoundTag cellTag) {
        if (!cellTag.contains(KEY_BIOME_CANDIDATES, TAG_LIST)) {
            return List.of();
        }
        ListTag candidateTagList = cellTag.getList(KEY_BIOME_CANDIDATES, TAG_STRING);
        List<String> candidates = new ArrayList<>();
        for (int i = 0; i < candidateTagList.size(); i++) {
            candidates.add(candidateTagList.getString(i));
        }
        return candidates;
    }

    private static ResolvedInitialTerrainPlanKey resolvedInitialTerrainPlanKey(UUID owner, int layoutVersion,
                                                                                long planSeed) {
        return new ResolvedInitialTerrainPlanKey(owner, layoutVersion, planSeed);
    }

    private static CompoundTag saveOpeningSnapshot(ApertureOpeningSnapshot openingSnapshot) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_ZHUANSHU, openingSnapshot.zhuanshu());
        tag.putInt(KEY_JIEDUAN, openingSnapshot.jieduan());
        tag.putInt(KEY_HEAVEN_SCORE, openingSnapshot.heavenScore());
        tag.putInt(KEY_EARTH_SCORE, openingSnapshot.earthScore());
        tag.putInt(KEY_HUMAN_SCORE, openingSnapshot.humanScore());
        tag.putInt(KEY_BALANCE_SCORE, openingSnapshot.balanceScore());
        tag.putBoolean(KEY_ASCENSION_ATTEMPT_INITIATED, openingSnapshot.ascensionAttemptInitiated());
        tag.putBoolean(KEY_SNAPSHOT_FROZEN, openingSnapshot.snapshotFrozen());
        return tag;
    }

    private static ApertureOpeningSnapshot loadOpeningSnapshot(CompoundTag tag) {
        return new ApertureOpeningSnapshot(
            tag.getInt(KEY_ZHUANSHU),
            tag.getInt(KEY_JIEDUAN),
            tag.getInt(KEY_HEAVEN_SCORE),
            tag.getInt(KEY_EARTH_SCORE),
            tag.getInt(KEY_HUMAN_SCORE),
            tag.getInt(KEY_BALANCE_SCORE),
            tag.getBoolean(KEY_ASCENSION_ATTEMPT_INITIATED),
            tag.getBoolean(KEY_SNAPSHOT_FROZEN)
        );
    }

    /**
     * 创建 SavedData 工厂。
     *
     * @return SavedData Factory
     */
    public static SavedData.Factory<ApertureWorldData> factory() {
        return new SavedData.Factory<>(ApertureWorldData::new, (tag, provider) -> load(tag), null);
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

    private record ResolvedInitialTerrainPlanKey(UUID owner, int layoutVersion, long planSeed) {

        private ResolvedInitialTerrainPlanKey {
            Objects.requireNonNull(owner, "owner");
            if (layoutVersion <= 0) {
                throw new IllegalArgumentException("layoutVersion 必须大于 0");
            }
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
