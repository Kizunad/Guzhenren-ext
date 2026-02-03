package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * 单个基地的核心数据结构。
 * <p>
 * 此记录包含基地的所有持久化状态。运行时缓存（frontier、刷怪计数等）不存储在此处。
 * </p>
 *
 * @param id                  基地唯一标识符
 * @param state               当前状态（ACTIVE/SEALED/DESTROYED）
 * @param corePos             核心方块的世界坐标
 * @param dimension           基地所在维度
 * @param bastionType         配置类型 id（指向 JSON 配置）
 * @param primaryDao          基地的主道途类型
 * @param tier                当前转数（1-9，支持高转内容）
 * @param evolutionProgress   进化进度（0.0 到 1.0）
 * @param totalNodes          节点总数（增量缓存，包含核心）
 * @param nodesByTier         各转数节点计数（增量缓存）
 * @param growthRadius        当前扩张半径
 * @param growthCursor        扩张游标（用于确定性随机）
 * @param resourcePool        资源池（用于资源驱动扩张）
 * @param pollution           污染值（0.0-1.0，表示基地污染程度，越高越脏）
 * @param counts              菌毯/Anchor 等额外计数（避免 CODEC 超出参数上限）
 * @param modifiers           基地词缀（生态演化/变异，默认空集）
 * @param timing              时间相关字段（封印、销毁、最后处理、离线累积）
 */
