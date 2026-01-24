package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地守卫攻击服务 - 处理守卫对玩家的资源压制效果。
 * <p>
 * 当基地守卫对玩家造成伤害时，根据转数消耗玩家的修炼资源：
 * <ul>
 *   <li>真元：主要消耗，按转数指数增长</li>
 *   <li>念头：次要消耗，影响玩家操纵蛊虫能力</li>
 *   <li>精力：次要消耗，影响玩家行动能力</li>
 *   <li>魂魄：少量消耗，累积可能导致魂魄消散</li>
 * </ul>
 * </p>
 *
 * <h2>资源消耗量级设计（收敛后）</h2>
 * <p>
 * 基于 GZR_INFO.md 的资源量级，采用 3 倍/转增长（平衡 1-5 转可玩性）：
 * <ul>
 *   <li>真元：基础 5，5转约 400</li>
 *   <li>精力：基础 3，线性增长，上限 50</li>
 *   <li>念头：基础 3，指数增长，上限 100</li>
 *   <li>魂魄：基础 1，指数增长，上限 50</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID)
public final class BastionGuardianAttackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionGuardianAttackService.class);

    private BastionGuardianAttackService() {
        // 工具类
    }

    // ===== 资源消耗配置常量 =====

    /**
     * 真元消耗配置（收敛后）。
     */
    private static final class ZhenyuanConfig {
        /** 1转基础消耗。 */
        static final double TIER_1_BASE = 5.0;
        /** 每转增长倍率（收敛为 3 倍）。 */
        static final double TIER_MULTIPLIER = 3.0;

        private ZhenyuanConfig() {
        }
    }

    /**
     * 精力消耗配置（收敛后）。
     */
    private static final class JingliConfig {
        /** 1转基础消耗。 */
        static final double TIER_1_BASE = 3.0;
        /** 每转增长系数（线性）。 */
        static final double TIER_SCALE = 1.5;
        /** 最大消耗上限（收敛）。 */
        static final double MAX_DRAIN = 50.0;

        private JingliConfig() {
        }
    }

    /**
     * 念头消耗配置（收敛后）。
     */
    private static final class NiantouConfig {
        /** 1转基础消耗。 */
        static final double TIER_1_BASE = 3.0;
        /** 每转增长系数（指数，收敛为 2 倍）。 */
        static final double TIER_MULTIPLIER = 2.0;
        /** 最大消耗上限（收敛）。 */
        static final double MAX_DRAIN = 100.0;

        private NiantouConfig() {
        }
    }

    /**
     * 魂魄消耗配置（收敛后）。
     */
    private static final class HunpoConfig {
        /** 1转基础消耗。 */
        static final double TIER_1_BASE = 1.0;
        /** 每转增长系数（指数，收敛为 2 倍）。 */
        static final double TIER_MULTIPLIER = 2.0;
        /** 最大消耗上限（收敛）。 */
        static final double MAX_DRAIN = 50.0;

        private HunpoConfig() {
        }
    }

    /**
     * 消息显示配置。
     */
    private static final class MessageConfig {
        /** 消息冷却时间（毫秒）。 */
        static final long MESSAGE_COOLDOWN_MS = 2000L;

        private MessageConfig() {
        }
    }

    /**
     * Debuff 效果配置。
     */
    private static final class DebuffConfig {
        /** 基础效果持续刻数（3秒）。 */
        static final int BASE_DURATION_TICKS = 60;
        /** 每转额外持续刻数（1秒）。 */
        static final int DURATION_PER_TIER = 20;
        /** 高转阈值（5转+开始施加额外 debuff）。 */
        static final int HIGH_TIER_THRESHOLD = 5;
        /** 极高转阈值（7转+开始施加黑暗）。 */
        static final int VERY_HIGH_TIER_THRESHOLD = 7;

        private DebuffConfig() {
        }
    }

    /** 玩家消息冷却记录。 */
    private static final java.util.Map<java.util.UUID, Long> MESSAGE_COOLDOWNS =
        new java.util.concurrent.ConcurrentHashMap<>();

    // ===== 事件处理 =====

    /**
     * 处理生物伤害事件。
     * <p>
     * 当基地守卫对玩家造成伤害时，额外消耗玩家资源并施加 debuff。
     * </p>
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        // 检查受害者是否为服务端玩家
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof ServerPlayer player)) {
            return;
        }

        // 检查攻击者是否为基地守卫
        Entity source = event.getSource().getEntity();
        if (source == null || !BastionGuardianData.isGuardian(source)) {
            return;
        }

        // 获取守卫转数（hasCompleteData 兜底：旧世界遗留守卫使用默认转数 1）
        int tier = BastionGuardianData.getTier(source);
        if (!BastionGuardianData.hasCompleteData(source)) {
            LOGGER.warn("守卫 {} 数据不完整，使用默认转数 {}",
                source.getUUID(), tier);
        }

        // 计算并消耗资源
        ResourceDrain drain = calculateResourceDrain(tier);
        applyResourceDrain(player, drain);

        // 施加 debuff 效果
        applyDebuffs(player, tier);

        // 发送反馈消息（带冷却）
        sendDrainMessage(player, drain, tier);

        LOGGER.debug("守卫（{}转）对玩家 {} 造成资源压制: 真元-{}, 精力-{}, 念头-{}, 魂魄-{}",
            tier,
            player.getName().getString(),
            drain.zhenyuan,
            drain.jingli,
            drain.niantou,
            drain.hunpo);
    }

    // ===== 资源计算 =====

    /**
     * 资源消耗数据记录。
     */
    private record ResourceDrain(
        double zhenyuan,
        double jingli,
        double niantou,
        double hunpo
    ) {
    }

    /**
     * 计算基于转数的资源消耗量。
     *
     * @param tier 守卫转数（1-9）
     * @return 资源消耗记录
     */
    private static ResourceDrain calculateResourceDrain(int tier) {
        // 真元：指数增长（3倍/转，收敛后）
        double zhenyuan = ZhenyuanConfig.TIER_1_BASE
            * Math.pow(ZhenyuanConfig.TIER_MULTIPLIER, tier - 1);

        // 精力：线性增长，有上限
        double jingli = Math.min(
            JingliConfig.MAX_DRAIN,
            JingliConfig.TIER_1_BASE * Math.pow(JingliConfig.TIER_SCALE, tier - 1)
        );

        // 念头：指数增长，有上限
        double niantou = Math.min(
            NiantouConfig.MAX_DRAIN,
            NiantouConfig.TIER_1_BASE * Math.pow(NiantouConfig.TIER_MULTIPLIER, tier - 1)
        );

        // 魂魄：指数增长，有上限
        double hunpo = Math.min(
            HunpoConfig.MAX_DRAIN,
            HunpoConfig.TIER_1_BASE * Math.pow(HunpoConfig.TIER_MULTIPLIER, tier - 1)
        );

        return new ResourceDrain(zhenyuan, jingli, niantou, hunpo);
    }

    /**
     * 应用资源消耗到玩家。
     *
     * @param player 目标玩家
     * @param drain  资源消耗量
     */
    private static void applyResourceDrain(ServerPlayer player, ResourceDrain drain) {
        ZhenYuanHelper.modify(player, -drain.zhenyuan);
        JingLiHelper.modify(player, -drain.jingli);
        NianTouHelper.modify(player, -drain.niantou);
        HunPoHelper.modify(player, -drain.hunpo);

        // 检查魂魄是否耗尽
        HunPoHelper.checkAndKill(player);
    }

    /**
     * 发送资源消耗消息（带冷却防止刷屏）。
     *
     * @param player 目标玩家
     * @param drain  资源消耗量
     * @param tier   守卫转数
     */
    private static void sendDrainMessage(ServerPlayer player, ResourceDrain drain, int tier) {
        long currentTime = System.currentTimeMillis();
        java.util.UUID playerId = player.getUUID();

        Long lastMessage = MESSAGE_COOLDOWNS.get(playerId);
        if (lastMessage != null
                && (currentTime - lastMessage) < MessageConfig.MESSAGE_COOLDOWN_MS) {
            return;
        }

        MESSAGE_COOLDOWNS.put(playerId, currentTime);

        String message = String.format(
            "§c[守卫压制] §e%d转守卫§c 消耗了你的修炼资源！\n" +
                "  真元 §e-%.0f§r | 精力 §e-%.0f§r | 念头 §e-%.0f§r | 魂魄 §e-%.0f§r",
            tier,
            drain.zhenyuan,
            drain.jingli,
            drain.niantou,
            drain.hunpo
        );

        player.sendSystemMessage(Component.literal(message));
    }

    // ===== Debuff 施加 =====

    /**
     * 根据守卫转数施加 debuff 效果。
     * <p>
     * 效果随转数递进：
     * <ul>
     *   <li>1-4转：减速（Slowness）</li>
     *   <li>5-6转：减速 + 挖掘疲劳（Mining Fatigue）</li>
     *   <li>7转+：减速 + 虚弱（Weakness）+ 黑暗（Darkness）</li>
     * </ul>
     * </p>
     *
     * @param player 目标玩家
     * @param tier   守卫转数
     */
    private static void applyDebuffs(ServerPlayer player, int tier) {
        int duration = DebuffConfig.BASE_DURATION_TICKS + (tier * DebuffConfig.DURATION_PER_TIER);
        int amplifier = Math.min(tier - 1, 2);  // 最高 III 级

        // 所有转数：减速
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            duration,
            amplifier,
            false,  // ambient
            true,   // visible
            true    // showIcon
        ));

        // 5转+：挖掘疲劳
        if (tier >= DebuffConfig.HIGH_TIER_THRESHOLD) {
            player.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                duration,
                Math.min(tier - DebuffConfig.HIGH_TIER_THRESHOLD, 2),
                false,
                true,
                true
            ));
        }

        // 7转+：虚弱 + 黑暗
        if (tier >= DebuffConfig.VERY_HIGH_TIER_THRESHOLD) {
            player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                duration,
                Math.min(tier - DebuffConfig.VERY_HIGH_TIER_THRESHOLD, 1),
                false,
                true,
                true
            ));

            player.addEffect(new MobEffectInstance(
                MobEffects.DARKNESS,
                duration / 2,  // 黑暗持续时间减半，避免过于烦人
                0,
                false,
                true,
                true
            ));
        }
    }
}
