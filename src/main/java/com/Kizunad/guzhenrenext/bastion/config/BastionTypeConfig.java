package com.Kizunad.guzhenrenext.bastion.config;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
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
 * @param upkeep          守卫维护费（upkeep）配置（兼容旧 JSON 缺省）
 * @param spawning        刷怪配置
 * @param expansion       扩张配置
 * @param connectivity    连通性扫描配置（连通性为非实时：按周期触发、按预算推进 BFS）
 * @param decay           菌毯衰败配置（倒计时/预算/间隔；缺省回退旧常量，保证旧 JSON 行为不变）
 * @param evolution       进化配置
 * @param aura            光环配置（影响半径与衰减）
 * @param energy          能源节点配置（影响资源池增长的额外加成）
 * @param hatchery        守卫孵化巢配置（Round 4.2：守卫产出机制地基，缺省为未启用）
 * @param elite           精英守卫配置（Round 7.1：倍率与技能池，缺省未启用）
 * @param anchorsWeight   有效节点数计算：Anchor 权重（缺省回退 10，保证旧 JSON 行为不变）
 * @param myceliumWeight  有效节点数计算：菌毯权重（缺省回退 1，保证旧 JSON 行为不变）
 * @param loot            战利品配置（可选）
 * @param highTier        高转内容配置（可选，7-9 转专属）
 */
