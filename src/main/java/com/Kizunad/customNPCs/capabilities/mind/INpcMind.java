package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.ai.decision.UtilityGoalSelector;
import com.Kizunad.customNPCs.ai.memory.MemoryModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * NPC 思维能力接口 - 定义 NPC 的心智结构
 * <p>
 * 此接口代表 NPC 的"大脑"，包含：
 * - 记忆系统（短期/长期记忆）
 * - 决策系统（目标选择器）
 * - 思考循环（tick 方法）
 */
public interface INpcMind {
    
    /**
     * 获取记忆模块
     * @return NPC 的记忆系统
     */
    MemoryModule getMemory();
    
    /**
     * 获取目标选择器
     * @return Utility AI 目标选择器
     */
    UtilityGoalSelector getGoalSelector();
    
    /**
     * 获取传感器管理器
     * @return 传感器管理器
     */
    com.Kizunad.customNPCs.ai.sensors.SensorManager getSensorManager();
    
    /**
     * 获取动作执行器
     * @return 动作执行器
     */
    com.Kizunad.customNPCs.ai.executor.ActionExecutor getActionExecutor();
    
    /**
     * 思考循环 - 每个游戏 tick 调用
     * <p>
     * 此方法执行：
     * 1. 执行传感器（感知环境）
     * 2. 更新记忆（清理过期条目）
     * 3. 重新评估目标优先级
     * 4. 执行当前目标
     * 
     * @param level 服务器世界
     * @param entity NPC 实体
     */
    void tick(ServerLevel level, LivingEntity entity);
    
    /**
     * 序列化到 NBT
     * @param provider Holder lookup provider
     * @return NBT 数据
     */
    CompoundTag serializeNBT(HolderLookup.Provider provider);
    
    /**
     * 从 NBT 反序列化
     * @param provider Holder lookup provider
     * @param nbt NBT 数据
     */
    void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt);
}
