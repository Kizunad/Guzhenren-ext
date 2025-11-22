package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * 传感器接口 - NPC 感知世界的方式
 * <p>
 * 传感器负责收集环境信息并更新 NPC 的记忆。
 * 不同的传感器类型可以提供不同的感知能力（视觉、听觉、危险感知等）
 */
public interface ISensor {
    
    /**
     * 获取传感器名称
     * @return 传感器标识符
     */
    String getName();
    
    /**
     * 执行感知
     * <p>
     * 此方法每个 tick 调用，应该：
     * 1. 扫描环境
     * 2. 识别重要信息
     * 3. 更新 NPC 的记忆
     * 
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @param level 服务器世界
     */
    void sense(INpcMind mind, LivingEntity entity, ServerLevel level);
    
    /**
     * 传感器是否应该在此 tick 激活
     * <p>
     * 可用于优化性能（例如：视觉传感器每 5 ticks 扫描一次）
     * 
     * @param tickCount 当前 tick 计数
     * @return 是否应该执行 sense()
     */
    default boolean shouldSense(long tickCount) {
        return true; // 默认每 tick 都感知
    }
    
    /**
     * 获取传感器优先级
     * <p>
     * 优先级高的传感器先执行
     * 
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 0;
    }
}
