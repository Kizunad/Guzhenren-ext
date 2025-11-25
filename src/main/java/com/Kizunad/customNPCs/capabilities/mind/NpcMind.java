package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.ai.decision.UtilityGoalSelector;
import com.Kizunad.customNPCs.ai.memory.MemoryModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * NpcMind 默认实现
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class NpcMind implements INpcMind, INBTSerializable<CompoundTag> {
    
    private final MemoryModule memory;
    private final UtilityGoalSelector goalSelector;
    private final com.Kizunad.customNPCs.ai.sensors.SensorManager sensorManager;
    private final com.Kizunad.customNPCs.ai.executor.ActionExecutor actionExecutor;
    private final com.Kizunad.customNPCs.ai.personality.PersonalityModule personality;
    
    public NpcMind() {
        this.memory = new MemoryModule();
        this.goalSelector = new UtilityGoalSelector();
        this.sensorManager = new com.Kizunad.customNPCs.ai.sensors.SensorManager();
        this.actionExecutor = new com.Kizunad.customNPCs.ai.executor.ActionExecutor();
        this.personality = new com.Kizunad.customNPCs.ai.personality.PersonalityModule();
    }
    
    /**
     * 创建带有自定义性格的 NpcMind（用于测试）
     * @param customPersonality 自定义性格模块
     */
    public NpcMind(com.Kizunad.customNPCs.ai.personality.PersonalityModule customPersonality) {
        this(
            new com.Kizunad.customNPCs.ai.executor.ActionExecutor(),
            new com.Kizunad.customNPCs.ai.sensors.SensorManager(),
            customPersonality
        );
    }

    /**
     * 创建带有自定义组件的 NpcMind（用于测试）
     * @param actionExecutor 自定义动作执行器
     * @param sensorManager 自定义传感器管理器
     */
    public NpcMind(
            com.Kizunad.customNPCs.ai.executor.ActionExecutor actionExecutor,
            com.Kizunad.customNPCs.ai.sensors.SensorManager sensorManager) {
        this(actionExecutor, sensorManager, new com.Kizunad.customNPCs.ai.personality.PersonalityModule());
    }

    /**
     * 全参数构造函数
     */
    public NpcMind(
            com.Kizunad.customNPCs.ai.executor.ActionExecutor actionExecutor,
            com.Kizunad.customNPCs.ai.sensors.SensorManager sensorManager,
            com.Kizunad.customNPCs.ai.personality.PersonalityModule personality) {
        this.memory = new MemoryModule();
        this.goalSelector = new UtilityGoalSelector();
        this.sensorManager = sensorManager;
        this.actionExecutor = actionExecutor;
        this.personality = personality;
    }
    
    @Override
    public MemoryModule getMemory() {
        return memory;
    }
    
    @Override
    public UtilityGoalSelector getGoalSelector() {
        return goalSelector;
    }
    
    @Override
    public com.Kizunad.customNPCs.ai.sensors.SensorManager getSensorManager() {
        return sensorManager;
    }
    
    @Override
    public com.Kizunad.customNPCs.ai.executor.ActionExecutor getActionExecutor() {
        return actionExecutor;
    }
    
    @Override
    public com.Kizunad.customNPCs.ai.personality.PersonalityModule getPersonality() {
        return personality;
    }
    
    @Override
    public void tick(ServerLevel level, LivingEntity entity) {
        // 绑定执行器上下文，防止跨实体/测试计划污染
        actionExecutor.bindToEntity(entity);
        
        // 1. 执行传感器（感知环境）
        sensorManager.tick(this, entity, level);
        
        // 2. 更新记忆（清理过期条目）
        memory.tick();
        
        // 3. 更新性格（情绪衰减）
        personality.tick();
        
        // 4. 目标选择器执行
        goalSelector.tick(this, entity);
        
        // 5. 动作执行器执行
        actionExecutor.tick(this, entity);
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("memory", memory.serializeNBT());
        // 注意：目标选择器不需要序列化，因为目标是在代码中注册的
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("memory")) {
            memory.deserializeNBT(nbt.getCompound("memory"));
        }
    }
    
    @Override
    /*
     * NPC 的"状态快照",用于告知规划器 NPC 当前所处状况,
     * 从而使规划器能够计算出如何达到目标状态。
     *
     * 工作流程:
     * 1. 目标激活: 通过 canRun() 检查当前状态是否满足目标。
     * 2. 目标启动: start() 方法获取当前状态。
     * 3. 规划器使用当前状态和目标状态,生成动作序列。
     * 4. 动作执行器执行动作,改变世界状态。
     * 5. 下次 tick 时重新获取当前状态,形成循环。
     */
    public com.Kizunad.customNPCs.ai.planner.WorldState getCurrentWorldState(LivingEntity entity) {
        com.Kizunad.customNPCs.ai.planner.WorldState state = 
                new com.Kizunad.customNPCs.ai.planner.WorldState();
        

        // Note: 这里需要建立对应的工具调用而不是直接在此处赋予状态
        // 从实体读取状态
        state.setState("health_low", entity.getHealth() < entity.getMaxHealth() * 0.3f);
        state.setState("health_critical", entity.getHealth() < entity.getMaxHealth() * 0.1f);
        
        // 从记忆读取状态
        Object hasFood = memory.getMemory("has_food");
        state.setState("has_food", hasFood != null ? hasFood : false);
        state.setState("has_threat", memory.hasMemory("threat_detected"));
        state.setState("is_safe", !memory.hasMemory("threat_detected"));
        
        // 从记忆读取自定义状态（供 GOAP 动作使用）
        // 这些状态由 GOAP 动作在执行时写入记忆
        Object hasApple = memory.getMemory("has_apple");
        state.setState("has_apple", hasApple != null ? hasApple : false);
        Object hasWood = memory.getMemory("has_wood");
        state.setState("has_wood", hasWood != null ? hasWood : false);
        Object hasPlanks = memory.getMemory("has_planks");
        state.setState("has_planks", hasPlanks != null ? hasPlanks : false);

        
        return state;
    }
}
