package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * 伤害传感器 - 感知受到的伤害和攻击者
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class DamageSensor implements ISensor {

    private int lastHurtTime = 0;
    private static final int CRITICAL_WINDOW_TICKS = 10;
    private static final int IMPORTANT_WINDOW_TICKS = 25;
    private static final int INFO_WINDOW_TICKS = 25;
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

            // 获取攻击者
            LivingEntity attacker = entity.getLastHurtByMob();
            if (attacker != null) {
                // 记录攻击者到记忆中（使用 UUID 以便持久化）
                UUID attackerUuid = attacker.getUUID();
                mind.getMemory().rememberLongTerm("last_attacker", attackerUuid);
                mind.getMemory().rememberShortTerm("under_attack", true, 200); // 10秒战斗状态

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

                // 触发紧急中断,立即重新评估目标（带节流，防止同一攻击者抖动）
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
}
