package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.memory.MemoryModule;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * 伤害传感器 - 感知受到的伤害和攻击者
 */
public class DamageSensor implements ISensor {

    private int lastHurtTime = 0; // 最后一次受伤时间
    private static final int MEMORY_DURATION = 120; // 记忆持续时间（单位：tick）
    private static final float CLOSE_COMBAT_RANGE = 5.0f; // 近战距离阈值
    private static final int CRITICAL_WINDOW_TICKS = 10; // 关键窗口（单位：tick）
    private static final int IMPORTANT_WINDOW_TICKS = 25; // 重要窗口（单位：tick）
    private static final int INFO_WINDOW_TICKS = 25; // 信息窗口（单位：tick）
    private final InterruptThrottle interruptThrottle = new InterruptThrottle(
        CRITICAL_WINDOW_TICKS,
        IMPORTANT_WINDOW_TICKS,
        INFO_WINDOW_TICKS
    );

    @Override
    public String getName() {
        return "damage_sensor";
    }

    @Override
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        // 检查是否受到伤害（hurtTime > 0 表示刚受伤）
        int hurtTime = entity.hurtTime;

        // 如果 hurtTime 大于之前记录的值，说明受到了新的伤害
        if (hurtTime > lastHurtTime && hurtTime > 0) {
            lastHurtTime = hurtTime;

            // 获取攻击者（玩家也属于 LivingEntity）
            LivingEntity attacker = entity.getLastHurtByMob();

            if (attacker != null) {
                // 记录攻击者到记忆中（使用 UUID 以便持久化）
                UUID attackerUuid = attacker.getUUID();

                mind
                    .getMemory()
                    .rememberLongTerm("last_attacker", attackerUuid);
                mind.getMemory().rememberShortTerm("under_attack", true, 200); // 10秒战斗状态

                double attackerDistance = entity.distanceTo(attacker);

                // 汇总威胁状态写入，避免内联重复逻辑
                rememberThreat(mind, attackerUuid, attackerDistance);

                // FUTURE:情绪需要单独集成
                // 触发情绪：怒（基础 +0.2）
                mind
                    .getPersonality()
                    .triggerEmotion(
                        com.Kizunad.customNPCs.ai.personality.EmotionType.ANGER,
                        0.2f
                    );

                // 如果攻击者很强（血量差距大），触发惧
                float healthRatio = entity.getHealth() / entity.getMaxHealth();
                float attackerHealthRatio =
                    attacker.getHealth() / attacker.getMaxHealth();

                if (attackerHealthRatio > healthRatio + 0.3f) {
                    // 对手明显更强，触发恐惧
                    mind
                        .getPersonality()
                        .triggerEmotion(
                            com.Kizunad.customNPCs.ai.personality.EmotionType.FEAR,
                            0.3f
                        );
                } else {
                    // 旗鼓相当或占优，减少恐惧（如果有的话）
                    mind
                        .getPersonality()
                        .triggerEmotion(
                            com.Kizunad.customNPCs.ai.personality.EmotionType.FEAR,
                            -0.1f
                        );
                }

                // 触发紧急中断，立即重新评估目标（带节流，防止同一攻击者抖动）
                if (
                    interruptThrottle.allowInterrupt(
                        attackerUuid,
                        SensorEventType.CRITICAL,
                        0,
                        level.getGameTime()
                    )
                ) {
                    mind.triggerInterrupt(entity, SensorEventType.CRITICAL);
                }

                MindLog.decision(
                    MindLogLevel.INFO,
                    "感知到攻击者: {}，触发情绪变化和 CRITICAL 中断",
                    attacker.getName().getString()
                );
            }
        } else if (hurtTime == 0) {
            // 重置计数器，准备检测下一次受伤
            lastHurtTime = 0;
        }
    }

    @Override
    public boolean shouldSense(long tickCount) {
        return true; // 每 tick 检查，保证反应及时
    }

    @Override
    public int getPriority() {
        return 100; // 高优先级，受伤反应通常是最紧急的
    }

    /**
     * 汇总威胁相关记忆写入，保持 CRITICAL 受击时的状态一致性。
     * @param mind 当前思维实例
     * @param threatId 攻击者 UUID
     * @param distance 到攻击者的距离
     */
    private void rememberThreat(INpcMind mind, UUID threatId, double distance) {
        MemoryModule memory = mind.getMemory();

        // 写入的记忆键：
        // DISTANCE_TO_TARGET, TARGET_IN_RANGE, TARGET_VISIBLE,
        // HOSTILE_NEARBY, IN_DANGER, THREAT_DETECTED, CURRENT_THREAT_ID
        memory.rememberShortTerm(
            WorldStateKeys.DISTANCE_TO_TARGET,
            distance,
            MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.TARGET_IN_RANGE,
            distance <= CLOSE_COMBAT_RANGE,
            MEMORY_DURATION
        );
        // 受击视为已暴露威胁：即便未直视，也将可见标记为 true 以强制决策抢占
        memory.rememberShortTerm(
            WorldStateKeys.TARGET_VISIBLE,
            true,
            MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.HOSTILE_NEARBY,
            true,
            MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.IN_DANGER,
            true,
            MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.THREAT_DETECTED,
            true,
            MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.CURRENT_THREAT_ID,
            threatId,
            MEMORY_DURATION
        );
    }
}
