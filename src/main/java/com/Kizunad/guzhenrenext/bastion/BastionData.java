package com.Kizunad.guzhenrenext.bastion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
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
        BastionTiming timing
) {

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
            BastionTiming.CODEC.fieldOf("timing").forGetter(BastionData::timing)
        ).apply(instance, BastionData::new)
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
            BastionTiming.createDefault(gameTime)
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
            growthCursor, resourcePool, timing
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
            growthCursor, resourcePool, newTiming
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
            growthCursor, resourcePool, newTiming
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
            growthCursor, resourcePool, timing
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
            growthCursor, resourcePool, timing
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
            newCursor, resourcePool, timing
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
            growthCursor, newPool, timing
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
            growthCursor, resourcePool, newTiming
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
            growthCursor, resourcePool, newTiming
        );
    }
}
