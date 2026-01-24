package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.block.BastionNodeBlock;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地节点掉落服务 - 处理节点被破坏时的资源奖励。
 * <p>
 * 当玩家破坏由扩张服务生成的节点（GENERATED=true）时，
 * 根据节点道途类型和转数给予相应的资源奖励。
 * </p>
 *
 * <h2>资源分配规则</h2>
 * <ul>
 *   <li>智道（ZHI_DAO）：念头</li>
 *   <li>魂道（HUN_DAO）：魂魄</li>
 *   <li>木道（MU_DAO）：真元</li>
 *   <li>力道（LI_DAO）：精力</li>
 * </ul>
 *
 * <h2>奖励量级设计（收敛后）</h2>
 * <ul>
 *   <li>1转：1 资源（入门级）</li>
 *   <li>2转：3 资源</li>
 *   <li>3转：10 资源</li>
 *   <li>4转：30 资源</li>
 *   <li>5转：100 资源</li>
 *   <li>6转+：主资源 + 道痕奖励</li>
 * </ul>
 */
@EventBusSubscriber(modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID)
public final class BastionNodeDropService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionNodeDropService.class);

    private BastionNodeDropService() {
        // 工具类
    }

    // ===== 奖励配置常量 =====

    /**
     * 节点破坏奖励相关常量。
     */
    private static final class RewardConfig {
        /** 1转节点的基础资源奖励（收敛后）。 */
        static final double TIER_1_BASE_REWARD = 1.0;
        /** 每转奖励倍率（约 3 倍增长，更平缓）。 */
        static final double TIER_MULTIPLIER = 3.0;
        /** 奖励随机浮动范围（±20%）。 */
        static final double VARIANCE_RATIO = 0.2;
        /** 六转以上开始给道痕的阈值。 */
        static final int DAO_HEN_TIER_THRESHOLD = 6;
        /** 六转道痕基础奖励。 */
        static final double DAO_HEN_BASE_REWARD = 1.0;
        /** 道痕每转增长倍率。 */
        static final double DAO_HEN_TIER_MULTIPLIER = 3.0;
        /** 道痕最小奖励（防止负值）。 */
        static final double DAO_HEN_MIN_REWARD = 0.1;

        private RewardConfig() {
        }
    }

    /**
     * 消息冷却配置。
     */
    private static final class MessageConfig {
        /** 消息汇总间隔（毫秒）- 1秒内的奖励汇总显示。 */
        static final long AGGREGATION_INTERVAL_MS = 1000L;

        private MessageConfig() {
        }
    }

    /** 玩家奖励汇总记录。 */
    private static final ConcurrentHashMap<UUID, RewardAggregator> REWARD_AGGREGATORS =
        new ConcurrentHashMap<>();

    // ===== 事件处理 =====

    /**
     * 处理方块破坏事件。
     * <p>
     * 仅处理由扩张服务生成的节点方块（GENERATED=true）。
     * 按道途分配不同类型的资源。
     * </p>
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // 检查方块是否为节点
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof BastionNodeBlock)) {
            return;
        }

        // 检查节点是否由扩张服务生成
        if (!state.getValue(BastionNodeBlock.GENERATED)) {
            return;
        }

        // 仅对服务端玩家处理
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        // 获取节点属性
        int tier = state.getValue(BastionNodeBlock.TIER);
        BastionDao dao = state.getValue(BastionNodeBlock.DAO);

        // 计算并给予奖励（使用玩家随机源）
        RandomSource random = player.getRandom();
        applyReward(player, dao, tier, random, event.getPos());
    }

    /**
     * 根据道途和转数给予资源奖励。
     */
    private static void applyReward(
            ServerPlayer player,
            BastionDao dao,
            int tier,
            RandomSource random,
            BlockPos pos) {

        double baseReward = calculateBaseReward(tier, random);
        String resourceName;
        double actualReward;

        // 按道途分配资源
        switch (dao) {
            case ZHI_DAO -> {
                // NianTouHelper.modify 返回 void，直接调用
                NianTouHelper.modify(player, baseReward);
                actualReward = baseReward;
                resourceName = "念头";
            }
            case HUN_DAO -> {
                actualReward = HunPoHelper.modify(player, baseReward);
                resourceName = "魂魄";
            }
            case MU_DAO -> {
                actualReward = ZhenYuanHelper.modify(player, baseReward);
                resourceName = "真元";
            }
            case LI_DAO -> {
                actualReward = JingLiHelper.modify(player, baseReward);
                resourceName = "精力";
            }
            default -> {
                // 回退到真元
                actualReward = ZhenYuanHelper.modify(player, baseReward);
                resourceName = "真元";
            }
        }

        // 六转以上额外给道痕
        double daoHenReward = 0.0;
        if (tier >= RewardConfig.DAO_HEN_TIER_THRESHOLD) {
            daoHenReward = calculateDaoHenReward(tier, random);
            DaoHenHelper.DaoType daoType = mapBastionDaoToDaoHenType(dao);
            if (daoType != null) {
                DaoHenHelper.addDaoHen(player, daoType, daoHenReward);
            }
        }

        // 汇总并发送消息
        aggregateAndNotify(player, dao, resourceName, baseReward, daoHenReward, tier);

        LOGGER.debug("玩家 {} 在 {} 破坏了 {}转 {} 节点，获得 {} {}{}",
            player.getName().getString(),
            pos.toShortString(),
            tier,
            dao.getSerializedName(),
            baseReward,
            resourceName,
            daoHenReward > 0 ? String.format(" + %.1f 道痕", daoHenReward) : "");
    }

    /**
     * 计算基础资源奖励。
     * <p>
     * 使用 3 倍指数增长（收敛后），带 ±20% 随机浮动。
     * </p>
     */
    private static double calculateBaseReward(int tier, RandomSource random) {
        double baseReward = RewardConfig.TIER_1_BASE_REWARD
            * Math.pow(RewardConfig.TIER_MULTIPLIER, tier - 1);

        // 使用传入的随机源
        double variance = (random.nextDouble() * 2 - 1) * RewardConfig.VARIANCE_RATIO;
        double finalReward = baseReward * (1 + variance);

        return Math.max(1.0, finalReward);
    }

    /**
     * 计算道痕奖励（六转以上）。
     */
    private static double calculateDaoHenReward(int tier, RandomSource random) {
        int tierAboveThreshold = tier - RewardConfig.DAO_HEN_TIER_THRESHOLD + 1;
        double baseReward = RewardConfig.DAO_HEN_BASE_REWARD
            * Math.pow(RewardConfig.DAO_HEN_TIER_MULTIPLIER, tierAboveThreshold - 1);

        double variance = (random.nextDouble() * 2 - 1) * RewardConfig.VARIANCE_RATIO;
        return Math.max(RewardConfig.DAO_HEN_MIN_REWARD, baseReward * (1 + variance));
    }

    /**
     * 将 BastionDao 映射到 DaoHenHelper.DaoType。
     */
    private static DaoHenHelper.DaoType mapBastionDaoToDaoHenType(BastionDao dao) {
        return switch (dao) {
            case ZHI_DAO -> DaoHenHelper.DaoType.ZHI_DAO;
            case HUN_DAO -> DaoHenHelper.DaoType.HUN_DAO;
            case MU_DAO -> DaoHenHelper.DaoType.MU_DAO;
            case LI_DAO -> DaoHenHelper.DaoType.LI_DAO;
        };
    }

    /**
     * 汇总奖励并在间隔后发送消息（防止刷屏）。
     */
    private static void aggregateAndNotify(
            ServerPlayer player,
            BastionDao dao,
            String resourceName,
            double reward,
            double daoHenReward,
            int tier) {

        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        RewardAggregator aggregator = REWARD_AGGREGATORS.compute(playerId, (id, existing) -> {
            if (existing == null || currentTime - existing.startTime > MessageConfig.AGGREGATION_INTERVAL_MS) {
                // 如果旧的聚合器存在且超时，先发送汇总消息
                if (existing != null && existing.count > 0) {
                    sendAggregatedMessage(player, existing);
                }
                return new RewardAggregator(currentTime, dao, resourceName);
            }
            return existing;
        });

        // 累加奖励
        aggregator.addReward(reward, daoHenReward, tier);
    }

    /**
     * 发送汇总后的奖励消息。
     */
    private static void sendAggregatedMessage(ServerPlayer player, RewardAggregator aggregator) {
        String daoHenPart = aggregator.totalDaoHen > 0
            ? String.format(" + §d%.1f 道痕§r", aggregator.totalDaoHen)
            : "";

        String message = String.format(
            "§a[节点破坏] §e+%.0f %s§r%s（%d个%d转节点）",
            aggregator.totalReward,
            aggregator.resourceName,
            daoHenPart,
            aggregator.count,
            aggregator.maxTier
        );

        player.sendSystemMessage(Component.literal(message));
    }

    /**
     * 奖励汇总器 - 用于防止频繁破坏节点时刷屏。
     */
    private static final class RewardAggregator {
        final long startTime;
        final BastionDao dao;
        final String resourceName;
        double totalReward;
        double totalDaoHen;
        int count;
        int maxTier;

        RewardAggregator(long startTime, BastionDao dao, String resourceName) {
            this.startTime = startTime;
            this.dao = dao;
            this.resourceName = resourceName;
            this.totalReward = 0;
            this.totalDaoHen = 0;
            this.count = 0;
            this.maxTier = 0;
        }

        void addReward(double reward, double daoHen, int tier) {
            this.totalReward += reward;
            this.totalDaoHen += daoHen;
            this.count++;
            this.maxTier = Math.max(this.maxTier, tier);
        }
    }
}
