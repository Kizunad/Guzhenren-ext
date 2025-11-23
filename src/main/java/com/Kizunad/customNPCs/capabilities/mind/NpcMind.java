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
public class NpcMind implements INpcMind, INBTSerializable<CompoundTag> {
    
    private final MemoryModule memory;
    private final UtilityGoalSelector goalSelector;
    private final com.Kizunad.customNPCs.ai.sensors.SensorManager sensorManager;
    private final com.Kizunad.customNPCs.ai.executor.ActionExecutor actionExecutor;
    
    public NpcMind() {
        this.memory = new MemoryModule();
        this.goalSelector = new UtilityGoalSelector();
        this.sensorManager = new com.Kizunad.customNPCs.ai.sensors.SensorManager();
        this.actionExecutor = new com.Kizunad.customNPCs.ai.executor.ActionExecutor();
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
    public void tick(ServerLevel level, LivingEntity entity) {
        // 1. 执行传感器（感知环境）
        sensorManager.tick(this, entity, level);
        
        // 2. 更新记忆（清理过期条目）
        memory.tick();
        
        // 3. 目标选择器执行
        goalSelector.tick(this, entity);
        
        // 4. 动作执行器执行
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
    public com.Kizunad.customNPCs.ai.planner.WorldState getCurrentWorldState(LivingEntity entity) {
        com.Kizunad.customNPCs.ai.planner.WorldState state = 
                new com.Kizunad.customNPCs.ai.planner.WorldState();
        
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

