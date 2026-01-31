package com.Kizunad.guzhenrenext.bastion;

import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import com.Kizunad.guzhenrenext.bastion.service.BastionCleanupService;
import com.Kizunad.guzhenrenext.bastion.service.BastionConnectivityService;
import com.Kizunad.guzhenrenext.bastion.service.BastionEnergyService;
import com.Kizunad.guzhenrenext.bastion.service.BastionExpansionService;
import com.Kizunad.guzhenrenext.bastion.service.BastionSpawnService;
import com.Kizunad.guzhenrenext.bastion.skill.BastionHighTierSkillService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地刻调度器 - 处理维度内所有基地的周期性更新。
 * <p>
 * 实现预算驱动的刻分配，基于玩家距离优先级排序。
 * 遵循设计文档的"固定预算 + 距离优先刻"方案。
 * </p>
 *
 * <h2>刻类别</h2>
 * <ul>
 *   <li><b>完整刻：</b>玩家在 128 格内 - 扩张 + 刷怪 + 进化</li>
 *   <li><b>轻量刻：</b>无附近玩家但 chunk 已加载 - 进化（减速）+ 资源池</li>
 *   <li><b>未加载：</b>chunk 未加载 - 仅累积 offlineAccumTicks</li>
 * </ul>
 */
public final class BastionTicker {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionTicker.class);

    private BastionTicker() {
        // 私有构造函数，防止实例化
    }

    // ===== 配置常量（后续可外置到配置文件） =====

    /**
     * 时间与调度相关常量。
     */
    private static final class TickConfig {
        /** 基地处理的刻间隔（每 N 游戏刻处理一次）。 */
        static final int TICK_INTERVAL = 20;
        /** 每刻最大处理基地数（预算）。 */
        static final int BUDGET_BASTIONS_PER_TICK = 8;
        /** 完整刻的距离阈值（方块数）。 */
        static final int FULL_TICK_DISTANCE = 128;

        private TickConfig() {
        }
    }

    /**
     * 进化与资源池乘数常量。
     */
    private static final class MultiplierConfig {
        /** 无玩家时的进化速度乘数。 */
        static final double NO_PLAYER_EVOLUTION_MULTIPLIER = 0.1;
        /** 无玩家时的资源池获取乘数。 */
        static final double NO_PLAYER_POOL_MULTIPLIER = 0.5;
        /** 资源池每节点容量系数。 */
        static final double POOL_CAP_PER_NODE = 10.0;
        /** 资源池基础获取系数。 */
        static final double POOL_BASE_GAIN_FACTOR = 0.01;
        /** 转数幂基数（用于计算转数加成）。 */
        static final double TIER_POWER_BASE = 1.5;

        /**
         * 有效节点数权重：Anchor 的权重（子核心/支撑节点）。
         * <p>
         * 设计：Anchor 更稀有且更关键，因此权重大于菌毯。
         * </p>
         */
        static final int EFFECTIVE_NODE_ANCHOR_WEIGHT = 10;

        /**
         * 有效节点数权重：菌毯的权重（贴地蔓延主网）。
         */
        static final int EFFECTIVE_NODE_MYCELIUM_WEIGHT = 1;

        private MultiplierConfig() {
        }
    }

    /**
     * 销毁后衰减相关常量。
     */
    private static final class DecayConfig {
        /** 销毁后开始清理前的缓冲刻数（30 秒）。 */
        static final long DESTRUCTION_BUFFER_TICKS = 600L;
        /** 销毁后节点衰减窗口刻数（60 秒，即 buffer 后 30 秒开始衰减，再 30 秒完成）。 */
        static final long DESTRUCTION_DECAY_WINDOW_TICKS = 1200L;
        /** 销毁后移除基地记录的刻数（90 秒）。 */
        static final long DESTRUCTION_REMOVAL_TICKS = 1800L;

        private DecayConfig() {
        }
    }

    // ===== 注册 =====

    /**
     * 向 NeoForge 事件总线注册刻处理器。
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(TickHandler.class);
        LOGGER.info("BastionTicker 已注册");
    }

    /**
     * 事件处理内部类。
     */
    private static final class TickHandler {
        private TickHandler() {
        }

        @SubscribeEvent
        public static void onLevelTickPost(LevelTickEvent.Post event) {
            if (!(event.getLevel() instanceof ServerLevel level)) {
                return;
            }

            long gameTime = level.getGameTime();

            // 仅在每 TICK_INTERVAL 刻处理一次
            if (gameTime % TickConfig.TICK_INTERVAL != 0) {
                return;
            }

            processBastions(level, gameTime);
        }
    }

    // ===== 主处理逻辑 =====

    /**
     * 在预算约束下处理世界中的所有基地。
     *
     * @param level    服务端世界
     * @param gameTime 当前游戏时间
     */
    private static void processBastions(ServerLevel level, long gameTime) {
        BastionSavedData savedData = BastionSavedData.get(level);
        List<BastionData> bastions = new ArrayList<>(savedData.getAllBastions());

        if (bastions.isEmpty()) {
            return;
        }

        // 按优先级排序（已销毁优先清理，然后按玩家距离）
        List<ServerPlayer> players = level.players();
        bastions.sort(createPriorityComparator(players));

        // 按预算处理
        int processed = 0;
        for (BastionData bastion : bastions) {
            if (processed >= TickConfig.BUDGET_BASTIONS_PER_TICK) {
                break;
            }

            TickCategory category = determineTickCategory(level, bastion, players);
            tickBastion(level, savedData, bastion, gameTime, category);
            processed++;
        }
    }

    /**
     * 创建基地处理优先级比较器。
     * 优先级：DESTROYED > 离玩家最近 > 其他
     */
    private static Comparator<BastionData> createPriorityComparator(List<ServerPlayer> players) {
        return (a, b) -> {
            // DESTROYED 基地优先（用于清理）
            if (a.state() == BastionState.DESTROYED && b.state() != BastionState.DESTROYED) {
                return -1;
            }
            if (b.state() == BastionState.DESTROYED && a.state() != BastionState.DESTROYED) {
                return 1;
            }

            // 然后按与最近玩家的距离排序
            double distA = getMinPlayerDistance(a.corePos(), players);
            double distB = getMinPlayerDistance(b.corePos(), players);
            return Double.compare(distA, distB);
        };
    }

    /**
     * 确定基地的刻类别。
     */
    private static TickCategory determineTickCategory(
            ServerLevel level, BastionData bastion, List<ServerPlayer> players) {
        BlockPos corePos = bastion.corePos();

        // 检查 chunk 是否已加载
        ChunkPos chunkPos = new ChunkPos(corePos);
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return TickCategory.UNLOADED;
        }

        // 检查玩家距离
        double minDistance = getMinPlayerDistance(corePos, players);
        if (minDistance <= TickConfig.FULL_TICK_DISTANCE) {
            return TickCategory.FULL;
        }

        return TickCategory.LIGHT;
    }

    /**
     * 获取某位置到任意玩家的最小距离。
     */
    private static double getMinPlayerDistance(BlockPos pos, List<ServerPlayer> players) {
        if (players.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double minDistSq = Double.MAX_VALUE;
        for (ServerPlayer player : players) {
            double distSq = player.blockPosition().distSqr(pos);
            if (distSq < minDistSq) {
                minDistSq = distSq;
            }
        }
        return Math.sqrt(minDistSq);
    }

    // ===== 基地刻逻辑 =====

    /**
     * 根据状态和刻类别处理单个基地。
     */
    private static void tickBastion(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime,
            TickCategory category) {
        BastionState effectiveState = bastion.getEffectiveState(gameTime);

        switch (effectiveState) {
            case DESTROYED -> tickDestroyedBastion(level, savedData, bastion, gameTime);
            case SEALED -> tickSealedBastion(savedData, bastion, gameTime, category);
            case ACTIVE -> tickActiveBastion(level, savedData, bastion, gameTime, category);
        }
    }

    /**
     * 处理 DESTROYED 状态的基地 - 处理衰减和最终移除。
     */
    private static void tickDestroyedBastion(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime) {
        long elapsed = gameTime - bastion.destroyedAtGameTime();

        if (elapsed < DecayConfig.DESTRUCTION_BUFFER_TICKS) {
            // 缓冲期 - 不做处理
            return;
        }

        if (elapsed < DecayConfig.DESTRUCTION_DECAY_WINDOW_TICKS) {
            // 衰减窗口 - 调用清理服务处理节点衰减
            BastionCleanupService.processCleanup(level, savedData, bastion, gameTime);

            // 回合2：连通性/衰败 v1 也在 DESTROYED 阶段继续推进（作为额外安全网）。
            // 设计：即使旧清理服务只处理 Anchor，这里也会让断连菌毯按倒计时消失。
            BastionConnectivityService.tick(level, savedData, bastion, gameTime);

            BastionData updated = bastion.withLastGameTime(gameTime);
            savedData.updateBastion(updated);
            return;
        }

        if (elapsed < DecayConfig.DESTRUCTION_REMOVAL_TICKS) {
            // 衰减后期 - 继续清理残余节点
            BastionCleanupService.processCleanup(level, savedData, bastion, gameTime);

            // 回合2：继续推进连通性/衰败。
            BastionConnectivityService.tick(level, savedData, bastion, gameTime);

            BastionData updated = bastion.withLastGameTime(gameTime);
            savedData.updateBastion(updated);
            return;
        }

        // 衰减窗口后 - 强制清理所有节点并移除基地记录
        BastionCleanupService.forceCleanupAll(level, savedData, bastion);

        // 调度孤儿节点检查作为安全网（处理遗漏的节点）
        BastionCleanupService.scheduleOrphanChecks(
            level, bastion.corePos(), bastion.growthRadius());

        savedData.removeBastion(bastion.id());

        // 清理高转技能运行时状态
        BastionHighTierSkillService.onBastionRemoved(bastion.id());

        // 清理同步缓存
        BastionNetworkHandler.clearSyncCache(bastion.id());

        LOGGER.info("基地 {} 在销毁超时后被移除", bastion.id());

        // 通知客户端移除缓存
        BastionNetworkHandler.notifyRemoveToNearbyPlayers(
            level, bastion.id(),
            bastion.corePos().getX(), bastion.corePos().getZ(),
            bastion.getAuraRadius());
    }

    /**
     * 处理 SEALED 状态的基地 - 最小化处理。
     */
    private static void tickSealedBastion(
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime,
            TickCategory category) {
        if (category == TickCategory.UNLOADED) {
            // 累积离线刻
            long tickDelta = gameTime - bastion.lastGameTime();
            BastionData updated = bastion
                .withOfflineAccumTicks(bastion.offlineAccumTicks() + tickDelta)
                .withLastGameTime(gameTime);
            savedData.updateBastion(updated);
            return;
        }

        // 封印基地：无扩张，无刷怪
        // 可选应用降低的进化速度（config.sealEvolutionMultiplier）
        // MVP 阶段仅更新 lastGameTime
        BastionData updated = bastion.withLastGameTime(gameTime);
        savedData.updateBastion(updated);
    }

    /**
     * 处理 ACTIVE 状态的基地 - 基于类别的完整处理。
     */
    private static void tickActiveBastion(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            long gameTime,
            TickCategory category) {
        if (category == TickCategory.UNLOADED) {
            // 累积离线刻
            long tickDelta = gameTime - bastion.lastGameTime();
            BastionData updated = bastion
                .withOfflineAccumTicks(bastion.offlineAccumTicks() + tickDelta)
                .withLastGameTime(gameTime);
            savedData.updateBastion(updated);
            return;
        }

        // 回合2：连通性 v1（非实时 BFS）+ 衰败 v1（可观察）
        // 设计：该逻辑应在 chunk 已加载时推进；由 determineTickCategory 保证 UNLOADED 已提前返回。
        // 预算由 BastionConnectivityService 自身控制，避免在 BastionTicker 再做二次预算叠加。
        BastionConnectivityService.tick(level, savedData, bastion, gameTime);

        // 根据类别计算乘数
        double evolutionMultiplier = category == TickCategory.FULL
            ? 1.0 : MultiplierConfig.NO_PLAYER_EVOLUTION_MULTIPLIER;
        double poolMultiplier = category == TickCategory.FULL
            ? 1.0 : MultiplierConfig.NO_PLAYER_POOL_MULTIPLIER;

        // 更新资源池（按“有效节点数”计算：Anchor 权重更高，菌毯权重更低）
        int effectiveNodes = calculateEffectiveNodes(bastion);

        // 回合3：能源挂载 v1 需要预算化扫描以更新“Anchor -> 能源类型”运行时缓存。
        // 说明：该缓存不持久化，且扫描成本较高，因此必须由 BastionEnergyService 进行预算化驱动。
        BastionEnergyService.tick(level, savedData, bastion, gameTime);

        // 从缓存统计各能源类型数量，并按配置 maxCount 截断。
        // 解释：maxCount 是“上限贡献数”，即使缓存里有更多同类能源挂载，也只取前 maxCount 个计入加成，
        // 以避免通过堆量 Anchor 获得线性无限收益。
        BastionTypeConfig typeConfigForEnergy = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.EnergyConfig energyConfig = typeConfigForEnergy.energy();
        Map<BlockPos, BastionEnergyType> energyMap = savedData.getOrCreateAnchorEnergyMap(bastion.id());

        int photoMax = Math.max(0, energyConfig.photosynthesis().maxCount());
        int waterMax = Math.max(0, energyConfig.waterIntake().maxCount());
        int geoMax = Math.max(0, energyConfig.geothermal().maxCount());
        int photoCount = 0;
        int waterCount = 0;
        int geoCount = 0;
        for (BastionEnergyType type : energyMap.values()) {
            if (type == null) {
                continue;
            }
            if (type == BastionEnergyType.PHOTOSYNTHESIS) {
                if (photoCount < photoMax) {
                    photoCount++;
                }
            } else if (type == BastionEnergyType.WATER_INTAKE) {
                if (waterCount < waterMax) {
                    waterCount++;
                }
            } else if (type == BastionEnergyType.GEOTHERMAL) {
                if (geoCount < geoMax) {
                    geoCount++;
                }
            }
        }

        // 能源加成策略：倍率是“加法语义”（mAdd=0.25 表示额外 +25%），最终 multiplier=poolMultiplier*(1+mAdd)。
        // flat 为平坦增量，不参与倍率放大：
        // - 原因：flat 用于表达“环境提供的固定供给”，若再乘倍率会形成乘法叠加，导致收益随节点/转数爆炸。
        // - 设计：flat 仅与 TickInterval 线性累计，保持可控。
        double mAdd = photoCount * energyConfig.photosynthesis().poolGainMultiplier()
            + waterCount * energyConfig.waterIntake().poolGainMultiplier()
            + geoCount * energyConfig.geothermal().poolGainMultiplier();
        double flatAdd = photoCount * energyConfig.photosynthesis().poolGainFlat()
            + waterCount * energyConfig.waterIntake().poolGainFlat()
            + geoCount * energyConfig.geothermal().poolGainFlat();
        double multiplierFinal = poolMultiplier * (1.0 + Math.max(0.0, mAdd));

        double basePoolGain = calculatePoolGain(bastion, effectiveNodes, multiplierFinal);
        double poolGain = basePoolGain + flatAdd * TickConfig.TICK_INTERVAL;
        double poolCap = effectiveNodes * MultiplierConfig.POOL_CAP_PER_NODE;
        double newPool = Math.min(poolCap, bastion.resourcePool() + poolGain);

        // 更新进化进度
        double evolutionGain = calculateEvolutionGain(bastion, evolutionMultiplier);
        double newProgress = bastion.evolutionProgress() + evolutionGain;
        int newTier = bastion.tier();

        // 检查是否升阶（使用配置中的 maxTier）
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        int maxTier = typeConfig.maxTier();
        if (newProgress >= 1.0 && bastion.tier() < maxTier) {
            int targetTier = bastion.tier() + 1;

            // 检查高转阈值要求（7转+）
            if (canEvolveToTier(bastion, typeConfig, targetTier, newPool)) {
                newTier = targetTier;
                newProgress = 0.0;
                LOGGER.info("基地 {} 进化到 {} 转", bastion.id(), newTier);

                // 播放升级音效和粒子
                BastionSoundPlayer.playEvolve(level, bastion.corePos());
                BastionParticles.spawnEvolveParticles(level, bastion.corePos(), bastion.primaryDao());
            } else {
                // 阈值未满足，进度停滞在 1.0（不再累积）
                newProgress = 1.0;
                LOGGER.debug("基地 {} 进化到 {} 转的阈值未满足，进度停滞",
                    bastion.id(), targetTier);
            }
        }

        // 构建更新后的基地
        BastionData updated = bastion
            .withResourcePool(newPool)
            .withEvolution(newProgress, newTier)
            .withLastGameTime(gameTime);

        savedData.updateBastion(updated);

        // 如果转数变化，同步到客户端并更新方块显示
        if (newTier != bastion.tier()) {
            BastionNetworkHandler.syncToNearbyPlayers(level, updated);
            com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock.updateDisplayedTier(
                level, bastion.corePos(), newTier);
        }

        // 仅在完整刻时执行扩张和刷怪
        if (category == TickCategory.FULL) {
            // 重新获取最新数据（savedData 已更新）
            BastionData freshData = savedData.getBastion(bastion.id());
            if (freshData != null) {
                // 扩张服务
                BastionExpansionService.tryExpand(level, savedData, freshData, gameTime);
                // 刷怪服务
                BastionSpawnService.trySpawn(level, savedData, freshData, gameTime);

                // 高转主动技能：仅在 FULL tick 驱动
                BastionHighTierSkillService.runActiveSkills(level, freshData, gameTime);
            }
        }
    }

    /**
     * 计算每刻间隔的资源池获取量。
     */
    private static int calculateEffectiveNodes(BastionData bastion) {
        // 核心/旧节点总数依然保留在 bastion.totalNodes，用于 aura 等旧机制。
        // 资源池使用单独的“有效节点数”，避免菌毯不计入 totalNodes 导致资源池过低。
        int anchors = Math.max(0, bastion.totalAnchors());
        int mycelium = Math.max(0, bastion.totalMycelium());
        return anchors * MultiplierConfig.EFFECTIVE_NODE_ANCHOR_WEIGHT
            + mycelium * MultiplierConfig.EFFECTIVE_NODE_MYCELIUM_WEIGHT;
    }

    private static double calculatePoolGain(BastionData bastion, int effectiveNodes, double multiplier) {
        double tierFactor = Math.pow(MultiplierConfig.TIER_POWER_BASE, bastion.tier() - 1);
        double baseGain = effectiveNodes * tierFactor * MultiplierConfig.POOL_BASE_GAIN_FACTOR;
        return baseGain * multiplier * TickConfig.TICK_INTERVAL;
    }

    /**
     * 计算每刻间隔的进化进度获取量。
     * <p>
     * 从 BastionTypeConfig 读取进化参数。
     * </p>
     */
    private static double calculateEvolutionGain(BastionData bastion, double multiplier) {
        // 从配置读取进化参数
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.EvolutionConfig evolutionConfig = typeConfig.evolution();

        // 当前转数的进化时间：baseTime * tierMultiplier^(tier-1)
        long evolutionTime = evolutionConfig.baseEvolutionTicks()
            * (long) Math.pow(evolutionConfig.tierMultiplier(), bastion.tier() - 1);
        double progressPerTick = 1.0 / evolutionTime;
        return progressPerTick * multiplier * TickConfig.TICK_INTERVAL;
    }

    /**
     * 获取转数转换所需的进化时间（刻）。
     * <p>
     * 从 BastionTypeConfig 读取进化参数。
     * </p>
     *
     * @param bastion 基地数据
     * @return 进化时间（刻）
     */
    private static long getEvolutionTimeForTier(BastionData bastion) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.EvolutionConfig evolutionConfig = typeConfig.evolution();
        return evolutionConfig.baseEvolutionTicks()
            * (long) Math.pow(evolutionConfig.tierMultiplier(), bastion.tier() - 1);
    }

    /**
     * 检查基地是否满足进化到目标转数的阈值要求。
     * <p>
     * 对于高转（7转+），检查配置中定义的 TierThreshold：
     * <ul>
     *   <li>requiredNodes：节点数量要求</li>
     *   <li>requiredEnergyPool：基地能量池要求</li>
     * </ul>
     * 对于低转（1-6转），默认返回 true（无额外阈值要求）。
     * </p>
     *
     * @param bastion    基地数据
     * @param typeConfig 基地类型配置
     * @param targetTier 目标转数
     * @param poolAmount 当前能量池数量
     * @return true 如果满足进化条件
     */
    private static boolean canEvolveToTier(
            BastionData bastion,
            BastionTypeConfig typeConfig,
            int targetTier,
            double poolAmount) {
        // 检查是否有高转配置
        if (typeConfig.highTier().isEmpty()) {
            return true;  // 无高转配置，默认允许
        }

        BastionTypeConfig.HighTierConfig highTier = typeConfig.highTier().get();
        BastionTypeConfig.TierThreshold threshold = highTier.getThresholdForTier(targetTier);

        if (threshold == null) {
            return true;  // 该转数无阈值定义，默认允许
        }

        // 检查节点数量
        if (bastion.totalNodes() < threshold.requiredNodes()) {
            LOGGER.trace("基地 {} 节点不足：需要 {}，当前 {}",
                bastion.id(), threshold.requiredNodes(), bastion.totalNodes());
            return false;
        }

        // 检查能量池数量
        if (poolAmount < threshold.requiredEnergyPool()) {
            LOGGER.trace("基地 {} 能量池不足：需要 {}，当前 {}",
                bastion.id(), threshold.requiredEnergyPool(), poolAmount);
            return false;
        }

        return true;
    }

    // ===== 刻类别枚举 =====

    /**
     * 基于玩家距离和 chunk 加载状态的刻处理类别。
     */
    private enum TickCategory {
        /**
         * 玩家在 128 格内 - 完整处理。
         */
        FULL,

        /**
         * chunk 已加载但无附近玩家 - 降低处理。
         */
        LIGHT,

        /**
         * chunk 未加载 - 最小处理（离线累积）。
         */
        UNLOADED
    }
}
