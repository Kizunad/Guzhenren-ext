package com.Kizunad.guzhenrenext.bastion.threat;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import com.Kizunad.guzhenrenext.bastion.service.BastionThreatService;
import com.Kizunad.guzhenrenext.bastion.threat.impl.ExpansionSurgeEvent;
import com.Kizunad.guzhenrenext.bastion.threat.impl.HunterSpawnEvent;
import com.Kizunad.guzhenrenext.bastion.threat.impl.RadiationPulseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 威胁事件服务 - 管理节点拆除后的随机威胁事件触发。
 * <p>
 * 设计目标：
 * <ul>
 *   <li>增加拆除节点的风险和紧张感</li>
 *   <li>基于概率的随机触发机制</li>
 *   <li>多种事件类型的加权随机选择</li>
 *   <li>冷却机制避免事件堆叠</li>
 * </ul>
 * </p>
 */
public final class ThreatEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreatEventService.class);

    private ThreatEventService() {
        // 工具类
    }

    // ===== 配置常量 =====

    /** 基础触发概率（每次节点拆除）。 */
    private static final double BASE_TRIGGER_CHANCE = 0.15;

    /** 转数触发概率加成（每转增加的概率）。 */
    private static final double TIER_CHANCE_BONUS = 0.03;

    /** 节点损失比例对概率的加成系数。 */
    private static final double LOSS_RATIO_CHANCE_MULTIPLIER = 0.5;

    /** 事件全局冷却时间（刻）- 防止短时间内连续触发。 */
    private static final long GLOBAL_COOLDOWN_TICKS = 200L;  // 10 秒

    /** 单个基地的事件冷却时间（刻）。 */
    private static final long BASTION_COOLDOWN_TICKS = 600L;  // 30 秒

    // ===== 逆转阵法（偷家）相关配置 =====

    /** 逆转阵法触发威胁的基础概率（每秒判定一次）。 */
    private static final double REVERSAL_BASE_TRIGGER_CHANCE = 0.75;

    /** 逆转阵法转数触发概率加成（每转增加的概率）。 */
    private static final double REVERSAL_TIER_CHANCE_BONUS = 0.02;

    /** 逆转阵法全局冷却（刻）。 */
    private static final long REVERSAL_GLOBAL_COOLDOWN_TICKS = 60L;  // 3 秒

    /** 逆转阵法单基地冷却（刻）。 */
    private static final long REVERSAL_BASTION_COOLDOWN_TICKS = 200L;  // 10 秒

    /** 逆转阵法事件池：猎手生成权重。 */
    private static final int REVERSAL_HUNTER_WEIGHT = 60;

    /** 逆转阵法事件池：辐射脉冲权重。 */
    private static final int REVERSAL_PULSE_WEIGHT = 40;


    /**
     * 节点拆除触发突变的基础概率。
     */
    private static final double BASE_MUTATION_CHANCE = 0.05;

    /**
     * 每转增加的突变概率。
     */
    private static final double MUTATION_TIER_BONUS = 0.01;

    /**
     * 同一基地突变冷却时间（刻）。
     */
    private static final long MUTATION_COOLDOWN_TICKS = 2400L; // 2 分钟

    /**
     * 突变冲击持续时间（刻）。
     */
    private static final int MUTATION_PULSE_DURATION_TICKS = 60;

    /**
     * 突变冲击基础伤害。
     */
    private static final float MUTATION_PULSE_BASE_DAMAGE = 1.0f;

    /**
     * 突变冲击每转伤害加成。
     */
    private static final float MUTATION_PULSE_DAMAGE_PER_TIER = 0.5f;

    /**
     * 突变冲击最大伤害。
     */
    private static final float MUTATION_PULSE_MAX_DAMAGE = 6.0f;

    /**
     * 突变冲击最大效果等级。
     */
    private static final int MUTATION_PULSE_MAX_AMPLIFIER = 2;

    /**
     * 转数等级换算：每 3 转提升一档效果。
     */
    private static final int AMPLIFIER_TIER_DIVISOR = 3;

    /**
     * 非配置化常量。
     */
    private static final class Constants {
        /** 触发概率上限。 */
        static final double MAX_TRIGGER_CHANCE = 0.8;

        private Constants() {
        }
    }

    /** 威胁值相关常量。 */
    private static final class ThreatConstants {
        /** 节点破坏时增加的威胁值。 */
        static final int NODE_DESTROYED_THREAT_GAIN = 10;

        private ThreatConstants() {
        }
    }

    // ===== 运行时状态 =====

    /** 已注册的威胁事件列表。 */
    private static final List<IThreatEvent> REGISTERED_EVENTS = new ArrayList<>();

    /** 基地冷却追踪：bastionId -> 上次触发时间。 */
    private static final Map<UUID, Long> BASTION_COOLDOWNS = new HashMap<>();

    /** 逆转阵法基地冷却追踪：bastionId -> 上次触发时间。 */
    private static final Map<UUID, Long> REVERSAL_BASTION_COOLDOWNS = new HashMap<>();

    /** 基地突变冷却追踪：bastionId -> 上次突变时间。 */
    private static final Map<UUID, Long> BASTION_MUTATION_COOLDOWNS = new HashMap<>();

    /** 全局上次触发时间。 */
    private static long lastGlobalTriggerTime = 0L;

    /** 逆转阵法全局上次触发时间。 */
    private static long lastReversalGlobalTriggerTime = 0L;

    // ===== 初始化 =====

    static {
        // 注册默认威胁事件
        registerEvent(new RadiationPulseEvent());
        registerEvent(new HunterSpawnEvent());
        registerEvent(new ExpansionSurgeEvent());
    }

    /**
     * 注册威胁事件。
     *
     * @param event 威胁事件实现
     */
    public static void registerEvent(IThreatEvent event) {
        REGISTERED_EVENTS.add(event);
        LOGGER.debug("注册威胁事件: {}", event.getId());
    }

    // ===== 公开 API =====

    /**
     * 尝试在节点被拆除后触发威胁事件。
     * <p>
     * 此方法应在节点成功拆除后调用。
     * </p>
     *
     * @param level           服务端世界
     * @param savedData       基地存档数据
     * @param bastion         基地数据（拆除后状态）
     * @param destroyedPos    被拆除节点的位置
     * @param nodeCountBefore 拆除前节点数
     * @param gameTime        当前游戏时间
     * @return true 如果触发了事件
     */
    public static boolean tryTriggerOnNodeDestroyed(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BlockPos destroyedPos,
            int nodeCountBefore,
            long gameTime) {

        // 冷却检查
        if (!checkCooldowns(bastion.id(), gameTime)) {
            return false;
        }

        // 计算触发概率
        double triggerChance = calculateTriggerChance(bastion, nodeCountBefore);

        // 节点被拆除直接增加威胁值（用于孵化巢强度驱动）。
        BastionThreatService.addThreat(level, bastion, ThreatConstants.NODE_DESTROYED_THREAT_GAIN);

        // 随机判定
        RandomSource random = level.getRandom();
        if (random.nextDouble() >= triggerChance) {
            LOGGER.trace("威胁事件未触发（概率检查失败）: bastion={}, chance={}",
                bastion.id(), triggerChance);
            return false;
        }

        // 收集附近玩家
        List<ServerPlayer> nearbyPlayers = collectNearbyPlayers(level, bastion);

        // 构建上下文
        ThreatEventContext context = new ThreatEventContext(
            level,
            bastion,
            destroyedPos,
            gameTime,
            nearbyPlayers,
            nodeCountBefore,
            bastion.totalNodes(),
            random
        );

        // 选择并执行事件
        IThreatEvent selectedEvent = selectEvent(context, random);
        if (selectedEvent == null) {
            LOGGER.trace("无可用威胁事件: bastion={}", bastion.id());
            return false;
        }

        // 执行事件
        LOGGER.info("触发威胁事件 {} 于基地 {} (概率={}, 转数={})",
            selectedEvent.getId(), bastion.id(), triggerChance, bastion.tier());
        selectedEvent.execute(context);

        // 更新冷却
        updateCooldowns(bastion.id(), gameTime);

        // 尝试触发基地突变（获得新词缀）
        tryTriggerMutation(level, savedData, bastion, nodeCountBefore, gameTime, random);

        // 如果事件可能改变了基地状态，同步到客户端
        BastionData updatedBastion = savedData.getBastion(bastion.id());
        if (updatedBastion != null) {
            BastionNetworkHandler.syncIfAuraRadiusChanged(level, updatedBastion);
        }

        return true;
    }

    /**
     * 逆转阵法运行时尝试触发威胁事件。
     * <p>
     * 设计目标：高风险高收益，阵法每秒工作时都有较高概率引发基地反扑。
     * 与“拆节点威胁事件”使用独立冷却，互不干扰。
     * </p>
     *
     * @param level    服务端世界
     * @param savedData 基地存档数据
     * @param bastion  基地数据
     * @param arrayPos 阵法位置
     * @param gameTime 当前游戏时间
     * @return true 如果触发了事件
     */
    public static boolean tryTriggerOnReversalArray(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            BlockPos arrayPos,
            long gameTime) {
        if (level == null || savedData == null || bastion == null || arrayPos == null) {
            return false;
        }

        if (!checkReversalCooldowns(bastion.id(), gameTime)) {
            return false;
        }

        List<ServerPlayer> nearbyPlayers = collectNearbyPlayers(level, bastion);
        if (nearbyPlayers.isEmpty()) {
            return false;
        }

        double chance = REVERSAL_BASE_TRIGGER_CHANCE
            + (bastion.tier() - 1) * REVERSAL_TIER_CHANCE_BONUS;
        chance = Math.min(Constants.MAX_TRIGGER_CHANCE, chance);

        RandomSource random = level.getRandom();
        if (random.nextDouble() >= chance) {
            return false;
        }

        ThreatEventContext context = new ThreatEventContext(
            level,
            bastion,
            arrayPos,
            gameTime,
            nearbyPlayers,
            bastion.totalNodes(),
            bastion.totalNodes(),
            random
        );

        IThreatEvent event = selectReversalEvent(context, random);
        if (event == null) {
            return false;
        }

        LOGGER.info("逆转阵法触发威胁事件 {} 于基地 {} (概率={}, 转数={})",
            event.getId(), bastion.id(), chance, bastion.tier());
        event.execute(context);

        updateReversalCooldowns(bastion.id(), gameTime);
        return true;
    }

    private static void tryTriggerMutation(
            ServerLevel level,
            BastionSavedData savedData,
            BastionData bastion,
            int nodeCountBefore,
            long gameTime,
            RandomSource random) {
        if (bastion == null) {
            return;
        }

        Long lastMutation = BASTION_MUTATION_COOLDOWNS.get(bastion.id());
        if (lastMutation != null && gameTime - lastMutation < MUTATION_COOLDOWN_TICKS) {
            return;
        }

        double chance = BASE_MUTATION_CHANCE + (bastion.tier() - 1) * MUTATION_TIER_BONUS;
        // 节点损失比例越大越容易突变
        double lossRatio = 0.0;
        if (nodeCountBefore > 0) {
            lossRatio = 1.0 - ((double) bastion.totalNodes() / nodeCountBefore);
        }
        chance *= (1.0 + lossRatio);
        chance = Math.min(Constants.MAX_TRIGGER_CHANCE, chance);

        if (random.nextDouble() >= chance) {
            return;
        }

        BastionModifier gained = selectMutationModifier(bastion, random);
        if (gained == null) {
            return;
        }

        java.util.Set<BastionModifier> newModifiers = new java.util.HashSet<>(bastion.modifiers());
        if (!newModifiers.add(gained)) {
            return;
        }

        BastionData updated = bastion.withModifiers(newModifiers);
        savedData.updateBastion(updated);
        BASTION_MUTATION_COOLDOWNS.put(bastion.id(), gameTime);

        // 戏剧性反馈：音效 + 粒子 + 冲击波（短时负面效果 + 轻量伤害）
        BastionSoundPlayer.playThreat(level, updated.corePos());
        BastionParticles.spawnThreatParticles(level, updated.corePos(), updated.primaryDao());
        List<ServerPlayer> nearbyPlayers = collectNearbyPlayers(level, updated);
        applyMutationPulse(level, updated, nearbyPlayers);

        // 同步到客户端，让边界/信息立即更新
        BastionNetworkHandler.syncToNearbyPlayers(level, updated);

        // 通知附近玩家（避免刷屏：这里只在突变发生时提示一次）
        for (ServerPlayer player : nearbyPlayers) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c基地发生突变：获得词缀 " + gained.getSerializedName()
            ));
        }

        LOGGER.info("基地 {} 发生突变，获得词缀 {}", bastion.id(), gained.getSerializedName());
    }

    private static void applyMutationPulse(
            ServerLevel level,
            BastionData bastion,
            List<ServerPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        float damage = Math.min(
            MUTATION_PULSE_MAX_DAMAGE,
            MUTATION_PULSE_BASE_DAMAGE
                + (bastion.tier() - 1) * MUTATION_PULSE_DAMAGE_PER_TIER
        );
        int amplifier = Math.min(
            MUTATION_PULSE_MAX_AMPLIFIER,
            Math.max(0, bastion.tier() / AMPLIFIER_TIER_DIVISOR)
        );

        for (ServerPlayer player : players) {
            // 轻量伤害 + 短时压制
            player.hurt(level.damageSources().magic(), damage);
            player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                MUTATION_PULSE_DURATION_TICKS,
                amplifier,
                false,
                true,
                true
            ));
            player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                MUTATION_PULSE_DURATION_TICKS,
                amplifier,
                false,
                true,
                true
            ));
        }
    }

    private static BastionModifier selectMutationModifier(
            BastionData bastion,
            RandomSource random) {
        // MVP：先只在 HARDENED / VOLATILE 里选
        BastionModifier[] pool = new BastionModifier[] {
            BastionModifier.HARDENED,
            BastionModifier.VOLATILE
        };

        java.util.List<BastionModifier> candidates = new java.util.ArrayList<>();
        for (BastionModifier modifier : pool) {
            if (!bastion.modifiers().contains(modifier)) {
                candidates.add(modifier);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * 清除指定基地的冷却状态。
     *
     * @param bastionId 基地 UUID
     */
    public static void clearCooldown(UUID bastionId) {
        BASTION_COOLDOWNS.remove(bastionId);
        REVERSAL_BASTION_COOLDOWNS.remove(bastionId);
    }

    /**
     * 清除所有冷却状态。
     */
    public static void clearAllCooldowns() {
        BASTION_COOLDOWNS.clear();
        REVERSAL_BASTION_COOLDOWNS.clear();
        lastGlobalTriggerTime = 0L;
        lastReversalGlobalTriggerTime = 0L;
    }

    // ===== 内部方法 =====

    /**
     * 检查冷却是否允许触发。
     */
    private static boolean checkCooldowns(UUID bastionId, long gameTime) {
        // 全局冷却检查
        if (gameTime - lastGlobalTriggerTime < GLOBAL_COOLDOWN_TICKS) {
            return false;
        }

        // 基地冷却检查
        Long lastTrigger = BASTION_COOLDOWNS.get(bastionId);
        if (lastTrigger != null && gameTime - lastTrigger < BASTION_COOLDOWN_TICKS) {
            return false;
        }

        return true;
    }

    /**
     * 更新冷却记录。
     */
    private static void updateCooldowns(UUID bastionId, long gameTime) {
        lastGlobalTriggerTime = gameTime;
        BASTION_COOLDOWNS.put(bastionId, gameTime);
    }

    private static boolean checkReversalCooldowns(UUID bastionId, long gameTime) {
        if (gameTime - lastReversalGlobalTriggerTime < REVERSAL_GLOBAL_COOLDOWN_TICKS) {
            return false;
        }
        Long last = REVERSAL_BASTION_COOLDOWNS.get(bastionId);
        return last == null || gameTime - last >= REVERSAL_BASTION_COOLDOWN_TICKS;
    }

    private static void updateReversalCooldowns(UUID bastionId, long gameTime) {
        lastReversalGlobalTriggerTime = gameTime;
        REVERSAL_BASTION_COOLDOWNS.put(bastionId, gameTime);
    }

    /**
     * 计算威胁事件触发概率。
     * <p>
     * 公式：chance = baseChance + tierBonus + lossRatioBonus
     * </p>
     */
    private static double calculateTriggerChance(BastionData bastion, int nodeCountBefore) {
        double baseChance = BASE_TRIGGER_CHANCE;

        // 转数加成：高转基地更危险
        double tierBonus = (bastion.tier() - 1) * TIER_CHANCE_BONUS;

        // 节点损失比例加成：拆得越多越危险
        double lossRatio = 0.0;
        if (nodeCountBefore > 0) {
            lossRatio = 1.0 - ((double) bastion.totalNodes() / nodeCountBefore);
        }
        double lossBonus = lossRatio * LOSS_RATIO_CHANCE_MULTIPLIER;

        return Math.min(Constants.MAX_TRIGGER_CHANCE, baseChance + tierBonus + lossBonus);
    }

    /**
     * 收集光环范围内的玩家。
     */
    private static List<ServerPlayer> collectNearbyPlayers(ServerLevel level, BastionData bastion) {
        List<ServerPlayer> result = new ArrayList<>();
        int auraRadius = bastion.getAuraRadius();
        long radiusSq = (long) auraRadius * auraRadius;

        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - bastion.corePos().getX();
            double dz = player.getZ() - bastion.corePos().getZ();
            if (dx * dx + dz * dz <= radiusSq) {
                result.add(player);
            }
        }

        return result;
    }

    /**
     * 基于权重随机选择一个可触发的事件。
     */
    private static IThreatEvent selectEvent(ThreatEventContext context, RandomSource random) {
        // 收集可触发的事件及其权重
        List<IThreatEvent> candidates = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int totalWeight = 0;

        for (IThreatEvent event : REGISTERED_EVENTS) {
            if (event.canTrigger(context)) {
                candidates.add(event);
                int weight = event.getBaseWeight();
                weights.add(weight);
                totalWeight += weight;
            }
        }

        if (candidates.isEmpty() || totalWeight <= 0) {
            return null;
        }

        // 加权随机选择
        // totalWeight 理论上等于 100，但仍以实际值为准。
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return candidates.get(i);
            }
        }

        return candidates.get(candidates.size() - 1);  // fallback
    }

    private static IThreatEvent selectReversalEvent(
            ThreatEventContext context,
            RandomSource random) {
        IThreatEvent hunter = new HunterSpawnEvent();
        IThreatEvent pulse = new RadiationPulseEvent();

        int totalWeight = 0;
        java.util.List<IThreatEvent> candidates = new java.util.ArrayList<>();
        java.util.List<Integer> weights = new java.util.ArrayList<>();

        if (hunter.canTrigger(context)) {
            candidates.add(hunter);
            weights.add(REVERSAL_HUNTER_WEIGHT);
            totalWeight += REVERSAL_HUNTER_WEIGHT;
        }
        if (pulse.canTrigger(context)) {
            candidates.add(pulse);
            weights.add(REVERSAL_PULSE_WEIGHT);
            totalWeight += REVERSAL_PULSE_WEIGHT;
        }

        if (candidates.isEmpty() || totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return candidates.get(i);
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
