package com.Kizunad.guzhenrenext.bastion.config;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基地类型配置数据。
 * <p>
 * 从 data/guzhenrenext/bastion_type/*.json 加载，定义基地的基础属性、
 * 刷怪配置、扩张参数等。
 * </p>
 *
 * @param id              配置唯一标识符（与文件名一致）
 * @param displayName     显示名称（用于 UI）
 * @param primaryDao      主道途类型
 * @param maxTier         最大转数（1-9）
 * @param spawning        刷怪配置
 * @param expansion       扩张配置
 * @param evolution       进化配置
 * @param loot            战利品配置（可选）
 * @param highTier        高转内容配置（可选，7-9 转专属）
 */
public record BastionTypeConfig(
        String id,
        String displayName,
        BastionDao primaryDao,
        int maxTier,
        SpawningConfig spawning,
        ExpansionConfig expansion,
        EvolutionConfig evolution,
        Optional<LootConfig> loot,
        Optional<HighTierConfig> highTier
) {

    /** 序列化/反序列化编解码器。 */
    public static final Codec<BastionTypeConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(BastionTypeConfig::id),
            Codec.STRING.fieldOf("display_name").forGetter(BastionTypeConfig::displayName),
            BastionDao.CODEC.fieldOf("primary_dao").forGetter(BastionTypeConfig::primaryDao),
            Codec.INT.optionalFieldOf("max_tier", DefaultValues.DEFAULT_MAX_TIER)
                .forGetter(BastionTypeConfig::maxTier),
            SpawningConfig.CODEC.optionalFieldOf("spawning", SpawningConfig.DEFAULT)
                .forGetter(BastionTypeConfig::spawning),
            ExpansionConfig.CODEC.optionalFieldOf("expansion", ExpansionConfig.DEFAULT)
                .forGetter(BastionTypeConfig::expansion),
            EvolutionConfig.CODEC.optionalFieldOf("evolution", EvolutionConfig.DEFAULT)
                .forGetter(BastionTypeConfig::evolution),
            LootConfig.CODEC.optionalFieldOf("loot").forGetter(BastionTypeConfig::loot),
            HighTierConfig.CODEC.optionalFieldOf("high_tier").forGetter(BastionTypeConfig::highTier)
        ).apply(instance, BastionTypeConfig::new)
    );

    /**
     * 默认值常量。
     */
    private static final class DefaultValues {
        static final int DEFAULT_MAX_TIER = 6;
        static final double DEFAULT_SPAWN_CHANCE = 0.1;
        static final double DEFAULT_TIER_SPAWN_BONUS = 0.05;
        static final int DEFAULT_MAX_SPAWNS = 2;
        static final double DEFAULT_EXPANSION_COST = 5.0;
        static final double DEFAULT_COST_MULTIPLIER = 1.2;
        static final int DEFAULT_MAX_RADIUS = 64;
        static final int DEFAULT_MAX_PER_TICK = 3;
        static final long DEFAULT_BASE_EVOLUTION_TICKS = 72000L;
        static final int DEFAULT_EVOLUTION_MULTIPLIER = 3;

        private DefaultValues() {
        }
    }

    /**
     * 刷怪配置。
     *
     * @param baseSpawnChance   基础刷怪概率（每刻）
     * @param tierSpawnBonus    转数对刷怪概率的加成
     * @param maxSpawnsPerTick  每刻最大刷怪数
     * @param entityWeights     实体类型权重列表
     */
    public record SpawningConfig(
            double baseSpawnChance,
            double tierSpawnBonus,
            int maxSpawnsPerTick,
            List<EntityWeight> entityWeights
    ) {
        public static final SpawningConfig DEFAULT = new SpawningConfig(
            DefaultValues.DEFAULT_SPAWN_CHANCE,
            DefaultValues.DEFAULT_TIER_SPAWN_BONUS,
            DefaultValues.DEFAULT_MAX_SPAWNS,
            List.of()
        );

        public static final Codec<SpawningConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("base_spawn_chance", DefaultValues.DEFAULT_SPAWN_CHANCE)
                    .forGetter(SpawningConfig::baseSpawnChance),
                Codec.DOUBLE.optionalFieldOf("tier_spawn_bonus", DefaultValues.DEFAULT_TIER_SPAWN_BONUS)
                    .forGetter(SpawningConfig::tierSpawnBonus),
                Codec.INT.optionalFieldOf("max_spawns_per_tick", DefaultValues.DEFAULT_MAX_SPAWNS)
                    .forGetter(SpawningConfig::maxSpawnsPerTick),
                EntityWeight.CODEC.listOf().optionalFieldOf("entity_weights", List.of())
                    .forGetter(SpawningConfig::entityWeights)
            ).apply(instance, SpawningConfig::new)
        );
    }

    /**
     * 实体权重配置。
     *
     * @param entityType 实体类型 ID（如 "minecraft:zombie"）
     * @param weight     权重值
     * @param minTier    最低转数要求
     */
    public record EntityWeight(
            String entityType,
            int weight,
            int minTier
    ) {
        public static final Codec<EntityWeight> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("entity_type").forGetter(EntityWeight::entityType),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(EntityWeight::weight),
                Codec.INT.optionalFieldOf("min_tier", 1).forGetter(EntityWeight::minTier)
            ).apply(instance, EntityWeight::new)
        );
    }

    /**
     * 扩张配置。
     *
     * @param baseCost       基础扩张成本
     * @param tierMultiplier 转数成本倍率
     * @param maxRadius      最大扩张半径
     * @param maxPerTick     每刻最大扩张次数
     */
    public record ExpansionConfig(
            double baseCost,
            double tierMultiplier,
            int maxRadius,
            int maxPerTick
    ) {
        public static final ExpansionConfig DEFAULT = new ExpansionConfig(
            DefaultValues.DEFAULT_EXPANSION_COST,
            DefaultValues.DEFAULT_COST_MULTIPLIER,
            DefaultValues.DEFAULT_MAX_RADIUS,
            DefaultValues.DEFAULT_MAX_PER_TICK
        );

        public static final Codec<ExpansionConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("base_cost", DefaultValues.DEFAULT_EXPANSION_COST)
                    .forGetter(ExpansionConfig::baseCost),
                Codec.DOUBLE.optionalFieldOf("tier_multiplier", DefaultValues.DEFAULT_COST_MULTIPLIER)
                    .forGetter(ExpansionConfig::tierMultiplier),
                Codec.INT.optionalFieldOf("max_radius", DefaultValues.DEFAULT_MAX_RADIUS)
                    .forGetter(ExpansionConfig::maxRadius),
                Codec.INT.optionalFieldOf("max_per_tick", DefaultValues.DEFAULT_MAX_PER_TICK)
                    .forGetter(ExpansionConfig::maxPerTick)
            ).apply(instance, ExpansionConfig::new)
        );
    }

    /**
     * 进化配置。
     *
     * @param baseEvolutionTicks 基础进化时间（刻）
     * @param tierMultiplier     转数时间倍率
     */
    public record EvolutionConfig(
            long baseEvolutionTicks,
            int tierMultiplier
    ) {
        public static final EvolutionConfig DEFAULT = new EvolutionConfig(
            DefaultValues.DEFAULT_BASE_EVOLUTION_TICKS,
            DefaultValues.DEFAULT_EVOLUTION_MULTIPLIER
        );

        public static final Codec<EvolutionConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.LONG.optionalFieldOf("base_evolution_ticks", DefaultValues.DEFAULT_BASE_EVOLUTION_TICKS)
                    .forGetter(EvolutionConfig::baseEvolutionTicks),
                Codec.INT.optionalFieldOf("tier_multiplier", DefaultValues.DEFAULT_EVOLUTION_MULTIPLIER)
                    .forGetter(EvolutionConfig::tierMultiplier)
            ).apply(instance, EvolutionConfig::new)
        );
    }

    /**
     * 战利品配置。
     *
     * @param lootTable 战利品表 ID
     * @param dropOnDestroy 销毁时是否掉落
     * @param dropOnCapture 占领时是否掉落
     */
    public record LootConfig(
            String lootTable,
            boolean dropOnDestroy,
            boolean dropOnCapture
    ) {
        public static final Codec<LootConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("loot_table").forGetter(LootConfig::lootTable),
                Codec.BOOL.optionalFieldOf("drop_on_destroy", true)
                    .forGetter(LootConfig::dropOnDestroy),
                Codec.BOOL.optionalFieldOf("drop_on_capture", true)
                    .forGetter(LootConfig::dropOnCapture)
            ).apply(instance, LootConfig::new)
        );
    }

    // ===== 高转内容配置（7-9 转）=====

    /**
     * 高转内容配置。
     * <p>
     * 定义 7-9 转基地的道痕阈值、技能列表和特殊效果。
     * </p>
     *
     * @param tierThresholds 各转数的道痕阈值配置
     * @param skills         技能/杀招列表
     * @param specialEffects 特殊效果 ID 列表
     */
    public record HighTierConfig(
            List<TierThreshold> tierThresholds,
            List<SkillEntry> skills,
            List<String> specialEffects
    ) {
        public static final HighTierConfig DEFAULT = new HighTierConfig(
            List.of(),
            List.of(),
            List.of()
        );

        public static final Codec<HighTierConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                TierThreshold.CODEC.listOf()
                    .optionalFieldOf("tier_thresholds", List.of())
                    .forGetter(HighTierConfig::tierThresholds),
                SkillEntry.CODEC.listOf()
                    .optionalFieldOf("skills", List.of())
                    .forGetter(HighTierConfig::skills),
                Codec.STRING.listOf()
                    .optionalFieldOf("special_effects", List.of())
                    .forGetter(HighTierConfig::specialEffects)
            ).apply(instance, HighTierConfig::new)
        );

        /**
         * 根据转数获取阈值配置。
         *
         * @param tier 转数
         * @return 阈值配置，如果没有定义则返回 null
         */
        public TierThreshold getThresholdForTier(int tier) {
            return tierThresholds.stream()
                .filter(t -> t.tier() == tier)
                .findFirst()
                .orElse(null);
        }

        /**
         * 获取指定转数可用的技能列表。
         *
         * @param tier 转数
         * @return 可用技能列表
         */
        public List<SkillEntry> getSkillsForTier(int tier) {
            return skills.stream()
                .filter(s -> s.minTier() <= tier)
                .toList();
        }
    }

    /**
     * 转数阈值配置。
     * <p>
     * 定义达到特定转数所需的道痕数量和其他条件。
     * </p>
     *
     * @param tier               转数（7-9）
     * @param requiredEnergyPool 所需基地能量池数量
     * @param requiredNodes      所需节点数量
     * @param evolutionTicks     进化所需刻数
     * @param bonusMultiplier    效果加成倍率
     */
    public record TierThreshold(
            int tier,
            double requiredEnergyPool,
            int requiredNodes,
            long evolutionTicks,
            double bonusMultiplier
    ) {
        /** 7 转默认能量池阈值。 */
        private static final double TIER_7_ENERGY_POOL = 10000.0;
        /** 8 转默认能量池阈值。 */
        private static final double TIER_8_ENERGY_POOL = 50000.0;
        /** 9 转默认能量池阈值。 */
        private static final double TIER_9_ENERGY_POOL = 200000.0;
        /** 默认节点数。 */
        private static final int DEFAULT_NODES = 100;
        /** 默认进化刻数。 */
        private static final long DEFAULT_EVOLUTION_TICKS = 144000L;
        /** 默认加成倍率。 */
        private static final double DEFAULT_BONUS = 1.0;

        public static final Codec<TierThreshold> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("tier").forGetter(TierThreshold::tier),
                Codec.DOUBLE.optionalFieldOf("required_energy_pool", TIER_7_ENERGY_POOL)
                    .forGetter(TierThreshold::requiredEnergyPool),
                Codec.INT.optionalFieldOf("required_nodes", DEFAULT_NODES)
                    .forGetter(TierThreshold::requiredNodes),
                Codec.LONG.optionalFieldOf("evolution_ticks", DEFAULT_EVOLUTION_TICKS)
                    .forGetter(TierThreshold::evolutionTicks),
                Codec.DOUBLE.optionalFieldOf("bonus_multiplier", DEFAULT_BONUS)
                    .forGetter(TierThreshold::bonusMultiplier)
            ).apply(instance, TierThreshold::new)
        );
    }

    /**
     * 技能/杀招条目配置。
     *
     * @param skillId     技能 ID（对应杀招 JSON 的 shazhaoID）
     * @param displayName 显示名称
     * @param minTier     最低解锁转数
     * @param category    技能类别（active/passive）
     * @param metadata    额外元数据
     */
    public record SkillEntry(
            String skillId,
            String displayName,
            int minTier,
            String category,
            Map<String, String> metadata
    ) {
        /** 默认类别。 */
        private static final String DEFAULT_CATEGORY = "active";

        public static final Codec<SkillEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("skill_id").forGetter(SkillEntry::skillId),
                Codec.STRING.optionalFieldOf("display_name", "")
                    .forGetter(SkillEntry::displayName),
                Codec.INT.optionalFieldOf("min_tier", 1)
                    .forGetter(SkillEntry::minTier),
                Codec.STRING.optionalFieldOf("category", DEFAULT_CATEGORY)
                    .forGetter(SkillEntry::category),
                Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .optionalFieldOf("metadata", Map.of())
                    .forGetter(SkillEntry::metadata)
            ).apply(instance, SkillEntry::new)
        );
    }
}
