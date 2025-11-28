package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.UtilityGoalSelector;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
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

    // 中断冷却机制
    private long lastInterruptTick = 0;
    private com.Kizunad.customNPCs.ai.sensors.SensorEventType lastInterruptType =
        null;
    private static final int INTERRUPT_COOLDOWN_TICKS = 10; // 0.5秒冷却 (10 ticks)

    public NpcMind() {
        this.memory = new MemoryModule();
        this.goalSelector = new UtilityGoalSelector();
        this.sensorManager =
            new com.Kizunad.customNPCs.ai.sensors.SensorManager();
        this.actionExecutor =
            new com.Kizunad.customNPCs.ai.executor.ActionExecutor();
        this.personality =
            new com.Kizunad.customNPCs.ai.personality.PersonalityModule();
    }

    /**
     * 创建带有自定义性格的 NpcMind（用于测试）
     * @param customPersonality 自定义性格模块
     */
    public NpcMind(
        com.Kizunad.customNPCs.ai.personality.PersonalityModule customPersonality
    ) {
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
        com.Kizunad.customNPCs.ai.sensors.SensorManager sensorManager
    ) {
        this(
            actionExecutor,
            sensorManager,
            new com.Kizunad.customNPCs.ai.personality.PersonalityModule()
        );
    }

    /**
     * 全参数构造函数
     */
    public NpcMind(
        com.Kizunad.customNPCs.ai.executor.ActionExecutor actionExecutor,
        com.Kizunad.customNPCs.ai.sensors.SensorManager sensorManager,
        com.Kizunad.customNPCs.ai.personality.PersonalityModule personality
    ) {
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
        tag.put("personality", personality.serializeNBT());
        // 注意：目标选择器不需要序列化，因为目标是在代码中注册的
        return tag;
    }

    @Override
    public void deserializeNBT(
        HolderLookup.Provider provider,
        CompoundTag nbt
    ) {
        if (nbt.contains("memory")) {
            memory.deserializeNBT(nbt.getCompound("memory"));
        }
        if (nbt.contains("personality")) {
            personality.deserializeNBT(nbt.getCompound("personality"));
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
    public com.Kizunad.customNPCs.ai.planner.WorldState getCurrentWorldState(
        LivingEntity entity
    ) {
        com.Kizunad.customNPCs.ai.planner.WorldState state =
            new com.Kizunad.customNPCs.ai.planner.WorldState();

        // FUTURE: 这里需要建立对应的工具调用而不是直接在此处赋予状态
        // 从实体读取状态
        state.setState(
            "health_low",
            entity.getHealth() < entity.getMaxHealth() * 0.3f
        );
        state.setState(
            "health_critical",
            entity.getHealth() < entity.getMaxHealth() * 0.1f
        );

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

        // === 新增: 标准动作相关状态键 ===
        // 目标相关状态
        Object targetVisible = memory.getMemory(WorldStateKeys.TARGET_VISIBLE);
        state.setState(
            WorldStateKeys.TARGET_VISIBLE,
            targetVisible != null ? targetVisible : false
        );
        
        Object targetInRange = memory.getMemory(WorldStateKeys.TARGET_IN_RANGE);
        state.setState(
            WorldStateKeys.TARGET_IN_RANGE,
            targetInRange != null ? targetInRange : false
        );
        
        Object targetDamaged = memory.getMemory(WorldStateKeys.TARGET_DAMAGED);
        state.setState(
            WorldStateKeys.TARGET_DAMAGED,
            targetDamaged != null ? targetDamaged : false
        );
        
        Object attackCooldown = memory.getMemory(WorldStateKeys.ATTACK_COOLDOWN_ACTIVE);
        state.setState(
            WorldStateKeys.ATTACK_COOLDOWN_ACTIVE,
            attackCooldown != null ? attackCooldown : false
        );

        // 物品相关状态
        Object itemUsable = memory.getMemory(WorldStateKeys.ITEM_USABLE);
        state.setState(
            WorldStateKeys.ITEM_USABLE,
            itemUsable != null ? itemUsable : true // 默认为true
        );
        
        Object itemUsed = memory.getMemory(WorldStateKeys.ITEM_USED);
        state.setState(
            WorldStateKeys.ITEM_USED,
            itemUsed != null ? itemUsed : false
        );
        
        Object hungerRestored = memory.getMemory(WorldStateKeys.HUNGER_RESTORED);
        state.setState(
            WorldStateKeys.HUNGER_RESTORED,
            hungerRestored != null ? hungerRestored : false
        );

        // 方块相关状态
        Object blockExists = memory.getMemory(WorldStateKeys.BLOCK_EXISTS);
        state.setState(
            WorldStateKeys.BLOCK_EXISTS,
            blockExists != null ? blockExists : true // 默认为true
        );
        
        Object blockInteracted = memory.getMemory(WorldStateKeys.BLOCK_INTERACTED);
        state.setState(
            WorldStateKeys.BLOCK_INTERACTED,
            blockInteracted != null ? blockInteracted : false
        );
        
        Object doorOpen = memory.getMemory(WorldStateKeys.DOOR_OPEN);
        state.setState(
            WorldStateKeys.DOOR_OPEN,
            doorOpen != null ? doorOpen : false
        );

        return state;
    }

    @Override
    public void triggerInterrupt(
        LivingEntity entity,
        com.Kizunad.customNPCs.ai.sensors.SensorEventType eventType
    ) {
        // 获取当前世界 tick (使用 entity 所在的 level)
        long currentTick = entity.level().getGameTime();

        // 冷却检查:
        // 同一类型事件在冷却期内不重复触发
        if (
            lastInterruptType == eventType &&
            currentTick - lastInterruptTick < INTERRUPT_COOLDOWN_TICKS
        ) {
            return;
        }

        // 记录中断
        lastInterruptTick = currentTick;
        lastInterruptType = eventType;

        MindLog.decision(
            MindLogLevel.INFO,
            "{} 触发中断: {} (tick: {})",
            entity.getName().getString(),
            eventType,
            currentTick
        );

        // 强制重新评估目标
        goalSelector.forceReevaluate(this, entity, eventType);
    }
}
