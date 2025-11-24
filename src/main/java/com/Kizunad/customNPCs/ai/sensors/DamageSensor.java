package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * 伤害传感器 - 感知受到的伤害和攻击者
 */
public class DamageSensor implements ISensor {

    private long lastHurtTime = -1;

    @Override
    public String getName() {
        return "damage_sensor";
    }

    @Override
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        // 检查是否受到伤害
        long currentHurtTime = entity.getLastHurtTimestamp();
        
        // 如果受伤时间更新了，说明受到了新的伤害
        if (currentHurtTime > lastHurtTime) {
            lastHurtTime = currentHurtTime;
            
            // 获取攻击者
            LivingEntity attacker = entity.getLastHurtByMob();
            if (attacker != null) {
                // 记录攻击者到记忆中
                mind.getMemory().rememberLongTerm("last_attacker", attacker);
                mind.getMemory().rememberShortTerm("under_attack", true, 200); // 10秒战斗状态
                
                // 更新世界状态（用于 GOAP）
                // 注意：这里假设 mind 有方法可以直接更新 WorldState，或者通过 Memory 间接更新
                // 目前我们主要通过 Memory，后续 NpcMind 会将 Memory 转换为 WorldState
                
                System.out.println("[DamageSensor] 感知到攻击者: " + attacker.getName().getString());
            }
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
