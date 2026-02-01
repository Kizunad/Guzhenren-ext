package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.aura.AuraNodeType;
import com.Kizunad.guzhenrenext.bastion.block.BastionAuraNodeBlock;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import java.util.List;
import com.Kizunad.guzhenrenext.bastion.skill.BastionHighTierSkillService;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地领域光环服务 - 处理玩家进入基地领地时的持续资源压制。
 * <p>
 * 当玩家处于 ACTIVE 状态基地的领地范围内时，每秒消耗少量资源：
 * <ul>
 *   <li>智道基地：消耗念头</li>
 *   <li>魂道基地：消耗魂魄</li>
 *   <li>木道基地：消耗真元</li>
 *   <li>力道基地：消耗精力</li>
 * </ul>
 * </p>
 *
 * <h2>设计说明</h2>
 * <ul>
 *   <li>消耗量远低于守卫攻击，仅作为「进入领地有代价」的提示</li>
 *   <li>SEALED 或 DESTROYED 状态的基地不产生领域压制</li>
 *   <li>玩家可以通过封印基地来暂时解除压制</li>
 * </ul>
 */
@EventBusSubscriber(modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID)
public final class BastionAuraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionAuraService.class);

    private BastionAuraService() {
        // 工具类
    }

    // ===== 光环配置常量 =====

    /**
     * 光环效果时间配置。
     */
    private static final class TimeConfig {
        /** 光环效果检测间隔（刻）。 */
        static final int TICK_INTERVAL = 20;
        /** 消息冷却时间（毫秒）。 */
        static final long MESSAGE_COOLDOWN_MS = 10000L;

        private TimeConfig() {
        }
    }

    /**
     * 隐匿光环配置。
     */
    private static final class StealthConfig {
        /** 隐身效果持续时间（刻），略高于检测间隔避免闪烁。 */
        static final int EFFECT_DURATION_TICKS = 50;

        private StealthConfig() {
        }
    }

    /**
     * 主资源消耗配置（按道途分配）。
     */
    private static final class ResourceConfig {
        /** 1转基础消耗（每秒）。 */
        static final double TIER_1_BASE = 1.0;
        /** 每转增长倍率。 */
        static final double TIER_MULTIPLIER = 2.0;
        /** 最大消耗上限。 */
        static final double MAX_DRAIN = 50.0;
        /** 魂魄最低保留值（光环不应直接杀死玩家）。 */
        static final double HUNPO_FLOOR = 1.0;

        private ResourceConfig() {
        }
    }

    /**
     * 领域搜索配置。
     */
    private static final class SearchConfig {
        /** 搜索基地的最大半径。 */
        static final int MAX_SEARCH_RADIUS = 128;
        /** Chunk 大小的位移量（16 = 2^4）。 */
        static final int CHUNK_BITS = 4;

        private SearchConfig() {
        }
    }

    /** 玩家消息冷却记录。 */
    private static final java.util.Map<java.util.UUID, Long> MESSAGE_COOLDOWNS =
        new java.util.concurrent.ConcurrentHashMap<>();

    /** 玩家上次所在基地记录（用于进入/离开检测）。 */
    private static final java.util.Map<java.util.UUID, java.util.UUID> PLAYER_IN_BASTION =
        new java.util.concurrent.ConcurrentHashMap<>();

    // ===== 事件处理 =====

    /**
     * 处理玩家刻事件。
     * <p>
     * 每秒检测一次玩家是否在基地领域内，并应用资源消耗。
     * 支持多个基地光环重叠，玩家会受到所有覆盖光环的效果（距离衰减后叠加）。
     * </p>
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        long gameTime = level.getGameTime();

        // 仅每秒处理一次
        if (gameTime % TimeConfig.TICK_INTERVAL != 0) {
            return;
        }

        // 查找玩家所在的所有基地（支持光环重叠）
        BlockPos playerPos = player.blockPosition();
        BastionSavedData savedData = BastionSavedData.get(level);
        List<BastionData> bastions = findBastionsContainingPlayer(savedData, playerPos, gameTime);

        java.util.UUID playerId = player.getUUID();
        java.util.UUID previousBastionId = PLAYER_IN_BASTION.get(playerId);

        if (!bastions.isEmpty()) {
            // 玩家在至少一个基地领域内
            // 使用第一个基地的 ID 作为"主基地"用于进入/离开消息
            BastionData primaryBastion = bastions.get(0);
            java.util.UUID currentBastionId = primaryBastion.id();

            // 检测是否刚进入新基地
            if (previousBastionId == null || !previousBastionId.equals(currentBastionId)) {
                PLAYER_IN_BASTION.put(playerId, currentBastionId);
                sendEnterMessage(player, primaryBastion);
            }

            // 应用所有覆盖光环的效果（距离衰减后叠加）
            for (BastionData bastion : bastions) {
                applyAuraEffect(player, bastion, playerPos);

                // 驱动高转被动技能/特效（每秒触发一次，内部会做每基地节流）
                BastionHighTierSkillService.runPassiveEffects(level, bastion, gameTime);
            }
        } else {
            // 玩家不在任何基地领域内
            if (previousBastionId != null) {
                PLAYER_IN_BASTION.remove(playerId);
                sendLeaveMessage(player);
            }
        }
    }

    /**
     * 处理维度级光环效果（隐匿）。
     * <p>
     * 每秒扫描当前维度的活跃基地：
     * <ul>
     *   <li>基地必须存在 STEALTH 类型光环节点</li>
     *   <li>对基地范围内的守卫实体施加短时隐身（非玩家）</li>
     * </ul>
     * 持续时间略大于检测间隔，保证不闪烁但不过度叠加。
     * </p>
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime % TimeConfig.TICK_INTERVAL != 0) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        for (BastionData bastion : savedData.getAllBastions()) {
            if (bastion.getEffectiveState(gameTime) != BastionState.ACTIVE) {
                continue;
            }
            if (!hasStealthAuraNode(level, savedData, bastion)) {
                continue;
            }
            applyStealthToGuardians(level, bastion);
        }
    }

    // ===== 基地查找 =====

    /**
     * 查找包含玩家位置的所有活跃基地（支持光环重叠）。
     * <p>
     * 使用 auraRadius（而非 growthRadius）判定玩家是否在光环范围内。
     * 支持多个基地光环重叠的场景。
     * </p>
     * <p>
     * 优化：使用 chunk 距离预过滤，减少精确距离计算次数。
     * </p>
     *
     * @param savedData 基地存储数据
     * @param playerPos 玩家位置
     * @param gameTime  当前游戏时间
     * @return 包含玩家的所有活跃基地列表（可能为空）
     */
    private static List<BastionData> findBastionsContainingPlayer(
            BastionSavedData savedData,
            BlockPos playerPos,
            long gameTime) {
        List<BastionData> result = new java.util.ArrayList<>();
        int playerChunkX = playerPos.getX() >> SearchConfig.CHUNK_BITS;
        int playerChunkZ = playerPos.getZ() >> SearchConfig.CHUNK_BITS;

        for (BastionData bastion : savedData.getAllBastions()) {
            // 仅检查 ACTIVE 状态的基地
            if (bastion.getEffectiveState(gameTime) != BastionState.ACTIVE) {
                continue;
            }

            BlockPos corePos = bastion.corePos();
            int auraRadius = bastion.getAuraRadius();  // 使用光环半径而非节点扩张半径

            // Chunk 距离预过滤（+1 容错边界）
            int coreChunkX = corePos.getX() >> SearchConfig.CHUNK_BITS;
            int coreChunkZ = corePos.getZ() >> SearchConfig.CHUNK_BITS;
            int maxChunkDist = (auraRadius >> SearchConfig.CHUNK_BITS) + 1;

            int chunkDistX = Math.abs(playerChunkX - coreChunkX);
            int chunkDistZ = Math.abs(playerChunkZ - coreChunkZ);

            if (chunkDistX > maxChunkDist || chunkDistZ > maxChunkDist) {
                continue;  // 快速跳过远距离基地
            }

            // 精确距离检查（使用 auraRadius）
            double distSq = playerPos.distSqr(corePos);
            if (distSq <= (long) auraRadius * auraRadius) {
                result.add(bastion);
            }
        }
        return result;
    }

    /**
     * 查找包含玩家位置的活跃基地（兼容旧接口，返回第一个匹配）。
     *
     * @param savedData 基地存储数据
     * @param playerPos 玩家位置
     * @param gameTime  当前游戏时间
     * @return 包含玩家的活跃基地，如果没有则返回 null
     */
    private static BastionData findBastionContainingPlayer(
            BastionSavedData savedData,
            BlockPos playerPos,
            long gameTime) {
        List<BastionData> bastions = findBastionsContainingPlayer(savedData, playerPos, gameTime);
        return bastions.isEmpty() ? null : bastions.get(0);
    }

    // ===== 光环效果 =====

    /**
     * 应用光环资源消耗效果（按道途分配资源类型 + 距离衰减）。
     * <p>
     * 资源消耗根据玩家到核心的距离进行衰减：
     * <ul>
     *   <li>中心区域：效果最强（接近 100%）</li>
     *   <li>边缘区域：效果最弱（接近 minFalloff，默认 5%）</li>
     * </ul>
     * 衰减公式：drain = baseDrain * (1 - distance/auraRadius)^falloffPower
     * </p>
     * <p>
     * 对于魂道基地，魂魄消耗有下限保护（最低保留 1），
     * 光环不应直接导致玩家死亡。
     * </p>
     *
     * @param player    目标玩家
     * @param bastion   所在基地
     * @param playerPos 玩家位置
     */
    private static void applyAuraEffect(ServerPlayer player, BastionData bastion, BlockPos playerPos) {
        int tier = bastion.tier();
        BastionDao dao = bastion.primaryDao();

        // 计算距离和衰减因子
        double distance = Math.sqrt(playerPos.distSqr(bastion.corePos()));
        double falloff = bastion.getAuraFalloff(distance);

        // 如果衰减因子为 0（超出光环范围），不应用效果
        if (falloff <= 0) {
            return;
        }

        // 计算基础消耗量（基于转数）
        double baseDrain = Math.min(
            ResourceConfig.MAX_DRAIN,
            ResourceConfig.TIER_1_BASE * Math.pow(ResourceConfig.TIER_MULTIPLIER, tier - 1)
        );

        // 应用距离衰减
        double drain = baseDrain * falloff;

        // 道途特化光环效果（如智道：疲劳+缓慢）
        dao.onAuraTick(player, tier, falloff);

        // 按道途分配资源消耗
        String resourceName;
        double actualDrain = drain;
        switch (dao) {
            case ZHI_DAO -> {
                NianTouHelper.modify(player, -drain);
                resourceName = "念头";
            }
            case HUN_DAO -> {
                // 魂魄下限保护：光环不应直接杀死玩家
                double currentHunpo = HunPoHelper.getAmount(player);
                double safeDrain = Math.max(0, currentHunpo - ResourceConfig.HUNPO_FLOOR);
                actualDrain = Math.min(drain, safeDrain);
                if (actualDrain > 0) {
                    HunPoHelper.modify(player, -actualDrain);
                }
                resourceName = "魂魄";
            }
            case MU_DAO -> {
                ZhenYuanHelper.modify(player, -drain);
                resourceName = "真元";
            }
            case LI_DAO -> {
                JingLiHelper.modify(player, -drain);
                resourceName = "精力";
            }
            default -> {
                ZhenYuanHelper.modify(player, -drain);
                resourceName = "真元";
            }
        }

        LOGGER.trace("玩家 {} 在 {}转 {} 基地领域内，距离 {} 衰减 {}，消耗 {} {}",
            player.getName().getString(),
            tier,
            dao.getSerializedName(),
            distance,
            falloff,
            actualDrain,
            resourceName);
    }

    /**
     * 检查基地是否拥有 STEALTH 类型光环节点。
     * <p>
     * 利用 Anchor 缓存：遍历基地已记录的 Anchor，检查其上方的光环节点方块是否为
     * STEALTH 类型。未找到则视为无隐匿光环。
     * </p>
     */
    private static boolean hasStealthAuraNode(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion) {
        java.util.Set<BlockPos> anchors = savedData.getAnchors(bastion.id());
        if (anchors == null || anchors.isEmpty()) {
            return false;
        }

        for (BlockPos anchorPos : anchors) {
            BlockPos nodePos = anchorPos.above();
            var state = level.getBlockState(nodePos);
            if (!(state.getBlock() instanceof BastionAuraNodeBlock)) {
                continue;
            }
            AuraNodeType type = state.getValue(BastionAuraNodeBlock.AURA_TYPE);
            if (type == AuraNodeType.STEALTH) {
                return true;
            }
        }
        return false;
    }

    /**
     * 为基地范围内的守卫实体施加隐身效果。
     * <p>
     * 仅影响属于该基地的守卫（BastionGuardianData 判断），不影响玩家或其它实体。
     * 以 auraRadius 为范围，垂直方向同样使用 radius 兜底覆盖高低差。
     * </p>
     */
    private static void applyStealthToGuardians(ServerLevel level, BastionData bastion) {
        int auraRadius = bastion.getAuraRadius();
        if (auraRadius <= 0) {
            return;
        }

        BlockPos core = bastion.corePos();
        AABB box = new AABB(core).inflate(auraRadius, auraRadius, auraRadius);

        List<Mob> guardians = level.getEntitiesOfClass(Mob.class, box, mob ->
            BastionGuardianData.isGuardian(mob)
                && BastionGuardianData.belongsToBastion(mob, bastion.id())
        );

        if (guardians.isEmpty()) {
            return;
        }

        for (Mob guardian : guardians) {
            guardian.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                StealthConfig.EFFECT_DURATION_TICKS,
                0,
                true,
                false,
                false
            ));
        }
    }

    // ===== 消息通知 =====

    /**
     * 发送进入领域消息。
     */
    private static void sendEnterMessage(ServerPlayer player, BastionData bastion) {
        long currentTime = System.currentTimeMillis();
        java.util.UUID playerId = player.getUUID();

        Long lastMessage = MESSAGE_COOLDOWNS.get(playerId);
        if (lastMessage != null
                && (currentTime - lastMessage) < TimeConfig.MESSAGE_COOLDOWN_MS) {
            return;
        }

        MESSAGE_COOLDOWNS.put(playerId, currentTime);

        // 根据道途显示消耗的资源类型
        String resourceName = switch (bastion.primaryDao()) {
            case ZHI_DAO -> "念头";
            case HUN_DAO -> "魂魄";
            case MU_DAO -> "真元";
            case LI_DAO -> "精力";
        };

        String message = String.format(
            "§c[领域压制] §e你进入了 %d转 %s 基地的领域范围！\n" +
                "§7在此区域内，你的 %s 将持续消耗...",
            bastion.tier(),
            bastion.primaryDao().getSerializedName(),
            resourceName
        );

        player.sendSystemMessage(Component.literal(message));
    }

    /**
     * 发送离开领域消息。
     */
    private static void sendLeaveMessage(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(
            "§a[领域解除] §f你已离开基地领域范围，资源消耗停止。"
        ));
    }
}
