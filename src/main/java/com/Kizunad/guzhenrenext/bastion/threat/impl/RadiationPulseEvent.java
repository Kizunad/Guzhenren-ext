package com.Kizunad.guzhenrenext.bastion.threat.impl;

import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.threat.IThreatEvent;
import com.Kizunad.guzhenrenext.bastion.threat.ThreatEventContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 辐射脉冲威胁事件。
 * <p>
 * 效果：对光环范围内的所有玩家施加负面效果（虚弱 + 缓慢 + 伤害）。
 * 强度随基地转数提升。
 * </p>
 */
public final class RadiationPulseEvent implements IThreatEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(RadiationPulseEvent.class);

    /** 事件 ID。 */
    private static final String ID = "radiation_pulse";

    /** 基础权重。 */
    private static final int BASE_WEIGHT = 40;

    /** 效果持续时间（刻）- 基础值。 */
    private static final int BASE_DURATION_TICKS = 200;  // 10 秒

    /** 每转增加的持续时间（刻）。 */
    private static final int DURATION_PER_TIER = 40;  // 2 秒

    /** 基础伤害值。 */
    private static final float BASE_DAMAGE = 2.0f;  // 1 心

    /** 每转增加的伤害。 */
    private static final float DAMAGE_PER_TIER = 1.0f;  // 0.5 心

    /** 最大效果等级（0~3）。 */
    private static final int MAX_AMPLIFIER = 3;

    /** 采矿疲劳最低触发转数。 */
    private static final int MIN_FATIGUE_TIER = 5;

    /** 采矿疲劳等级计算偏移（tier - 4）。 */
    private static final int FATIGUE_TIER_OFFSET = 4;

    /** 采矿疲劳最大等级（0~2）。 */
    private static final int MAX_FATIGUE_AMPLIFIER = 2;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getBaseWeight() {
        return BASE_WEIGHT;
    }

    @Override
    public boolean canTrigger(ThreatEventContext context) {
        // 需要有附近玩家
        return context.hasNearbyPlayers();
    }

    @Override
    public void execute(ThreatEventContext context) {
        int tier = context.bastion().tier();
        int duration = BASE_DURATION_TICKS + (tier - 1) * DURATION_PER_TIER;
        float damage = BASE_DAMAGE + (tier - 1) * DAMAGE_PER_TIER;
        int amplifier = Math.min(tier - 1, MAX_AMPLIFIER);

        LOGGER.debug("执行辐射脉冲: tier={}, duration={}, damage={}, amplifier={}",
            tier, duration, damage, amplifier);

        // 播放音效和粒子
        BastionSoundPlayer.playThreat(context.level(), context.getCorePos());
        BastionParticles.spawnThreatParticles(
            context.level(), context.getCorePos(), context.bastion().primaryDao());

        // 对所有附近玩家施加效果
        for (ServerPlayer player : context.nearbyPlayers()) {
            // 施加伤害
            player.hurt(context.level().damageSources().magic(), damage);

            // 施加虚弱效果
            player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                duration,
                amplifier,
                false,  // ambient
                true,   // visible
                true    // showIcon
            ));

            // 施加缓慢效果
            player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                duration,
                amplifier,
                false,
                true,
                true
            ));

            // 高转额外施加采矿疲劳
            if (tier >= MIN_FATIGUE_TIER) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    duration,
                    Math.min(tier - FATIGUE_TIER_OFFSET, MAX_FATIGUE_AMPLIFIER),
                    false,
                    true,
                    true
                ));
            }

            LOGGER.trace("辐射脉冲影响玩家: {}", player.getName().getString());
        }
    }
}
