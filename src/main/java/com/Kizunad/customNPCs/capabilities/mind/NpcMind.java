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
    
    public NpcMind() {
        this.memory = new MemoryModule();
        this.goalSelector = new UtilityGoalSelector();
        this.sensorManager = new com.Kizunad.customNPCs.ai.sensors.SensorManager();
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
    public void tick(ServerLevel level, LivingEntity entity) {
        // 1. 执行传感器（感知环境）
        sensorManager.tick(this, entity, level);
        
        // 2. 更新记忆（清理过期条目）
        memory.tick();
        
        // 3. 目标选择器执行
        goalSelector.tick(this, entity);
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
}