public record BastionTypeConfig(
        String id,
        String displayName,
        BastionDao primaryDao,
        int maxTier,
        UpkeepConfig upkeep,
        SpawningConfig spawning,
         ExpansionConfig expansion,
         ConnectivityConfig connectivity,
         ShellConfig shell,
         DecayConfig decay,
         EvolutionConfig evolution,
         AuraConfig aura,
         EnergyConfig energy,
         HatcheryConfig hatchery,
         EliteConfig elite,
         int anchorsWeight,
         int myceliumWeight,
         Optional<LootConfig> loot,
         Optional<HighTierConfig> highTier,
         Optional<GuardianShazhaoConfig> guardianShazhao
) {

    /**
     * 有效节点数计算：Anchor 权重默认值。
     * <p>
     * 兼容策略：旧版 bastionType JSON 若缺失 anchors_weight 字段，必须回退到该默认值，
     * 以保证行为与改动前一致。
     * </p>
     */
    public static final int DEFAULT_ANCHORS_WEIGHT = DefaultValues.DEFAULT_ANCHORS_WEIGHT;

    /**
     * 有效节点数计算：菌毯权重默认值。
     * <p>
     * 兼容策略：旧版 bastionType JSON 若缺失 mycelium_weight 字段，必须回退到该默认值。
     * </p>
     */
    public static final int DEFAULT_MYCELIUM_WEIGHT = DefaultValues.DEFAULT_MYCELIUM_WEIGHT;

    /**
     * 可选内容配置（用于 codec 分组）。
     * <p>
     * 注意：BastionTypeConfig 字段较多，RecordCodecBuilder 的 group 有 16 参数限制。
     * 这里使用 {@link MapCodec} 把 shell/elite 以及可选字段（loot/high_tier/guardian_shazhao）打包为 1 组，
     * 但 JSON schema 不变：仍然是根对象上的字段（不会引入额外嵌套对象）。
     * </p>
     */
    private static final class OptionalContentConfig {
        private final ShellConfig shell;
        private final EliteConfig elite;
        private final Optional<LootConfig> loot;
        private final Optional<HighTierConfig> highTier;
        private final Optional<GuardianShazhaoConfig> guardianShazhao;

        private OptionalContentConfig(
                ShellConfig shell,
                EliteConfig elite,
                Optional<LootConfig> loot,
                Optional<HighTierConfig> highTier,
                Optional<GuardianShazhaoConfig> guardianShazhao
        ) {
            this.shell = shell;
            this.elite = elite;
            this.loot = loot;
            this.highTier = highTier;
            this.guardianShazhao = guardianShazhao;
        }

        private ShellConfig shell() {
            return shell;
        }

        private EliteConfig elite() {
            return elite;
        }

        private Optional<LootConfig> loot() {
            return loot;
        }

        private Optional<HighTierConfig> highTier() {
            return highTier;
        }

        private Optional<GuardianShazhaoConfig> guardianShazhao() {
            return guardianShazhao;
        }

        private static final MapCodec<OptionalContentConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                ShellConfig.CODEC.optionalFieldOf("shell", ShellConfig.DEFAULT)
                    .forGetter(OptionalContentConfig::shell),
                EliteConfig.CODEC.optionalFieldOf("elite", EliteConfig.DEFAULT)
                    .forGetter(OptionalContentConfig::elite),
                LootConfig.CODEC.optionalFieldOf("loot").forGetter(OptionalContentConfig::loot),
                HighTierConfig.CODEC.optionalFieldOf("high_tier").forGetter(OptionalContentConfig::highTier),
                GuardianShazhaoConfig.CODEC.optionalFieldOf("guardian_shazhao")
                    .forGetter(OptionalContentConfig::guardianShazhao)
            ).apply(instance, OptionalContentConfig::new)
        );
    }

    /**
     * 精英守卫配置。
     * <p>
     * Round 7.1：定义精英守卫的属性倍率和技能池。
     * </p>
     */
    public record EliteConfig(
            boolean enabled,
            double healthMultiplier,
            double damageMultiplier,
            double armorMultiplier,
            double speedMultiplier,
            List<String> skillPool
    ) {
        public static final EliteConfig DEFAULT = new EliteConfig(
            DefaultValues.DEFAULT_ELITE_ENABLED,
            DefaultValues.DEFAULT_ELITE_HEALTH_MULTIPLIER,
            DefaultValues.DEFAULT_ELITE_DAMAGE_MULTIPLIER,
            DefaultValues.DEFAULT_ELITE_ARMOR_MULTIPLIER,
            DefaultValues.DEFAULT_ELITE_SPEED_MULTIPLIER,
            List.of()
        );

        public static final Codec<EliteConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("enabled", DefaultValues.DEFAULT_ELITE_ENABLED)
                    .forGetter(EliteConfig::enabled),
                Codec.DOUBLE.optionalFieldOf(
                        "health_multiplier",
                        DefaultValues.DEFAULT_ELITE_HEALTH_MULTIPLIER)
                    .forGetter(EliteConfig::healthMultiplier),
                Codec.DOUBLE.optionalFieldOf(
                        "damage_multiplier",
                        DefaultValues.DEFAULT_ELITE_DAMAGE_MULTIPLIER)
                    .forGetter(EliteConfig::damageMultiplier),
                Codec.DOUBLE.optionalFieldOf(
                        "armor_multiplier",
                        DefaultValues.DEFAULT_ELITE_ARMOR_MULTIPLIER)
                    .forGetter(EliteConfig::armorMultiplier),
                Codec.DOUBLE.optionalFieldOf(
                        "speed_multiplier",
                        DefaultValues.DEFAULT_ELITE_SPEED_MULTIPLIER)
                    .forGetter(EliteConfig::speedMultiplier),
                Codec.STRING.listOf().optionalFieldOf("skill_pool", List.of())
                    .forGetter(EliteConfig::skillPool)
            ).apply(instance, EliteConfig::new)
        );
    }

    /** 序列化/反序列化编解码器。 */
    public static final Codec<BastionTypeConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(BastionTypeConfig::id),
            Codec.STRING.fieldOf("display_name").forGetter(BastionTypeConfig::displayName),
            BastionDao.CODEC.fieldOf("primary_dao").forGetter(BastionTypeConfig::primaryDao),
            Codec.INT.optionalFieldOf("max_tier", DefaultValues.DEFAULT_MAX_TIER)
                .forGetter(BastionTypeConfig::maxTier),
            UpkeepConfig.CODEC.optionalFieldOf("upkeep", UpkeepConfig.DEFAULT)
                .forGetter(BastionTypeConfig::upkeep),
            SpawningConfig.CODEC.optionalFieldOf("spawning", SpawningConfig.DEFAULT)
                .forGetter(BastionTypeConfig::spawning),
            ExpansionConfig.CODEC.optionalFieldOf("expansion", ExpansionConfig.DEFAULT)
                .forGetter(BastionTypeConfig::expansion),
            ConnectivityConfig.CODEC.optionalFieldOf("connectivity", ConnectivityConfig.DEFAULT)
                .forGetter(BastionTypeConfig::connectivity),
            // 外壳（甲壳）配置：Round 6.2 引入，默认未启用，兼容旧 JSON。
            // 与可选内容一起打包以规避 RecordCodecBuilder 16 参数限制，同时保持 JSON 扁平。
            DecayConfig.CODEC.optionalFieldOf("decay", DecayConfig.DEFAULT)
                .forGetter(BastionTypeConfig::decay),
            EvolutionConfig.CODEC.optionalFieldOf("evolution", EvolutionConfig.DEFAULT)
                .forGetter(BastionTypeConfig::evolution),
            AuraConfig.CODEC.optionalFieldOf("aura", AuraConfig.DEFAULT)
                .forGetter(BastionTypeConfig::aura),
            EnergyConfig.CODEC.optionalFieldOf("energy", EnergyConfig.DEFAULT)
                .forGetter(BastionTypeConfig::energy),
            HatcheryConfig.CODEC.optionalFieldOf("hatchery", HatcheryConfig.DEFAULT)
                .forGetter(BastionTypeConfig::hatchery),
            // 有效节点数（effectiveNodes）权重配置：
            // - anchorsWeight：Anchor（子核心/支撑节点）的权重
            // - myceliumWeight：菌毯（贴地蔓延主网）的权重
            // 兼容策略：旧版 JSON 若缺失该字段，必须回退为 10/1，保持当前游戏平衡不被破坏。
            Codec.INT.optionalFieldOf("anchors_weight", DefaultValues.DEFAULT_ANCHORS_WEIGHT)
                .forGetter(BastionTypeConfig::anchorsWeight),
            Codec.INT.optionalFieldOf("mycelium_weight", DefaultValues.DEFAULT_MYCELIUM_WEIGHT)
                .forGetter(BastionTypeConfig::myceliumWeight),
            // 可选内容配置。
            // 注意：这里用 MapCodec 进行字段分组以规避 16 参数限制，但字段仍位于根对象。
            OptionalContentConfig.MAP_CODEC.forGetter(config -> new OptionalContentConfig(
                config.shell(),
                config.elite(),
                config.loot(),
                config.highTier(),
                config.guardianShazhao()
            ))
        ).apply(instance, (id,
                displayName,
                primaryDao,
                maxTier,
                upkeep,
                spawning,
                expansion,
                connectivity,
                decay,
                evolution,
                aura,
                energy,
                hatchery,
                anchorsWeight,
                myceliumWeight,
                optionalContent) -> new BastionTypeConfig(
            id,
            displayName,
            primaryDao,
            maxTier,
            upkeep,
            spawning,
            expansion,
            connectivity,
            optionalContent.shell(),
            decay,
            evolution,
            aura,
            energy,
            hatchery,
            optionalContent.elite(),
            anchorsWeight,
            myceliumWeight,
            optionalContent.loot(),
            optionalContent.highTier(),
            optionalContent.guardianShazhao()
        ))
    );

    // ===== 守卫孵化巢（hatchery）配置 =====

    /**
     * 守卫孵化巢（GuardianHatchery）配置。
     * <p>
     * Round 4.2 的目标是提供“产出机制地基”：当基地资源池足够时，按配置周期性产出守卫。
     * 本配置缺省必须是“未启用”，以保证旧世界/旧 JSON 在不新增字段时行为完全不变。
     * </p>
     * <p>
     * 重要语义：
     * <ul>
     *     <li>enabled=false 时，无论世界中是否存在孵化巢方块，都不产出。</li>
     *     <li>cooldownTicks 为“基地级”冷却：同一基地多个孵化巢也共用节流（避免刷屏/爆量）。</li>
     *     <li>maxAlive 仅统计属于该基地的守卫（由 {@code BastionGuardianData} 判定）。</li>
     * </ul>
     * </p>
     */
    public record HatcheryConfig(
            boolean enabled,
            int autoSpawnThreshold,
            long cooldownTicks,
            int spawnPerCycle,
            int maxAlive,
            double costPerSpawn,
            /**
             * 孵化巢连通性校验：Anchor 邻近半径。
             * <p>
             * Round 4.2.2：孵化巢必须满足“菌毯网络连通 + 周围 M 格内存在属于该基地的 Anchor”，
             * 其中 M 由该字段控制。
             * </p>
             * <p>
             * 兼容策略：旧版 bastion_type JSON 若缺失该字段，必须回退到默认值，
             * 以保证旧配置无需新增字段也能正常加载。
             * </p>
             */
            int anchorProximityRadius,
            GuardianWeights weights
    ) {
        public static final HatcheryConfig DEFAULT = new HatcheryConfig(
            false,
            DefaultValues.DEFAULT_HATCHERY_AUTO_SPAWN_THRESHOLD,
            DefaultValues.DEFAULT_HATCHERY_COOLDOWN_TICKS,
            DefaultValues.DEFAULT_HATCHERY_SPAWN_PER_CYCLE,
            DefaultValues.DEFAULT_HATCHERY_MAX_ALIVE,
            DefaultValues.DEFAULT_HATCHERY_COST_PER_SPAWN,
            DefaultValues.DEFAULT_HATCHERY_ANCHOR_PROXIMITY_RADIUS,
            GuardianWeights.DEFAULT
        );

        public static final Codec<HatcheryConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("enabled", false)
                    .forGetter(HatcheryConfig::enabled),
                Codec.INT.optionalFieldOf(
                        "auto_spawn_threshold",
                        DefaultValues.DEFAULT_HATCHERY_AUTO_SPAWN_THRESHOLD)
                    .forGetter(HatcheryConfig::autoSpawnThreshold),
                Codec.LONG.optionalFieldOf(
                        "cooldown_ticks",
                        DefaultValues.DEFAULT_HATCHERY_COOLDOWN_TICKS)
                    .forGetter(HatcheryConfig::cooldownTicks),
                Codec.INT.optionalFieldOf(
                        "spawn_per_cycle",
                        DefaultValues.DEFAULT_HATCHERY_SPAWN_PER_CYCLE)
                    .forGetter(HatcheryConfig::spawnPerCycle),
                Codec.INT.optionalFieldOf(
                        "max_alive",
                        DefaultValues.DEFAULT_HATCHERY_MAX_ALIVE)
                    .forGetter(HatcheryConfig::maxAlive),
                Codec.DOUBLE.optionalFieldOf(
                        "cost_per_spawn",
                        DefaultValues.DEFAULT_HATCHERY_COST_PER_SPAWN)
                    .forGetter(HatcheryConfig::costPerSpawn),
                Codec.INT.optionalFieldOf(
                        "anchor_proximity_radius",
                        DefaultValues.DEFAULT_HATCHERY_ANCHOR_PROXIMITY_RADIUS)
                    .forGetter(HatcheryConfig::anchorProximityRadius),
                GuardianWeights.CODEC.optionalFieldOf("weights", GuardianWeights.DEFAULT)
                    .forGetter(HatcheryConfig::weights)
            ).apply(instance, HatcheryConfig::new)
        );
    }

    /**
     * 守卫类型权重配置。
     * <p>
     * Round 4.2 只要求最小落点：minion/ranged/support 按比例抽取。
     * elite/boss 预留权重接口，默认 0（不参与抽取）。
     * </p>
     */
    public record GuardianWeights(int minion, int ranged, int support, int elite, int boss) {
        public static final GuardianWeights DEFAULT = new GuardianWeights(
            DefaultValues.DEFAULT_HATCHERY_WEIGHT_MINION,
            DefaultValues.DEFAULT_HATCHERY_WEIGHT_RANGED,
            DefaultValues.DEFAULT_HATCHERY_WEIGHT_SUPPORT,
            DefaultValues.DEFAULT_HATCHERY_WEIGHT_ELITE,
            DefaultValues.DEFAULT_HATCHERY_WEIGHT_BOSS
        );

        public static final Codec<GuardianWeights> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.optionalFieldOf(
                        "minion",
                        DefaultValues.DEFAULT_HATCHERY_WEIGHT_MINION)
                    .forGetter(GuardianWeights::minion),
                Codec.INT.optionalFieldOf(
                        "ranged",
                        DefaultValues.DEFAULT_HATCHERY_WEIGHT_RANGED)
                    .forGetter(GuardianWeights::ranged),
                Codec.INT.optionalFieldOf(
                        "support",
                        DefaultValues.DEFAULT_HATCHERY_WEIGHT_SUPPORT)
                    .forGetter(GuardianWeights::support),
                Codec.INT.optionalFieldOf(
                        "elite",
                        DefaultValues.DEFAULT_HATCHERY_WEIGHT_ELITE)
                    .forGetter(GuardianWeights::elite),
                Codec.INT.optionalFieldOf(
                        "boss",
                        DefaultValues.DEFAULT_HATCHERY_WEIGHT_BOSS)
                    .forGetter(GuardianWeights::boss)
            ).apply(instance, GuardianWeights::new)
        );
    }

    // ===== 守卫维护费（upkeep）配置 =====

    /**
     * 守卫维护费（upkeep）配置。
     * <p>
     * Round 4.1 的目标：把“守卫扣费 + 停机阈值”从代码常量下沉到 bastion_type JSON，便于不同道途独立调参。
     * </p>
     * <p>
     * 兼容策略：旧版 bastion_type JSON 若缺失 upkeep 字段或其子字段，必须回退到旧行为：
     * <ul>
     *   <li>{@code per_guardian_cost} 缺省回退为 1.0（对齐 BastionGuardianUpkeepService 旧常量）。</li>
     *   <li>{@code shutdown_threshold} 缺省回退为 0.0（对齐 BastionSpawnService 旧门禁：resourcePool<=0）。</li>
     * </ul>
     * </p>
     * <p>
     * 重要语义说明：停机门禁采用“含阈值”的比较（{@code resourcePool <= shutdown_threshold}）。
     * 这样在默认阈值为 0.0 时，仍能严格复现旧逻辑“资源池为 0 即停机”。
     * </p>
     *
     * @param perGuardianCost   每个守卫在一个维护间隔（tickInterval）内的维护费用
     * @param shutdownThreshold 刷怪停机阈值（含）：资源池 <= 阈值时禁止继续刷出新守卫
     */
    public record UpkeepConfig(double perGuardianCost, double shutdownThreshold) {
        public static final UpkeepConfig DEFAULT = new UpkeepConfig(
            DefaultValues.DEFAULT_UPKEEP_PER_GUARDIAN_COST,
            DefaultValues.DEFAULT_UPKEEP_SHUTDOWN_THRESHOLD
        );

        public static final Codec<UpkeepConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf(
                        "per_guardian_cost",
                        DefaultValues.DEFAULT_UPKEEP_PER_GUARDIAN_COST)
                    .forGetter(UpkeepConfig::perGuardianCost),
                Codec.DOUBLE.optionalFieldOf(
                        "shutdown_threshold",
                        DefaultValues.DEFAULT_UPKEEP_SHUTDOWN_THRESHOLD)
                    .forGetter(UpkeepConfig::shutdownThreshold)
            ).apply(instance, UpkeepConfig::new)
        );
    }

    // ===== 衰败配置（菌毯断连后的倒计时） =====

    /**
     * 衰败（decay）配置。
     * <p>
     * 用途：当菌毯节点与基地网络断连后，会进入衰败倒计时；倒计时到期后移除方块。
     * 本配置将“倒计时/预算/间隔”从代码常量下沉到 bastion_type JSON，便于不同基地类型调参。
     * </p>
     * <p>
     * 兼容策略：旧版 bastion_type JSON 若缺失 decay 字段，必须回退到 v1 的常量行为。
     * </p>
     *
     * @param totalTicks   衰败总倒计时（tick）
     * @param tickInterval 衰败处理间隔（tick）
     * @param budgetNodes  单次衰败 tick 最大处理节点数（预算）
     */
    public record DecayConfig(int totalTicks, long tickInterval, int budgetNodes) {
        public static final DecayConfig DEFAULT = new DecayConfig(
            DefaultValues.DEFAULT_MYCELIUM_DECAY_TOTAL_TICKS,
            DefaultValues.DEFAULT_MYCELIUM_DECAY_TICK_INTERVAL,
            DefaultValues.DEFAULT_MYCELIUM_DECAY_BUDGET_NODES
        );

        public static final Codec<DecayConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.optionalFieldOf(
                        "total_ticks",
                        DefaultValues.DEFAULT_MYCELIUM_DECAY_TOTAL_TICKS)
                    .forGetter(DecayConfig::totalTicks),
                Codec.LONG.optionalFieldOf(
                        "tick_interval",
                        DefaultValues.DEFAULT_MYCELIUM_DECAY_TICK_INTERVAL)
                    .forGetter(DecayConfig::tickInterval),
                Codec.INT.optionalFieldOf(
                        "budget_nodes",
                        DefaultValues.DEFAULT_MYCELIUM_DECAY_BUDGET_NODES)
                    .forGetter(DecayConfig::budgetNodes)
            ).apply(instance, DecayConfig::new)
        );
    }

    /**
     * 守卫杀招配置。
     * <p>
     * 用途：让基地守卫在战斗中按权重选择“主动杀招”（复用 shazhao JSON 配置格式），
     * 但不走玩家的解锁/物品/消耗校验。
     * </p>
     *
     * @param activePool 主动杀招池（按权重随机；可按 minTier 分层）
     */
    public record GuardianShazhaoConfig(List<WeightedShazhao> activePool) {
        public static final GuardianShazhaoConfig DEFAULT = new GuardianShazhaoConfig(List.of());

        public static final Codec<GuardianShazhaoConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                WeightedShazhao.CODEC.listOf().optionalFieldOf("active_pool", List.of())
                    .forGetter(GuardianShazhaoConfig::activePool)
            ).apply(instance, GuardianShazhaoConfig::new)
        );
    }

    /**
     * 杀招权重条目。
     *
     * @param shazhaoId 杀招 ID（例如 guzhhenrenext:shazhao_active_xxx）
     * @param weight    权重
     * @param minTier   最低转数
     */
    public record WeightedShazhao(String shazhaoId, int weight, int minTier) {
        public static final Codec<WeightedShazhao> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("shazhao_id").forGetter(WeightedShazhao::shazhaoId),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedShazhao::weight),
                Codec.INT.optionalFieldOf("min_tier", 1).forGetter(WeightedShazhao::minTier)
            ).apply(instance, WeightedShazhao::new)
        );
    }

    // ===== 连通性扫描配置 =====

    /**
     * 连通性扫描配置。
     * <p>
     * 说明：基地连通性（核心/Anchor/菌毯网络）采用“非实时”的增量 BFS：
     * <ul>
     *   <li>按 {@link #scanIntervalTicks()} 周期触发一次扫描；</li>
     *   <li>每次 tick 仅消耗 {@link #bfsBudgetNodes()} 的节点预算推进 BFS，避免卡顿。</li>
     * </ul>
     * </p>
     * <p>
     * 兼容策略：旧版 bastion_type JSON 若缺失 connectivity 字段，必须回退到 v1 的常量行为。
     * </p>
     *
     * @param scanIntervalTicks 扫描间隔（tick）
     * @param bfsBudgetNodes    BFS 预算（单次调用最大处理节点数）
     */
    public record ConnectivityConfig(long scanIntervalTicks, int bfsBudgetNodes) {
        public static final ConnectivityConfig DEFAULT = new ConnectivityConfig(
            DefaultValues.DEFAULT_CONNECTIVITY_SCAN_INTERVAL_TICKS,
            DefaultValues.DEFAULT_CONNECTIVITY_BFS_BUDGET_NODES
        );

        public static final Codec<ConnectivityConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.LONG.optionalFieldOf(
                        "scan_interval_ticks",
                        DefaultValues.DEFAULT_CONNECTIVITY_SCAN_INTERVAL_TICKS)
                    .forGetter(ConnectivityConfig::scanIntervalTicks),
                Codec.INT.optionalFieldOf(
                        "bfs_budget_nodes",
                        DefaultValues.DEFAULT_CONNECTIVITY_BFS_BUDGET_NODES)
                    .forGetter(ConnectivityConfig::bfsBudgetNodes)
            ).apply(instance, ConnectivityConfig::new)
        );
    }

    // ===== 外壳（甲壳）配置 =====

    /**
     * 外壳（甲壳）配置。
     * <p>
     * Round 6.2：定义外壳再生的连通性要求和资源阈值。
     * </p>
     *
     * @param enabled           是否启用外壳系统
     * @param regenCostPerBlock 每个方块的再生成本
     * @param regenCooldownTicks再生冷却时间（tick）
     * @param maxShellBlocks    最大外壳方块数（0 表示不限制）
     * @param resourceThreshold 资源池阈值：低于此值停止再生
     */
    public record ShellConfig(
            boolean enabled,
            double regenCostPerBlock,
            long regenCooldownTicks,
            int maxShellBlocks,
            double resourceThreshold
    ) {
        public static final ShellConfig DEFAULT = new ShellConfig(
            DefaultValues.DEFAULT_SHELL_ENABLED,
            DefaultValues.DEFAULT_SHELL_REGEN_COST_PER_BLOCK,
            DefaultValues.DEFAULT_SHELL_REGEN_COOLDOWN_TICKS,
            DefaultValues.DEFAULT_SHELL_MAX_BLOCKS,
            DefaultValues.DEFAULT_SHELL_RESOURCE_THRESHOLD
        );

        public static final Codec<ShellConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("enabled", DefaultValues.DEFAULT_SHELL_ENABLED)
                    .forGetter(ShellConfig::enabled),
                Codec.DOUBLE.optionalFieldOf(
                        "regen_cost_per_block",
                        DefaultValues.DEFAULT_SHELL_REGEN_COST_PER_BLOCK)
                    .forGetter(ShellConfig::regenCostPerBlock),
                Codec.LONG.optionalFieldOf(
                        "regen_cooldown_ticks",
                        DefaultValues.DEFAULT_SHELL_REGEN_COOLDOWN_TICKS)
                    .forGetter(ShellConfig::regenCooldownTicks),
                Codec.INT.optionalFieldOf(
                        "max_shell_blocks",
                        DefaultValues.DEFAULT_SHELL_MAX_BLOCKS)
                    .forGetter(ShellConfig::maxShellBlocks),
                Codec.DOUBLE.optionalFieldOf(
                        "resource_threshold",
                        DefaultValues.DEFAULT_SHELL_RESOURCE_THRESHOLD)
                    .forGetter(ShellConfig::resourceThreshold)
            ).apply(instance, ShellConfig::new)
        );
    }

    /**
     * 默认值常量。
     */
    private static final class DefaultValues {
        static final int DEFAULT_MAX_TIER = 6;
        /** 有效节点数：Anchor 权重默认值（兼容旧 JSON）。 */
        static final int DEFAULT_ANCHORS_WEIGHT = 10;
        /** 有效节点数：菌毯权重默认值（兼容旧 JSON）。 */
        static final int DEFAULT_MYCELIUM_WEIGHT = 1;

        // ===== 连通性扫描默认值 =====
        /**
         * 连通性扫描间隔默认值（tick）。
         * <p>
         * 对齐 v1：BastionConnectivityService 内部旧常量 CONNECTIVITY_SCAN_INTERVAL_TICKS=40。
         * </p>
         */
        static final long DEFAULT_CONNECTIVITY_SCAN_INTERVAL_TICKS = 40L;

        /**
         * 连通性 BFS 预算默认值（节点数）。
         * <p>
         * 对齐 v1：BastionConnectivityService 内部旧常量 CONNECTIVITY_BFS_BUDGET_NODES=128。
         * </p>
         */
        static final int DEFAULT_CONNECTIVITY_BFS_BUDGET_NODES = 128;

        // ===== 外壳（甲壳）默认值 =====
        /** 是否启用外壳系统（默认关闭以兼容旧 JSON）。 */
        static final boolean DEFAULT_SHELL_ENABLED = false;
         /** 每个外壳方块的再生成本。 */
         static final double DEFAULT_SHELL_REGEN_COST_PER_BLOCK = 1.0;
         /** 外壳再生冷却时间（tick）。 */
         static final long DEFAULT_SHELL_REGEN_COOLDOWN_TICKS = 100L;
         /** 外壳方块数量上限（0 表示不限制）。 */
         static final int DEFAULT_SHELL_MAX_BLOCKS = 0;
         /** 资源池阈值：低于该值停止再生。 */
         static final double DEFAULT_SHELL_RESOURCE_THRESHOLD = 0.0;

         // ===== 精英守卫默认值（Round 7.1） =====
         /** 是否启用精英守卫系统，缺省关闭以兼容旧 JSON。 */
         static final boolean DEFAULT_ELITE_ENABLED = false;
         /** 精英生命倍率默认值。 */
         static final double DEFAULT_ELITE_HEALTH_MULTIPLIER = 2.0;
         /** 精英伤害倍率默认值。 */
         static final double DEFAULT_ELITE_DAMAGE_MULTIPLIER = 1.5;
         /** 精英护甲倍率默认值。 */
         static final double DEFAULT_ELITE_ARMOR_MULTIPLIER = 1.5;
         /** 精英移动速度倍率默认值。 */
         static final double DEFAULT_ELITE_SPEED_MULTIPLIER = 1.2;

        // ===== 菌毯衰败默认值 =====

        /**
         * 衰败总倒计时默认值（tick）。
         * <p>
         * 对齐 v1：BastionConnectivityService.Config.MYCELIUM_DECAY_TOTAL_TICKS=200。
         * </p>
         */
        static final int DEFAULT_MYCELIUM_DECAY_TOTAL_TICKS = 200;

        /**
         * 衰败处理间隔默认值（tick）。
         * <p>
         * 对齐 v1：BastionConnectivityService.Config.MYCELIUM_DECAY_TICK_INTERVAL=20。
         * </p>
         */
        static final long DEFAULT_MYCELIUM_DECAY_TICK_INTERVAL = 20L;

        /**
         * 单次衰败 tick 最大处理节点数默认值（预算）。
         * <p>
         * 对齐 v1：BastionConnectivityService.Config.MYCELIUM_DECAY_BUDGET_NODES=16。
         * </p>
         */
        static final int DEFAULT_MYCELIUM_DECAY_BUDGET_NODES = 16;

        // ===== 守卫维护费（upkeep）默认值 =====

        /**
         * 每个守卫每个维护间隔的维护费默认值。
         * <p>
         * 对齐 v1：BastionGuardianUpkeepService 内部旧常量 UPKEEP_COST_PER_GUARDIAN_PER_INTERVAL=1.0。
         * </p>
         */
        static final double DEFAULT_UPKEEP_PER_GUARDIAN_COST = 1.0;

        /**
         * 刷怪停机阈值默认值。
         * <p>
         * 对齐 v1：BastionSpawnService 的旧门禁为 resourcePool<=0，因此缺省应为 0.0。
         * </p>
         */
        static final double DEFAULT_UPKEEP_SHUTDOWN_THRESHOLD = 0.0;

        static final double DEFAULT_SPAWN_CHANCE = 0.1;
        static final double DEFAULT_TIER_SPAWN_BONUS = 0.05;
        static final int DEFAULT_MAX_SPAWNS = 2;
        static final double DEFAULT_EXPANSION_COST = 5.0;
        static final double DEFAULT_COST_MULTIPLIER = 1.2;
        static final int DEFAULT_MAX_RADIUS = 64;
        static final int DEFAULT_MAX_PER_TICK = 3;
        static final int DEFAULT_NODE_SPACING = 2;
        static final long DEFAULT_BASE_EVOLUTION_TICKS = 72000L;
        static final int DEFAULT_EVOLUTION_MULTIPLIER = 3;
        // Aura defaults: 1转=16, 6转=256, 9转=4096
        static final int DEFAULT_AURA_BASE_RADIUS = 16;
        static final double DEFAULT_AURA_TIER_EXPONENT = 2.0;
        static final int DEFAULT_AURA_MAX_RADIUS = 4096;
        static final double DEFAULT_FALLOFF_POWER = 2.0;
        static final double DEFAULT_MIN_FALLOFF = 0.05;
        // 缩圈相关默认值：refNodes 为各转"满状态"所需节点数
        static final List<Integer> DEFAULT_REF_NODES_BY_TIER = List.of(20, 40, 80, 150, 280, 500);
        static final double DEFAULT_MIN_SCALE = 0.3;
        /** 光环同类叠加默认：取最大值（兼容旧行为）。 */
        static final AuraStackingMode DEFAULT_SAME_TYPE_STACKING = AuraStackingMode.MAX;
        /** 光环异类默认：可同时生效（兼容旧行为）。 */
        static final boolean DEFAULT_DIFFERENT_TYPES_COEXIST = true;
        /** 光环节点建造成本默认值（兼容旧 JSON）。 */
        static final double DEFAULT_AURA_BUILD_COST = 0.0;
        /** 光环节点数量上限默认值（0 表示不启用上限，兼容旧 JSON）。 */
        static final int DEFAULT_AURA_MAX_COUNT = 0;

        // ===== 能源节点默认值 =====
        static final double DEFAULT_ENERGY_BUILD_COST = 0.0;
        static final int DEFAULT_ENERGY_MAX_COUNT = 0;
        static final double DEFAULT_POOL_GAIN_MULTIPLIER = 0.0;
        static final double DEFAULT_POOL_GAIN_FLAT = 0.0;
        static final int DEFAULT_SCAN_RADIUS = 8;

        // ===== 孵化巢默认值（Round 4.2） =====
        // 兼容策略：默认必须“未启用”，否则旧世界在未配置时会无意产出守卫。
        static final int DEFAULT_HATCHERY_AUTO_SPAWN_THRESHOLD = 32;
        static final long DEFAULT_HATCHERY_COOLDOWN_TICKS = 200L;
        static final int DEFAULT_HATCHERY_SPAWN_PER_CYCLE = 1;
        static final int DEFAULT_HATCHERY_MAX_ALIVE = 0;
        static final double DEFAULT_HATCHERY_COST_PER_SPAWN = 0.0;

        /**
         * Round 4.2.2：孵化巢邻近 Anchor 校验半径默认值。
         * <p>
         * 默认 8：用于“孵化巢周围 M 格内存在至少一个属于该基地的 Anchor”的连通性约束。
         * </p>
         */
        static final int DEFAULT_HATCHERY_ANCHOR_PROXIMITY_RADIUS = 8;

        // 权重默认值：为了让“未启用”的默认完全不生效，这里保持一个无害的权重基线。
        // 注意：真正的“是否产出”应由 enabled/maxAlive/cost 等多重门禁控制。
        static final int DEFAULT_HATCHERY_WEIGHT_MINION = 1;
        static final int DEFAULT_HATCHERY_WEIGHT_RANGED = 1;
        static final int DEFAULT_HATCHERY_WEIGHT_SUPPORT = 1;
        static final int DEFAULT_HATCHERY_WEIGHT_ELITE = 0;
        static final int DEFAULT_HATCHERY_WEIGHT_BOSS = 0;

        /**
         * 能源冲突优先级默认值。
         * <p>
         * Round 3.2 的关键：同一 Anchor 周边环境可能同时满足多个能源条件。
         * 在旧实现中，优先级被硬编码为：地热 &gt; 汲水 &gt; 光合。
         * </p>
         * <p>
         * 为保证旧配置/旧玩法的默认行为不变，当 bastion_type.energy.priority_order 缺失时，
         * 必须回退到该默认顺序。
         * </p>
         */
        static final List<com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType> DEFAULT_ENERGY_PRIORITY_ORDER =
            List.of(
                com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType.GEOTHERMAL,
                com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType.WATER_INTAKE,
                com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType.PHOTOSYNTHESIS
            );

        private DefaultValues() {
        }
    }

    // ===== 能源节点配置 =====

    /**
     * 能源节点配置。
     * <p>
     * 能源节点（挂载在 Anchor 上）为资源池增长提供额外加成。
     * 当前版本仅定义配置 schema + 默认值，不包含具体扫描逻辑。
     * </p>
     *
     * @param photosynthesis 光合：依赖天空光（skylight）的能源
     * @param waterIntake    汲水：依赖邻近水源的能源
     * @param geothermal     地热：依赖邻近岩浆/深层地形的能源
     */
    public record EnergyConfig(
            EnergyNodeConfig photosynthesis,
            EnergyNodeConfig waterIntake,
            EnergyNodeConfig geothermal,

            /**
             * 同一 Anchor 多条件满足时的最终能源类型选择顺序（冲突优先级）。
             * <p>
             * 说明：能源节点的环境判定可能同时满足多个条件（例如附近同时有水与岩浆）。
             * Round 3.2 要求将该优先级配置化：由 bastion_type.energy.priority_order 控制。
             * </p>
             * <p>
             * 兼容策略：旧 JSON 若缺失该字段，必须回退到 Round 3.1 的默认行为（地热 &gt; 汲水 &gt; 光合）。
             * </p>
             */
            List<com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType> priorityOrder
    ) {
        public static final EnergyConfig DEFAULT = new EnergyConfig(
            EnergyNodeConfig.DEFAULT,
            EnergyNodeConfig.DEFAULT,
            EnergyNodeConfig.DEFAULT,
            DefaultValues.DEFAULT_ENERGY_PRIORITY_ORDER
        );

        public static final Codec<EnergyConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EnergyNodeConfig.CODEC.optionalFieldOf("photosynthesis", EnergyNodeConfig.DEFAULT)
                    .forGetter(EnergyConfig::photosynthesis),
                EnergyNodeConfig.CODEC.optionalFieldOf("water_intake", EnergyNodeConfig.DEFAULT)
                    .forGetter(EnergyConfig::waterIntake),
                EnergyNodeConfig.CODEC.optionalFieldOf("geothermal", EnergyNodeConfig.DEFAULT)
                    .forGetter(EnergyConfig::geothermal),
                com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType.CODEC.listOf()
                    .optionalFieldOf("priority_order", DefaultValues.DEFAULT_ENERGY_PRIORITY_ORDER)
                    .forGetter(EnergyConfig::priorityOrder)
            ).apply(instance, EnergyConfig::new)
        );

        /**
         * 获取可用于运行时判定的规范化优先级顺序。
         * <p>
         * 允许配置中出现：
         * <ul>
         *     <li>重复项（会被去重）</li>
         *     <li>缺失项（会按默认顺序补齐）</li>
         * </ul>
         * 以保证“总能选出一个最终类型”，避免配置错误导致无法建造。
         * </p>
         */
        public List<com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType> normalizedPriorityOrder() {
            java.util.LinkedHashSet<com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType> ordered =
                new java.util.LinkedHashSet<>();
            if (priorityOrder != null) {
                ordered.addAll(priorityOrder);
            }
            ordered.addAll(DefaultValues.DEFAULT_ENERGY_PRIORITY_ORDER);
            return List.copyOf(ordered);
        }
    }

    /**
     * 单种能源节点配置。
     *
     * @param buildCost           建造成本（消耗资源池；单位：pool）
     * @param maxCount            上限数量（每个基地类型可配置不同上限）
     * @param poolGainMultiplier  资源池增长倍率增量（加法语义：0.25 表示额外 +25%）
     * @param poolGainFlat        资源池增长平坦增量（加法语义；单位：pool/刻间隔）
     * @param scanRadius          检测半径（方块；用于后续环境扫描）
     */
    public record EnergyNodeConfig(
            double buildCost,
            int maxCount,
            double poolGainMultiplier,
            double poolGainFlat,
            int scanRadius
    ) {
        public static final EnergyNodeConfig DEFAULT = new EnergyNodeConfig(
            DefaultValues.DEFAULT_ENERGY_BUILD_COST,
            DefaultValues.DEFAULT_ENERGY_MAX_COUNT,
            DefaultValues.DEFAULT_POOL_GAIN_MULTIPLIER,
            DefaultValues.DEFAULT_POOL_GAIN_FLAT,
            DefaultValues.DEFAULT_SCAN_RADIUS
        );

        public static final Codec<EnergyNodeConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("build_cost", DefaultValues.DEFAULT_ENERGY_BUILD_COST)
                    .forGetter(EnergyNodeConfig::buildCost),
                Codec.INT.optionalFieldOf("max_count", DefaultValues.DEFAULT_ENERGY_MAX_COUNT)
                    .forGetter(EnergyNodeConfig::maxCount),
                Codec.DOUBLE.optionalFieldOf("pool_gain_multiplier", DefaultValues.DEFAULT_POOL_GAIN_MULTIPLIER)
                    .forGetter(EnergyNodeConfig::poolGainMultiplier),
                Codec.DOUBLE.optionalFieldOf("pool_gain_flat", DefaultValues.DEFAULT_POOL_GAIN_FLAT)
                    .forGetter(EnergyNodeConfig::poolGainFlat),
                Codec.INT.optionalFieldOf("scan_radius", DefaultValues.DEFAULT_SCAN_RADIUS)
                    .forGetter(EnergyNodeConfig::scanRadius)
            ).apply(instance, EnergyNodeConfig::new)
        );
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
     * 扩张配置（菌毯 + Anchor）。
     * <p>
     * 原结构为单一 ExpansionConfig，本版本拆分为：
     * <ul>
     *   <li>菌毯扩张（mycelium）：低成本、高频率、贴地蔓延</li>
     *   <li>Anchor 自动生成（anchor）：高成本、低频率、作为支点</li>
     * </ul>
     * </p>
     */
    public record ExpansionConfig(MyceliumConfig mycelium, AnchorConfig anchor) {
        public static final ExpansionConfig DEFAULT = new ExpansionConfig(MyceliumConfig.DEFAULT, AnchorConfig.DEFAULT);

        public static final Codec<ExpansionConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                MyceliumConfig.CODEC.optionalFieldOf("mycelium", MyceliumConfig.DEFAULT)
                    .forGetter(ExpansionConfig::mycelium),
                AnchorConfig.CODEC.optionalFieldOf("anchor", AnchorConfig.DEFAULT)
                    .forGetter(ExpansionConfig::anchor)
            ).apply(instance, ExpansionConfig::new)
        );
    }

    /** 菌毯扩张配置。 */
    public record MyceliumConfig(
            double baseCost,
            double tierMultiplier,
            int maxRadius,
            int maxPerTick,
            int spacing
    ) {
        public static final MyceliumConfig DEFAULT = new MyceliumConfig(
            DefaultValues.DEFAULT_EXPANSION_COST,
            DefaultValues.DEFAULT_COST_MULTIPLIER,
            DefaultValues.DEFAULT_MAX_RADIUS,
            DefaultValues.DEFAULT_MAX_PER_TICK,
            DefaultValues.DEFAULT_NODE_SPACING
        );

        public static final Codec<MyceliumConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("base_cost", DefaultValues.DEFAULT_EXPANSION_COST)
                    .forGetter(MyceliumConfig::baseCost),
                Codec.DOUBLE.optionalFieldOf("tier_multiplier", DefaultValues.DEFAULT_COST_MULTIPLIER)
                    .forGetter(MyceliumConfig::tierMultiplier),
                Codec.INT.optionalFieldOf("max_radius", DefaultValues.DEFAULT_MAX_RADIUS)
                    .forGetter(MyceliumConfig::maxRadius),
                Codec.INT.optionalFieldOf("max_per_tick", DefaultValues.DEFAULT_MAX_PER_TICK)
                    .forGetter(MyceliumConfig::maxPerTick),
                Codec.INT.optionalFieldOf("spacing", DefaultValues.DEFAULT_NODE_SPACING)
                    .forGetter(MyceliumConfig::spacing)
            ).apply(instance, MyceliumConfig::new)
        );
    }

    /** Anchor 自动生成配置。 */
    public record AnchorConfig(
            double buildCost,
            int spacing,
            int maxCount,
            int triggerDistance,
            long cooldownTicks
    ) {
        private static final double DEFAULT_BUILD_COST = 50.0;
        private static final int DEFAULT_SPACING = 8;
        private static final int DEFAULT_MAX_COUNT = 16;
        private static final int DEFAULT_TRIGGER_DISTANCE = 10;
        private static final long DEFAULT_COOLDOWN_TICKS = 100L;

        public static final AnchorConfig DEFAULT = new AnchorConfig(
            DEFAULT_BUILD_COST,
            DEFAULT_SPACING,
            DEFAULT_MAX_COUNT,
            DEFAULT_TRIGGER_DISTANCE,
            DEFAULT_COOLDOWN_TICKS
        );

        public static final Codec<AnchorConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("build_cost", DEFAULT_BUILD_COST)
                    .forGetter(AnchorConfig::buildCost),
                Codec.INT.optionalFieldOf("spacing", DEFAULT_SPACING)
                    .forGetter(AnchorConfig::spacing),
                Codec.INT.optionalFieldOf("max_count", DEFAULT_MAX_COUNT)
                    .forGetter(AnchorConfig::maxCount),
                Codec.INT.optionalFieldOf("trigger_distance", DEFAULT_TRIGGER_DISTANCE)
                    .forGetter(AnchorConfig::triggerDistance),
                Codec.LONG.optionalFieldOf("cooldown_ticks", DEFAULT_COOLDOWN_TICKS)
                    .forGetter(AnchorConfig::cooldownTicks)
            ).apply(instance, AnchorConfig::new)
        );
    }

    /**
     * 光环极性。
     * <p>
     * 用于区分光环的“受益者/受害者”方向：
     * <ul>
     *   <li>{@link #POSITIVE}：正极，倾向于<strong>增幅守卫</strong>（例如守卫获得加成）。</li>
     *   <li>{@link #NEGATIVE}：负极，倾向于<strong>压制玩家</strong>（例如玩家在光环内受抑制）。</li>
     * </ul>
     * </p>
     * <p>
     * 默认值必须为 {@link #NEGATIVE}：因为旧版本配置/旧存档没有 polarity 字段时，
     * 历史行为就是“压制玩家”；缺省为 NEGATIVE 可确保完全向后兼容。
     * </p>
     */
    public enum AuraPolarity {
        /** 正极：增幅守卫。 */
        POSITIVE,
        /** 负极：压制玩家（兼容旧配置的默认行为）。 */
        NEGATIVE;

        /**
         * JSON 编解码器。
         * <p>
         * 采用字符串表示（"positive" / "negative"），并允许大小写差异。
         * </p>
         */
        public static final Codec<AuraPolarity> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                String normalized = value.trim().toLowerCase(Locale.ROOT);
                return switch (normalized) {
                    case "positive" -> DataResult.success(POSITIVE);
                    case "negative" -> DataResult.success(NEGATIVE);
                    default -> DataResult.error(() -> "未知的 polarity: " + value
                        + "（期望为 positive/negative）");
                };
            },
            polarity -> switch (polarity) {
                case POSITIVE -> "positive";
                case NEGATIVE -> "negative";
            }
        );
    }

    /**
     * 光环叠加模式。
     * <p>
     * 控制同类光环的叠加方式：取最大值、叠加相加或取平均。
     * </p>
     */
    public enum AuraStackingMode {
        /** 同类取最大值。 */
        MAX,
        /** 同类叠加（相加）。 */
        ADDITIVE,
        /** 同类取平均。 */
        AVERAGE;

        /** JSON 编解码器（字符串表示，大小写不敏感）。 */
        public static final Codec<AuraStackingMode> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                String normalized = value.trim().toLowerCase(Locale.ROOT);
                return switch (normalized) {
                    case "max" -> DataResult.success(MAX);
                    case "additive" -> DataResult.success(ADDITIVE);
                    case "average" -> DataResult.success(AVERAGE);
                    default -> DataResult.error(() -> "未知的 stacking 模式: " + value
                        + "（期望为 max/additive/average）");
                };
            },
            mode -> switch (mode) {
                case MAX -> "max";
                case ADDITIVE -> "additive";
                case AVERAGE -> "average";
            }
        );
    }

    /**
     * 光环配置（影响半径与衰减）。
     * <p>
     * auraRadius 用于玩家资源消耗判定和边界渲染，与节点扩张半径解耦。
     * 衰减公式：factor = max(minFalloff, (1 - distance/auraRadius)^falloffPower)
     * </p>
     * <p>
     * 缩圈机制：effectiveRadius = baseRadius * scale，
     * 其中 scale = clamp(minScale, totalNodes/refNodes, 1.0)
     * </p>
     *
     * @param polarity        光环极性：正极增幅守卫 / 负极压制玩家。
     *                        <p>
     *                        注意：当旧 JSON/旧存档缺少 polarity 字段时，必须默认为 NEGATIVE，
     *                        以保持当前默认“压制玩家”的历史行为。
     *                        </p>
     * @param baseRadius      1 转基础半径
     * @param tierExponent    转数指数（auraRadius = baseRadius * tierExponent^(tier-1)）
     * @param maxRadius       最大半径上限
     * @param falloffPower    衰减指数（越大中心越强、边缘越弱）
     * @param minFalloff      最小衰减因子（边缘仍有微弱效果）
     * @param refNodesByTier  各转的满状态参考节点数（用于缩圈计算）
     * @param minScale        最小缩圈比例（节点全拆时仍保留的光环比例）
     */
     public record AuraConfig(
             AuraPolarity polarity,
             int baseRadius,
             double tierExponent,
             int maxRadius,
             double falloffPower,
             double minFalloff,
             List<Integer> refNodesByTier,
             double minScale,
             AuraStackingMode sameTypeStacking,
             boolean differentTypesCoexist,
             /** 光环节点建造成本（扣资源池）。 */
             double buildCost,
             /** 光环节点数量上限（0 表示不启用上限）。 */
             int maxCount
     ) {
         public static final AuraConfig DEFAULT = new AuraConfig(
             AuraPolarity.NEGATIVE,
             DefaultValues.DEFAULT_AURA_BASE_RADIUS,
             DefaultValues.DEFAULT_AURA_TIER_EXPONENT,
             DefaultValues.DEFAULT_AURA_MAX_RADIUS,
             DefaultValues.DEFAULT_FALLOFF_POWER,
             DefaultValues.DEFAULT_MIN_FALLOFF,
             DefaultValues.DEFAULT_REF_NODES_BY_TIER,
             DefaultValues.DEFAULT_MIN_SCALE,
             DefaultValues.DEFAULT_SAME_TYPE_STACKING,
             DefaultValues.DEFAULT_DIFFERENT_TYPES_COEXIST,
             DefaultValues.DEFAULT_AURA_BUILD_COST,
             DefaultValues.DEFAULT_AURA_MAX_COUNT
         );

        /**
         * 兼容旧代码/旧配置的构造方法：缺省 polarity 为 NEGATIVE。
         * <p>
         * 之所以固定为 NEGATIVE，是因为旧版本没有 polarity 字段时默认行为就是“压制玩家”。
         * </p>
         */
         public AuraConfig(
                 int baseRadius,
                 double tierExponent,
                 int maxRadius,
                 double falloffPower,
                 double minFalloff,
                 List<Integer> refNodesByTier,
                 double minScale
         ) {
             this(AuraPolarity.NEGATIVE, baseRadius, tierExponent, maxRadius, falloffPower, minFalloff,
                refNodesByTier, minScale, DefaultValues.DEFAULT_SAME_TYPE_STACKING,
                DefaultValues.DEFAULT_DIFFERENT_TYPES_COEXIST, DefaultValues.DEFAULT_AURA_BUILD_COST,
                DefaultValues.DEFAULT_AURA_MAX_COUNT);
         }

         public static final Codec<AuraConfig> CODEC = RecordCodecBuilder.create(instance ->
             instance.group(
                 AuraPolarity.CODEC.optionalFieldOf("polarity", AuraPolarity.NEGATIVE)
                    .forGetter(AuraConfig::polarity),
                Codec.INT.optionalFieldOf("base_radius", DefaultValues.DEFAULT_AURA_BASE_RADIUS)
                    .forGetter(AuraConfig::baseRadius),
                Codec.DOUBLE.optionalFieldOf("tier_exponent", DefaultValues.DEFAULT_AURA_TIER_EXPONENT)
                    .forGetter(AuraConfig::tierExponent),
                Codec.INT.optionalFieldOf("max_radius", DefaultValues.DEFAULT_AURA_MAX_RADIUS)
                    .forGetter(AuraConfig::maxRadius),
                Codec.DOUBLE.optionalFieldOf("falloff_power", DefaultValues.DEFAULT_FALLOFF_POWER)
                    .forGetter(AuraConfig::falloffPower),
                Codec.DOUBLE.optionalFieldOf("min_falloff", DefaultValues.DEFAULT_MIN_FALLOFF)
                    .forGetter(AuraConfig::minFalloff),
                Codec.INT.listOf().optionalFieldOf("ref_nodes_by_tier",
                        DefaultValues.DEFAULT_REF_NODES_BY_TIER)
                    .forGetter(AuraConfig::refNodesByTier),
                 Codec.DOUBLE.optionalFieldOf("min_scale", DefaultValues.DEFAULT_MIN_SCALE)
                     .forGetter(AuraConfig::minScale),
                 AuraStackingMode.CODEC.optionalFieldOf("same_type_stacking",
                         DefaultValues.DEFAULT_SAME_TYPE_STACKING)
                     .forGetter(AuraConfig::sameTypeStacking),
                 Codec.BOOL.optionalFieldOf("different_types_coexist",
                         DefaultValues.DEFAULT_DIFFERENT_TYPES_COEXIST)
                    .forGetter(AuraConfig::differentTypesCoexist),
                 Codec.DOUBLE.optionalFieldOf("aura_build_cost", DefaultValues.DEFAULT_AURA_BUILD_COST)
                     .forGetter(AuraConfig::buildCost),
                 Codec.INT.optionalFieldOf("aura_max_count", DefaultValues.DEFAULT_AURA_MAX_COUNT)
                     .forGetter(AuraConfig::maxCount)
             ).apply(instance, AuraConfig::new)
         );

        /**
         * 计算指定转数的光环半径。
         *
         * @param tier 转数
         * @return 光环半径
         */
        public int calculateRadius(int tier) {
            double radius = baseRadius * Math.pow(tierExponent, tier - 1);
            return Math.min(maxRadius, (int) Math.round(radius));
        }

        /**
         * 计算距离衰减因子。
         *
         * @param distance   玩家到核心的距离
         * @param auraRadius 当前光环半径
         * @return 衰减因子（0.0 ~ 1.0）
         */
        public double calculateFalloff(double distance, int auraRadius) {
            if (auraRadius <= 0 || distance >= auraRadius) {
                return 0.0;
            }
            double t = distance / auraRadius;
            return Math.max(minFalloff, Math.pow(1.0 - t, falloffPower));
        }

        /**
         * 获取指定转数的满状态参考节点数。
         * <p>
         * 用于缩圈计算：scale = clamp(minScale, totalNodes/refNodes, 1.0)。
         * 如果 refNodesByTier 列表长度不足，则使用最后一个值；如果列表为空则返回 1。
         * </p>
         *
         * @param tier 转数（1-based）
         * @return 该转数的参考节点数
         */
        public int getRefNodesForTier(int tier) {
            if (refNodesByTier.isEmpty()) {
                return 1;  // 避免除零
            }
            int index = Math.max(0, Math.min(tier - 1, refNodesByTier.size() - 1));
            return Math.max(1, refNodesByTier.get(index));
        }

        /**
         * 计算基于节点数量的有效光环半径。
         * <p>
         * effectiveRadius = baseRadius * tierExponent^(tier-1) * scale，
         * 其中 scale = clamp(minScale, totalNodes/refNodes, 1.0)。
         * </p>
         *
         * @param tier       当前转数
         * @param totalNodes 当前节点数量
         * @return 有效光环半径
         */
        public int calculateEffectiveRadius(int tier, int totalNodes) {
            int baseAuraRadius = calculateRadius(tier);
            int refNodes = getRefNodesForTier(tier);
            double scale = Math.max(minScale, Math.min(1.0, (double) totalNodes / refNodes));
            return Math.max(1, (int) Math.round(baseAuraRadius * scale));
        }
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
