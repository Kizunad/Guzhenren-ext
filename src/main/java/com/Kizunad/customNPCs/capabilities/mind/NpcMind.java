package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.UtilityGoalSelector;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.llm.LlmPlanner;
import com.Kizunad.customNPCs.ai.llm.LongTermMemory;
import com.Kizunad.customNPCs.ai.memory.MemoryModule;
import com.Kizunad.customNPCs.ai.interaction.NpcQuestState;
import com.Kizunad.customNPCs.ai.interaction.NpcTradeState;
import com.Kizunad.customNPCs.ai.status.NpcStatus;
import com.Kizunad.customNPCs.ai.util.ArmorEvaluationUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
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
    private final NpcInventory inventory;
    private final NpcStatus status;
    private final NpcTradeState tradeState;
    private final NpcQuestState questState;
    private final LongTermMemory longTermMemory;
    private final LlmPlanner llmPlanner;
    private final MaterialWallet materialWallet;

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
        this.inventory = new NpcInventory();
        this.status = new NpcStatus();
        this.tradeState = new NpcTradeState();
        this.questState = new NpcQuestState();
        this.longTermMemory = new LongTermMemory();
        this.llmPlanner = new LlmPlanner();
        this.materialWallet = new MaterialWallet();
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
        this.inventory = new NpcInventory();
        this.status = new NpcStatus();
        this.tradeState = new NpcTradeState();
        this.questState = new NpcQuestState();
        this.longTermMemory = new LongTermMemory();
        this.llmPlanner = new LlmPlanner();
        this.materialWallet = new MaterialWallet();
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
    public NpcInventory getInventory() {
        return inventory;
    }

    @Override
    public LongTermMemory getLongTermMemory() {
        return longTermMemory;
    }

    @Override
    public NpcStatus getStatus() {
        return status;
    }

    @Override
    public NpcTradeState getTradeState() {
        return tradeState;
    }

    @Override
    public NpcQuestState getQuestState() {
        return questState;
    }

    public LlmPlanner getLlmPlanner() {
        return llmPlanner;
    }

    public MaterialWallet getMaterialWallet() {
        return materialWallet;
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

        // 3.5 更新状态（饥饿/饱和/回血/掉血）
        status.tick(level, entity);

        // 3.6 周期性异步请求 LLM 计划（仅生成候选，不直接执行）
        llmPlanner.tick(level, entity, this);

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
        tag.put("inventory", inventory.serializeNBT(provider));
        tag.put("status", status.serializeNBT(provider));
        tag.put("trade", tradeState.serializeNBT(provider));
        tag.put("quest", questState.serializeNBT(provider));
        tag.put("llm_long_term", longTermMemory.serializeNBT());
        tag.put("llm", llmPlanner.serializeNBT(provider));
        tag.put("material_wallet", materialWallet.serializeNBT());
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
        if (nbt.contains("inventory")) {
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
        }
        if (nbt.contains("status")) {
            status.deserializeNBT(provider, nbt.getCompound("status"));
        }
        if (nbt.contains("trade")) {
            tradeState.deserializeNBT(provider, nbt.getCompound("trade"));
        }
        if (nbt.contains("quest")) {
            questState.deserializeNBT(provider, nbt.getCompound("quest"));
        }
        if (nbt.contains("llm_long_term")) {
            longTermMemory.deserializeNBT(nbt.getCompound("llm_long_term"));
        }
        if (nbt.contains("llm")) {
            llmPlanner.deserializeNBT(provider, nbt.getCompound("llm"));
        }
        if (nbt.contains("material_wallet")) {
            materialWallet.deserializeNBT(nbt.getCompound("material_wallet"));
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
        boolean hasFood = inventoryHasFood(entity);
        state.setState(WorldStateKeys.HAS_FOOD, hasFood);
        boolean hazardNearby =
            memory.hasMemory(WorldStateKeys.HAZARD_NEARBY) ||
            Boolean.TRUE.equals(memory.getMemory("hazard_detected"));
        Object hostileCount = memory.getMemory("hostile_entities_count");
        boolean hasHostile = hostileCount instanceof Integer count && count > 0;
        Object allyCount = memory.getMemory("ally_entities_count");
        boolean hasAlly = allyCount instanceof Integer count && count > 0;
        boolean hasThreat = memory.hasMemory("threat_detected") || hasHostile;
        boolean inDanger = hasThreat || hazardNearby;
        state.setState("has_threat", hasThreat);
        state.setState("is_safe", !inDanger);
        state.setState(WorldStateKeys.IN_DANGER, inDanger);
        state.setState(WorldStateKeys.HAZARD_NEARBY, hazardNearby);
        state.setState(WorldStateKeys.HOSTILE_NEARBY, hasHostile);
        state.setState(WorldStateKeys.ALLY_NEARBY, hasAlly);

        // 从记忆读取自定义状态（供 GOAP 动作使用）
        // 这些状态由 GOAP 动作在执行时写入记忆
        Object hasApple = memory.getMemory("has_apple");
        state.setState("has_apple", hasApple != null ? hasApple : false);
        Object hasWood = memory.getMemory("has_wood");
        state.setState("has_wood", hasWood != null ? hasWood : false);
        Object hasPlanks = memory.getMemory("has_planks");
        state.setState("has_planks", hasPlanks != null ? hasPlanks : false);

        populateTargetStates(state);

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

        Object hungerRestored = memory.getMemory(
            WorldStateKeys.HUNGER_RESTORED
        );
        state.setState(
            WorldStateKeys.HUNGER_RESTORED,
            hungerRestored != null ? hungerRestored : false
        );

        // 饥饿/饱和状态（来自 NpcStatus）
        state.setState(
            WorldStateKeys.HUNGER_PERCENT,
            status.getHungerPercent()
        );
        state.setState(WorldStateKeys.IS_HUNGRY, status.isHungry());
        state.setState(WorldStateKeys.HUNGER_CRITICAL, status.isCritical());

        // 方块相关状态
        Object blockExists = memory.getMemory(WorldStateKeys.BLOCK_EXISTS);
        state.setState(
            WorldStateKeys.BLOCK_EXISTS,
            blockExists != null ? blockExists : true // 默认为true
        );

        Object blockInteracted = memory.getMemory(
            WorldStateKeys.BLOCK_INTERACTED
        );
        state.setState(
            WorldStateKeys.BLOCK_INTERACTED,
            blockInteracted != null ? blockInteracted : false
        );

        Object doorOpen = memory.getMemory(WorldStateKeys.DOOR_OPEN);
        state.setState(
            WorldStateKeys.DOOR_OPEN,
            doorOpen != null ? doorOpen : false
        );

        // 装备/背包相关状态
        boolean armorUpgradeAvailable = ArmorEvaluationUtil.hasBetterArmor(
            inventory,
            entity
        );
        state.setState(
            WorldStateKeys.ARMOR_BETTER_AVAILABLE,
            armorUpgradeAvailable
        );
        state.setState(
            WorldStateKeys.ARMOR_SCORE,
            ArmorEvaluationUtil.totalEquippedScore(entity)
        );
        // 直接由当前装备是否需要升级判定是否已优化，避免记忆状态与实际脱节
        state.setState(WorldStateKeys.ARMOR_OPTIMIZED, !armorUpgradeAvailable);

        // 武器/防御能力
        state.setState(
            WorldStateKeys.HAS_RANGED_WEAPON,
            hasRangedWeapon(entity)
        );
        state.setState(WorldStateKeys.CAN_BLOCK, canBlock(entity));

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

    private boolean inventoryHasFood(LivingEntity entity) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getFoodProperties(entity) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将与目标实体相关的状态从 NPC 的记忆（memory）中填充到给定的世界状态（WorldState）对象中。
     *
     * 此方法用于 AI 规划器的目标世界状态初始化。具体填充以下状态键：
     * <ul>
     * <li>{@code WorldStateKeys.TARGET_VISIBLE}：目标实体当前对 NPC 是否可见，默认 {@code false}</li>
     * <li>{@code WorldStateKeys.TARGET_IN_RANGE}：目标实体是否在攻击/交互范围内，默认 {@code false}</li>
     * <li>{@code WorldStateKeys.TARGET_DAMAGED}：目标实体是否受伤/低血量，默认 {@code false}</li>
     * <li>{@code WorldStateKeys.DISTANCE_TO_TARGET}：到目标实体的当前距离（double 值），默认 {@code -1.0d}</li>
     * <li>{@code WorldStateKeys.ATTACK_COOLDOWN_ACTIVE}：攻击冷却是否当前激活，默认 {@code false}</li>
     * </ul>
     *
     * @param state 要填充的世界状态对象
     */
    private void populateTargetStates(
        com.Kizunad.customNPCs.ai.planner.WorldState state
    ) {
        // 获取目标实体当前对 NPC 是否可见
        Object targetVisible = memory.getMemory(WorldStateKeys.TARGET_VISIBLE);
        state.setState(
            WorldStateKeys.TARGET_VISIBLE,
            targetVisible != null ? targetVisible : false
        );

        // 获取目标实体是否在攻击/交互范围内
        Object targetInRange = memory.getMemory(WorldStateKeys.TARGET_IN_RANGE);
        state.setState(
            WorldStateKeys.TARGET_IN_RANGE,
            targetInRange != null ? targetInRange : false
        );

        // 获取目标实体是否受伤/低血量
        Object targetDamaged = memory.getMemory(WorldStateKeys.TARGET_DAMAGED);
        state.setState(
            WorldStateKeys.TARGET_DAMAGED,
            targetDamaged != null ? targetDamaged : false
        );

        // 获取到目标实体的当前距离（double 值）
        Object targetDistance = memory.getMemory(
            WorldStateKeys.DISTANCE_TO_TARGET
        );
        state.setState(
            WorldStateKeys.DISTANCE_TO_TARGET,
            targetDistance != null ? targetDistance : -1.0d
        );

        // 获取攻击冷却是否当前激活
        Object attackCooldown = memory.getMemory(
            WorldStateKeys.ATTACK_COOLDOWN_ACTIVE
        );
        state.setState(
            WorldStateKeys.ATTACK_COOLDOWN_ACTIVE,
            attackCooldown != null ? attackCooldown : false
        );
    }

    public void setLastInterruptTick(long lastInterruptTick) {
        this.lastInterruptTick = lastInterruptTick;
    }

    public void setLastInterruptType(
        com.Kizunad.customNPCs.ai.sensors.SensorEventType lastInterruptType
    ) {
        this.lastInterruptType = lastInterruptType;
    }

    private boolean hasRangedWeapon(LivingEntity entity) {
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();
        return (
            main.getItem() instanceof ProjectileWeaponItem ||
            off.getItem() instanceof ProjectileWeaponItem
        );
    }

    private boolean canBlock(LivingEntity entity) {
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();
        return main.is(Items.SHIELD) || off.is(Items.SHIELD);
    }
}