public record BastionData(
        UUID id,
        BastionState state,
        BlockPos corePos,
        ResourceKey<Level> dimension,
        String bastionType,
        BastionDao primaryDao,
        int tier,
        double evolutionProgress,
        int totalNodes,
        Map<Integer, Integer> nodesByTier,
         int growthRadius,
         long growthCursor,
         double resourcePool,
         double pollution,
         BastionCounts counts,
         Set<BastionModifier> modifiers,
         BastionTiming timing,
         CaptureState captureState,
         long lastBossSpawnGameTime,
         BastionTalentData talentData
) {

    /**
     * 额外计数信息。
     * <p>
     * 目的：避免 BastionData CODEC 字段超过 RecordCodecBuilder 的参数上限。</p>
     *
     * @param totalMycelium 菌毯总数
     * @param totalAnchors  Anchor 总数
     */
     public record BastionCounts(int totalMycelium, int totalAnchors, int threatMeter) {
         public static final BastionCounts DEFAULT = new BastionCounts(0, 0, 0);

         public static final Codec<BastionCounts> CODEC = RecordCodecBuilder.create(instance ->
             instance.group(
                 Codec.INT.optionalFieldOf("total_mycelium", 0).forGetter(BastionCounts::totalMycelium),
                 Codec.INT.optionalFieldOf("total_anchors", 0).forGetter(BastionCounts::totalAnchors),
                 Codec.INT.optionalFieldOf("threat_meter", 0).forGetter(BastionCounts::threatMeter)
             ).apply(instance, BastionCounts::new)
         );
     }

    /**
     * 接管原因枚举。
     * <p>
     * NONE 表示未可接管；BOSS_DEFEATED 与 PURIFICATION_READY 对应 10.1 规划的两个入口。
     * </p>
     */
    public enum CaptureReason {
        NONE,
        BOSS_DEFEATED,
        PURIFICATION_READY;

        /** 序列化/反序列化编解码器。 */
        public static final Codec<CaptureReason> CODEC = Codec.STRING.xmap(
            value -> CaptureReason.valueOf(value.toUpperCase(java.util.Locale.ROOT)),
            reason -> reason.name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    /**
     * 接管状态信息。
     * <p>
     * 通过嵌套 record 规避 RecordCodecBuilder 16 参数限制，并保持旧存档兼容。
     * </p>
     *
     * @param capturable               是否可接管
     * @param reason                   可接管原因（NONE/BOSS_DEFEATED/PURIFICATION_READY）
     * @param capturableUntilGameTime  可接管窗口截止时间（0 表示无限期）
     * @param captured                 是否已被玩家接管
     * @param capturedBy               接管者 UUID（null 表示尚未接管）
     */
    public record CaptureState(
        boolean capturable,
        CaptureReason reason,
        long capturableUntilGameTime,
        boolean captured,
        java.util.UUID capturedBy
    ) {
        public static final CaptureState DEFAULT = new CaptureState(
            false,
            CaptureReason.NONE,
            0L,
            false,
            null
        );

        /** 序列化/反序列化编解码器。 */
        public static final Codec<CaptureState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("capturable", false).forGetter(CaptureState::capturable),
                CaptureReason.CODEC.optionalFieldOf("reason", CaptureReason.NONE)
                    .forGetter(CaptureState::reason),
                Codec.LONG.optionalFieldOf("capturable_until_game_time", 0L)
                    .forGetter(CaptureState::capturableUntilGameTime),
                Codec.BOOL.optionalFieldOf("captured", false)
                    .forGetter(CaptureState::captured),
                UUIDUtil.CODEC.optionalFieldOf("captured_by")
                    .forGetter(state -> java.util.Optional.ofNullable(state.capturedBy()))
            ).apply(instance, (capturable, reason, until, captured, capturedBy) -> new CaptureState(
                capturable,
                reason,
                until,
                captured,
                capturedBy.orElse(null)
            ))
        );

        /**
         * 判断是否被指定玩家接管。
         *
         * @param playerId 玩家 UUID
         * @return true 表示已被该玩家接管
         */
        public boolean isCapturedBy(java.util.UUID playerId) {
            return captured && playerId != null && playerId.equals(capturedBy);
        }
    }

    /** 兼容旧调用：返回菌毯总数。 */
    public int totalMycelium() {
        return counts == null ? 0 : counts.totalMycelium();
    }

    /** 兼容旧调用：返回 Anchor 总数。 */
    public int totalAnchors() {
        return counts == null ? 0 : counts.totalAnchors();
    }

     /** 兼容调用：返回威胁值计量。 */
     public int threatMeter() {
         return counts == null ? 0 : counts.threatMeter();
     }

    /**
     * 词缀集合的编解码器。
     * <p>
     * 存档使用列表形式，运行时使用 Set 去重。
     * </p>
     */
    private static final MapCodec<Set<BastionModifier>> MODIFIERS_CODEC =
        BastionModifier.CODEC.listOf()
            .optionalFieldOf("modifiers", java.util.List.of())
            .xmap(java.util.Set::copyOf, java.util.List::copyOf);

    /** 默认最大转数（基础配置，可通过 BastionTypeConfig 扩展到 9）。 */
    public static final int DEFAULT_MAX_TIER = 9;

    /**
     * 时间相关字段的嵌套记录，避免超出 Codec 的 16 参数限制。
     *
     * @param sealedUntilGameTime 封印截止的游戏时间（0 表示未封印）
     * @param destroyedAtGameTime 核心被破坏的游戏时间（0 表示未破坏）
     * @param lastGameTime        上次处理的游戏时间
     * @param offlineAccumTicks   chunk 未加载时累积的离线 tick
     */
    public record BastionTiming(
            long sealedUntilGameTime,
            long destroyedAtGameTime,
            long lastGameTime,
            long offlineAccumTicks
    ) {
        /** 序列化/反序列化编解码器。 */
        public static final Codec<BastionTiming> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.LONG.fieldOf("sealed_until_game_time").forGetter(BastionTiming::sealedUntilGameTime),
                Codec.LONG.fieldOf("destroyed_at_game_time").forGetter(BastionTiming::destroyedAtGameTime),
                Codec.LONG.fieldOf("last_game_time").forGetter(BastionTiming::lastGameTime),
                Codec.LONG.fieldOf("offline_accum_ticks").forGetter(BastionTiming::offlineAccumTicks)
            ).apply(instance, BastionTiming::new)
        );

        /**
         * 创建默认时间数据。
         *
         * @param gameTime 当前游戏时间
         * @return 默认 BastionTiming 实例
         */
        public static BastionTiming createDefault(long gameTime) {
            return new BastionTiming(0L, 0L, gameTime, 0L);
        }
    }

    /**
     * 附加状态分组。
     * <p>
     * 目的：把 counts / modifiers / timing / capture_state 打包为一组，避免 RecordCodecBuilder
     * 16 参数限制，同时保持 JSON 扁平结构与旧存档兼容。
     * </p>
     */
     private record AdditionalState(
             BastionCounts counts,
             Set<BastionModifier> modifiers,
             BastionTiming timing,
             CaptureState captureState,
             long lastBossSpawnGameTime,
             BastionTalentData talentData) {
        private static final AdditionalState DEFAULT = new AdditionalState(
            BastionCounts.DEFAULT,
            java.util.Set.of(),
            BastionTiming.createDefault(0L),
            CaptureState.DEFAULT,
            0L,
            BastionTalentData.DEFAULT
        );

        private static final MapCodec<AdditionalState> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                BastionCounts.CODEC.optionalFieldOf("counts", BastionCounts.DEFAULT)
                    .forGetter(AdditionalState::counts),
                MODIFIERS_CODEC.forGetter(AdditionalState::modifiers),
                BastionTiming.CODEC.optionalFieldOf("timing", BastionTiming.createDefault(0L))
                    .forGetter(AdditionalState::timing),
                CaptureState.CODEC.optionalFieldOf("capture_state", CaptureState.DEFAULT)
                    .forGetter(AdditionalState::captureState),
                Codec.LONG.optionalFieldOf("last_boss_spawn_game_time", 0L)
                    .forGetter(AdditionalState::lastBossSpawnGameTime),
                BastionTalentData.CODEC.optionalFieldOf(
                    "talent_data",
                    BastionTalentData.DEFAULT
                ).forGetter(AdditionalState::talentData)
            ).apply(instance, AdditionalState::new)
        );
    }

    /** 序列化/反序列化编解码器。 */
    public static final Codec<BastionData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(BastionData::id),
            BastionState.CODEC.fieldOf("state").forGetter(BastionData::state),
            BlockPos.CODEC.fieldOf("core_pos").forGetter(BastionData::corePos),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(BastionData::dimension),
            Codec.STRING.fieldOf("bastion_type").forGetter(BastionData::bastionType),
            BastionDao.CODEC.fieldOf("primary_dao").forGetter(BastionData::primaryDao),
            Codec.INT.fieldOf("tier").forGetter(BastionData::tier),
            Codec.DOUBLE.fieldOf("evolution_progress").forGetter(BastionData::evolutionProgress),
            Codec.INT.fieldOf("total_nodes").forGetter(BastionData::totalNodes),
            Codec.unboundedMap(Codec.INT, Codec.INT).fieldOf("nodes_by_tier")
                .forGetter(BastionData::nodesByTier),
            Codec.INT.fieldOf("growth_radius").forGetter(BastionData::growthRadius),
            Codec.LONG.fieldOf("growth_cursor").forGetter(BastionData::growthCursor),
             Codec.DOUBLE.fieldOf("resource_pool").forGetter(BastionData::resourcePool),
              Codec.DOUBLE.optionalFieldOf("pollution", 0.0).forGetter(BastionData::pollution),
              AdditionalState.CODEC.forGetter(data -> new AdditionalState(
                  data.counts == null ? BastionCounts.DEFAULT : data.counts,
                  data.modifiers,
                  data.timing,
                 data.captureState == null ? CaptureState.DEFAULT : data.captureState,
                  data.lastBossSpawnGameTime,
                  data.talentData == null
                      ? BastionTalentData.DEFAULT
                      : data.talentData
             ))
        ).apply(instance, (id,
                state,
                corePos,
                dimension,
                bastionType,
                primaryDao,
                tier,
                evolutionProgress,
                totalNodes,
                nodesByTier,
                growthRadius,
                 growthCursor,
                 resourcePool,
                 pollution,
                   additional) -> new BastionData(
                id,
                state,
                corePos,
                dimension,
                bastionType,
                primaryDao,
                tier,
                evolutionProgress,
                totalNodes,
                 nodesByTier,
                 growthRadius,
                 growthCursor,
                  resourcePool,
                  pollution,
                 additional.counts(),
                 additional.modifiers(),
                 additional.timing(),
                 additional.captureState(),
                 additional.lastBossSpawnGameTime(),
                 additional.talentData()))
    );

    // ===== 时间字段的便捷访问器 =====

    /** 返回封印截止的游戏时间。 */
    public long sealedUntilGameTime() {
        return timing.sealedUntilGameTime();
    }

    /** 返回核心被破坏的游戏时间。 */
    public long destroyedAtGameTime() {
        return timing.destroyedAtGameTime();
    }

    /** 返回上次处理的游戏时间。 */
    public long lastGameTime() {
        return timing.lastGameTime();
    }

    /** 返回离线累积的 tick 数。 */
    public long offlineAccumTicks() {
        return timing.offlineAccumTicks();
    }

    /**
     * 创建具有默认值的新基地。
     * <p>
     * 注意：totalNodes 初始化为 1，nodesByTier 初始化为 {1:1}，
     * 表示核心本身被计为 1 个 tier1 节点。后续所有公式应基于此假设。
     * </p>
     *
     * @param corePos    核心方块坐标
     * @param dimension  维度
     * @param bastionType 配置类型 id
     * @param primaryDao 主道途类型
     * @param gameTime   当前游戏时间
     * @return 新的 BastionData 实例
     */
    public static BastionData create(
            BlockPos corePos,
            ResourceKey<Level> dimension,
            String bastionType,
            BastionDao primaryDao,
            long gameTime) {
        return new BastionData(
            UUID.randomUUID(),
            BastionState.ACTIVE,
            corePos,
            dimension,
            bastionType,
            primaryDao,
            1,              // 初始转数
            0.0,            // 进化进度
            1,              // 节点总数（核心计为 1）
            new HashMap<>(Map.of(1, 1)),  // 各转数节点计数
            1,              // 初始扩张半径
            0L,             // 扩张游标
            0.0,            // 资源池
            0.0,            // 污染值
            BastionCounts.DEFAULT,
            java.util.Set.of(),
            BastionTiming.createDefault(gameTime),
            CaptureState.DEFAULT,
            0L,
            com.Kizunad.guzhenrenext.bastion.talent.BastionTalentData.DEFAULT
        );
    }

    /**
     * 返回天赋数据（兼容旧存档 null）。
     *
     * @return 非空天赋数据
     */
    public BastionTalentData talentData() {
        return talentData == null ? BastionTalentData.DEFAULT : talentData;
    }

    /**
     * 创建天赋数据更新后的副本。
     *
     * @param newTalentData 新的天赋数据（null 将回退为默认值）
     * @return 更新后的 BastionData
     */
    public BastionData withTalentData(BastionTalentData newTalentData) {
        BastionTalentData safe = newTalentData == null ? BastionTalentData.DEFAULT : newTalentData;
        return new BastionData(
            id,
            state,
            corePos,
            dimension,
            bastionType,
            primaryDao,
            tier,
            evolutionProgress,
            totalNodes,
            nodesByTier,
            growthRadius,
            growthCursor,
            resourcePool,
            pollution,
            counts,
            modifiers,
            timing,
            captureState,
            lastBossSpawnGameTime,
            safe
        );
    }

    /**
     * 返回考虑时间条件后的有效状态。
     *
     * @param currentGameTime 当前游戏时间
     * @return 有效 BastionState
     */
    public BastionState getEffectiveState(long currentGameTime) {
        return BastionState.getEffectiveState(state, timing.sealedUntilGameTime(), currentGameTime);
    }

    /**
     * 创建节点计数更新后的副本。
     *
     * @param nodeTier 被添加/移除节点的转数
     * @param countDelta 增量（+1 添加，-1 移除）
     * @return 更新后的 BastionData
     */
    public BastionData withNodeCountUpdate(int nodeTier, int countDelta) {
        Map<Integer, Integer> newNodesByTier = new HashMap<>(nodesByTier);
        newNodesByTier.merge(nodeTier, countDelta, Integer::sum);
        // 移除零值条目
        newNodesByTier.entrySet().removeIf(e -> e.getValue() <= 0);

        int newTotal = Math.max(0, totalNodes + countDelta);
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, newTotal, newNodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, counts, modifiers, timing, captureState,
             lastBossSpawnGameTime,
             talentData
        );
    }

    /**
     * 创建状态更新为 DESTROYED 的副本。
     *
     * @param gameTime 被破坏时的游戏时间
     * @return DESTROYED 状态的 BastionData
     */
    public BastionData withDestroyed(long gameTime) {
        BastionTiming newTiming = new BastionTiming(
            timing.sealedUntilGameTime(),
            gameTime,
            timing.lastGameTime(),
            timing.offlineAccumTicks()
        );
        return new BastionData(
            id, BastionState.DESTROYED, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
            growthCursor, resourcePool, pollution, counts, modifiers, newTiming, captureState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建应用封印的副本。
     *
     * @param sealUntil 封印截止的游戏时间
     * @return 应用封印的 BastionData
     */
    public BastionData withSealed(long sealUntil) {
        BastionTiming newTiming = new BastionTiming(
            sealUntil,
            timing.destroyedAtGameTime(),
            timing.lastGameTime(),
            timing.offlineAccumTicks()
        );
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
            growthCursor, resourcePool, pollution, counts, modifiers, newTiming, captureState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建进化进度和/或转数更新后的副本。
     *
     * @param newProgress 新进化进度
     * @param newTier 新转数（如果进化）
     * @return 更新后的 BastionData
     */
    public BastionData withEvolution(double newProgress, int newTier) {
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, newTier,
            newProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, counts, modifiers, timing, captureState,
             lastBossSpawnGameTime,
             talentData
        );
    }

    /**
     * 创建扩张半径更新后的副本。
     *
     * @param newRadius 新扩张半径值
     * @return 更新后的 BastionData
     */
    public BastionData withGrowthRadius(int newRadius) {
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, newRadius,
            growthCursor, resourcePool, pollution, counts, modifiers, timing, captureState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建扩张游标更新后的副本。
     *
     * @param newCursor 新扩张游标值
     * @return 更新后的 BastionData
     */
    public BastionData withGrowthCursor(long newCursor) {
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
            newCursor, resourcePool, pollution, counts, modifiers, timing, captureState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建资源池更新后的副本。
     *
     * @param newPool 新资源池值
     * @return 更新后的 BastionData
     */
    public BastionData withResourcePool(double newPool) {
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
            growthCursor, newPool, pollution, counts, modifiers, timing, captureState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建菌毯计数更新后的副本。
     *
     * @param delta 菌毯增量（+1 添加，-1 移除）
     * @return 更新后的 BastionData
     */
    public BastionData withMyceliumCountDelta(int delta) {
        BastionCounts safe = counts == null ? BastionCounts.DEFAULT : counts;
        int newTotal = Math.max(0, safe.totalMycelium() + delta);
        BastionCounts updatedCounts = new BastionCounts(newTotal, safe.totalAnchors(), safe.threatMeter());
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, updatedCounts, modifiers, timing, captureState,
             lastBossSpawnGameTime,
             talentData
        );
    }

    /**
     * 创建 Anchor 计数更新后的副本。
     *
     * @param delta Anchor 增量（+1 添加，-1 移除）
     * @return 更新后的 BastionData
     */
    public BastionData withAnchorCountDelta(int delta) {
        BastionCounts safe = counts == null ? BastionCounts.DEFAULT : counts;
        int newTotal = Math.max(0, safe.totalAnchors() + delta);
        BastionCounts updatedCounts = new BastionCounts(safe.totalMycelium(), newTotal, safe.threatMeter());
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, updatedCounts, modifiers, timing, captureState,
             lastBossSpawnGameTime,
             talentData
        );
    }

    /**
     * 创建最后游戏时间更新后的副本。
     *
     * @param newLastGameTime 新的最后游戏时间
     * @return 更新后的 BastionData
     */
    public BastionData withLastGameTime(long newLastGameTime) {
        BastionTiming newTiming = new BastionTiming(
            timing.sealedUntilGameTime(),
            timing.destroyedAtGameTime(),
            newLastGameTime,
            timing.offlineAccumTicks()
        );
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
            growthCursor, resourcePool, pollution, counts, modifiers, newTiming, captureState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建离线累积 tick 更新后的副本。
     *
     * @param newOfflineAccum 新的离线累积 tick 数
     * @return 更新后的 BastionData
     */
    public BastionData withOfflineAccumTicks(long newOfflineAccum) {
        BastionTiming newTiming = new BastionTiming(
            timing.sealedUntilGameTime(),
            timing.destroyedAtGameTime(),
            timing.lastGameTime(),
            newOfflineAccum
        );
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, counts, modifiers, newTiming, captureState,
             lastBossSpawnGameTime,
             talentData
        );
    }

     /**
      * 创建威胁值更新后的副本。
      *
      * @param newThreat 新的威胁值
      * @return 更新后的 BastionData
      */
     public BastionData withThreatMeter(int newThreat) {
         BastionCounts safe = counts == null ? BastionCounts.DEFAULT : counts;
         int clamped = Math.max(0, newThreat);
         BastionCounts updatedCounts = new BastionCounts(safe.totalMycelium(), safe.totalAnchors(), clamped);
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, updatedCounts, modifiers, timing, captureState,
             lastBossSpawnGameTime,
             talentData
        );
    }

    // ===== 光环半径计算（从 growthRadius 解耦） =====

    /**
     * 计算当前有效光环半径（考虑节点缩圈）。
     * <p>
     * 光环半径基于 BastionTypeConfig 的 auraConfig 配置，并根据节点数量进行缩圈：
     * effectiveRadius = baseRadius * tierExponent^(tier-1) * scale，
     * 其中 scale = clamp(minScale, totalNodes/refNodes, 1.0)。
     * </p>
     * <p>
     * 当节点被拆除时，光环半径会相应缩小，但保留最少 minScale（默认 30%）的范围。
     * 当节点数量达到或超过 refNodes 时，光环为满状态。
     * </p>
     *
     * @return 当前的有效光环半径（已应用缩圈）
     */
    public int getAuraRadius() {
        var typeConfig = com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager
            .getOrDefault(bastionType);
        return typeConfig.aura().calculateEffectiveRadius(tier, totalNodes);
    }

    /**
     * 计算满状态下的基础光环半径（不考虑节点缩圈）。
     * <p>
     * 公式：baseAuraRadius = baseRadius * tierExponent^(tier-1)，上限为 maxRadius。
     * </p>
     * <p>
     * 典型值：
     * <ul>
     *   <li>1 转：16 格</li>
     *   <li>6 转：256 格（16²）</li>
     *   <li>9 转：4096 格（16³）</li>
     * </ul>
     * </p>
     *
     * @return 当前转数对应的满状态光环半径
     */
    public int getBaseAuraRadius() {
        var typeConfig = com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager
            .getOrDefault(bastionType);
        return typeConfig.aura().calculateRadius(tier);
    }

    /**
     * 计算玩家距离衰减因子。
     * <p>
     * 衰减公式：factor = max(minFalloff, (1 - distance/auraRadius)^falloffPower)。
     * 中心区域效果最强（接近 1.0），边缘区域效果最弱（接近 minFalloff）。
     * </p>
     * <p>
     * 注意：此方法使用有效光环半径（已应用缩圈），确保衰减计算与实际光环范围一致。
     * </p>
     *
     * @param distance 玩家到核心的距离
     * @return 衰减因子（0.0 ~ 1.0），距离超出光环范围返回 0.0
     */
    public double getAuraFalloff(double distance) {
        var typeConfig = com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager
            .getOrDefault(bastionType);
        int effectiveRadius = typeConfig.aura().calculateEffectiveRadius(tier, totalNodes);
        return typeConfig.aura().calculateFalloff(distance, effectiveRadius);
    }

    /**
     * 创建词缀集合更新后的副本。
     *
     * @param newModifiers 新词缀集合
     * @return 更新后的 BastionData
     */
    public BastionData withModifiers(java.util.Set<BastionModifier> newModifiers) {
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, counts,
             newModifiers == null ? java.util.Set.of() : java.util.Set.copyOf(newModifiers),
             timing,
             captureState,
             lastBossSpawnGameTime,
             talentData
         );
     }

    // ===== 接管状态相关便捷方法 =====

    /**
     * 创建接管状态更新后的副本。
     *
     * @param newCaptureState 新的接管状态（null 将回退为默认值）
     * @return 更新后的 BastionData
     */
     public BastionData withCaptureState(CaptureState newCaptureState) {
         CaptureState safe = newCaptureState == null ? CaptureState.DEFAULT : newCaptureState;
         return new BastionData(
             id, state, corePos, dimension, bastionType, primaryDao, tier,
             evolutionProgress, totalNodes, nodesByTier, growthRadius,
             growthCursor, resourcePool, pollution, counts, modifiers, timing, safe,
             lastBossSpawnGameTime,
             talentData
         );
     }

    /**
     * 创建仅更新可接管标记的副本（原因与窗口保持不变）。
     *
     * @param capturable 是否可接管
     * @return 更新后的 BastionData
     */
     public BastionData withCapturable(boolean capturable) {
         CaptureState safe = captureState == null ? CaptureState.DEFAULT : captureState;
        return withCaptureState(new CaptureState(
            capturable,
            safe.reason(),
            safe.capturableUntilGameTime(),
            safe.captured(),
            safe.capturedBy()
        ));
     }

    /**
     * 创建接管原因与窗口更新后的副本。
     *
     * @param capturable             是否可接管
     * @param reason                 可接管原因
     * @param capturableUntilGameTime 可接管窗口截止时间（0 表示无限期）
     * @return 更新后的 BastionData
     */
     public BastionData withCapturable(boolean capturable, CaptureReason reason, long capturableUntilGameTime) {
         CaptureReason safeReason = reason == null ? CaptureReason.NONE : reason;
        CaptureState safe = captureState == null ? CaptureState.DEFAULT : captureState;
        return withCaptureState(new CaptureState(
            capturable,
            safeReason,
            Math.max(0L, capturableUntilGameTime),
            safe.captured(),
            safe.capturedBy()
        ));
     }

    /** 返回当前污染值（0.0~1.0）。 */
     public double pollution() {
         return Math.max(0.0, pollution);
     }

    /**
     * 返回当前污染阶段。
     * <p>
     * 阶段分界（闭区间/左闭右开，1.0 固定落在 CRITICAL）：
     * NONE 0-0.33、LIGHT 0.33-0.66、MEDIUM 0.66-0.9、CRITICAL 0.9-1.0。
     * </p>
     *
     * @return 依据污染值计算的阶段
     */
    public PollutionStage getPollutionStage() {
        return PollutionStage.from(pollution());
    }

    /**
     * 创建污染值更新后的副本。
     *
     * @param newPollution 新污染值（将被夹取到 0.0-1.0）
     * @return 更新后的 BastionData
     */
     public BastionData withPollution(double newPollution) {
         double clamped = Math.min(1.0, Math.max(0.0, newPollution));
         return new BastionData(
              id, state, corePos, dimension, bastionType, primaryDao, tier,
              evolutionProgress, totalNodes, nodesByTier, growthRadius,
              growthCursor, resourcePool, clamped, counts, modifiers, timing, captureState,
              lastBossSpawnGameTime,
              talentData
           );
       }

    /**
     * 创建接管后的副本：转为 ACTIVE、清空封印/摧毁时间并写入占领者。
     *
     * @param ownerId   接管者 UUID
     * @param gameTime  当前游戏时间（用于刷新 lastGameTime）
     * @return 接管后的 BastionData
     */
    public BastionData withCaptured(java.util.UUID ownerId, long gameTime) {
        CaptureState safe = captureState == null ? CaptureState.DEFAULT : captureState;
        CaptureState capturedState = new CaptureState(
            false,
            CaptureReason.NONE,
            0L,
            true,
            ownerId
        );
        BastionTiming newTiming = new BastionTiming(0L, 0L, gameTime, timing.offlineAccumTicks());
        return new BastionData(
            id,
            BastionState.ACTIVE,
            corePos,
            dimension,
            bastionType,
            primaryDao,
            tier,
            evolutionProgress,
            totalNodes,
            nodesByTier,
            growthRadius,
            growthCursor,
            resourcePool,
            pollution,
            counts,
            modifiers,
            newTiming,
            capturedState,
            lastBossSpawnGameTime,
            talentData
        );
    }

    /**
     * 创建 Boss 生成时间更新后的副本。
     *
     * @param gameTime 最新的 Boss 生成游戏时间
     * @return 更新后的 BastionData
     */
    public BastionData withLastBossSpawnGameTime(long gameTime) {
        return new BastionData(
            id, state, corePos, dimension, bastionType, primaryDao, tier,
            evolutionProgress, totalNodes, nodesByTier, growthRadius,
            growthCursor, resourcePool, pollution, counts, modifiers, timing, captureState,
            gameTime,
            talentData
        );
    }

    /**
     * 是否已被接管。
     *
     * @return true 表示基地处于友方（已接管）模式
     */
    public boolean isCaptured() {
        CaptureState safe = captureState == null ? CaptureState.DEFAULT : captureState;
        return safe.captured();
    }

    /**
     * 判断指定玩家是否为接管者。
     *
     * @param playerId 玩家 UUID
     * @return true 表示该玩家为接管者
     */
    public boolean isFriendlyTo(java.util.UUID playerId) {
        CaptureState safe = captureState == null ? CaptureState.DEFAULT : captureState;
        return safe.isCapturedBy(playerId);
    }

}
