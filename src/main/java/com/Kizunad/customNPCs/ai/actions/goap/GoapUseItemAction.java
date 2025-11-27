package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.UseItemAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GOAP 使用物品动作 - 包装 UseItemAction 添加GOAP规划信息
 * <p>
 * 前置条件:
 * - has_item_<item_type>: true (拥有指定类型的物品)
 * - item_usable: true (物品可用)
 * <p>
 * 效果:
 * - item_used: true (物品已使用)
 * - hunger_restored: true (如果是食物，饥饿值恢复)
 * <p>
 * 代价: 1.5 (快速且安全的动作)
 */
public class GoapUseItemAction implements IGoapAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoapUseItemAction.class);

    private static final float DEFAULT_COST = 1.5f;
    private static final int HUNGER_EFFECT_DURATION_TICKS = 200;
    private static final int ITEM_USE_TIME_MEMORY_DURATION_TICKS = 100;

    private final WorldState preconditions;
    private final WorldState effects;
    private final UseItemAction wrappedAction;
    private final float cost;
    private final String itemType;
    private final boolean isFood;

    /**
     * 创建GOAP使用物品动作
     * @param targetItem 目标物品
     * @param itemType 物品类型名称（用于状态键）
     * @param isFood 是否是食物
     */
    public GoapUseItemAction(Item targetItem, String itemType, boolean isFood) {
        this(targetItem, itemType, isFood, DEFAULT_COST);
    }

    /**
     * 创建GOAP使用物品动作（指定代价）
     * @param targetItem 目标物品
     * @param itemType 物品类型名称
     * @param isFood 是否是食物
     * @param cost 动作代价
     */
    public GoapUseItemAction(Item targetItem, String itemType, boolean isFood, float cost) {
        this.wrappedAction = new UseItemAction(targetItem);
        this.cost = cost;
        this.itemType = itemType;
        this.isFood = isFood;

        // 前置条件：拥有物品且可用
        this.preconditions = new WorldState();
        this.preconditions.setState(WorldStateKeys.hasItem(itemType), true);
        this.preconditions.setState(WorldStateKeys.ITEM_USABLE, true);

        // 效果：物品已使用
        this.effects = new WorldState();
        this.effects.setState(WorldStateKeys.ITEM_USED, true);
        
        // 如果是食物，添加饥饿恢复效果
        if (isFood) {
            this.effects.setState(WorldStateKeys.HUNGER_RESTORED, true);
        }
    }

    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }

    @Override
    public WorldState getEffects() {
        return effects;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        ActionStatus status = wrappedAction.tick(mind, entity);

        // 成功时更新Memory状态
        if (status == ActionStatus.SUCCESS) {
            mind.getMemory().rememberLongTerm(WorldStateKeys.ITEM_USED, true);
            
            if (isFood) {
                mind.getMemory().rememberShortTerm(
                    WorldStateKeys.HUNGER_RESTORED,
                    true,
                    HUNGER_EFFECT_DURATION_TICKS
                );
            }
            
            mind.getMemory().rememberShortTerm(
                "last_item_use_time",
                System.currentTimeMillis(),
                ITEM_USE_TIME_MEMORY_DURATION_TICKS
            );

            LOGGER.info(
                "[GoapUseItemAction] 物品使用成功: {} | 是否食物: {}",
                itemType,
                isFood
            );
        }

        return status;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        wrappedAction.start(mind, entity);
        LOGGER.debug("[GoapUseItemAction] 开始使用物品: {}", itemType);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        wrappedAction.stop(mind, entity);
        LOGGER.debug("[GoapUseItemAction] 停止使用物品");
    }

    @Override
    public boolean canInterrupt() {
        return wrappedAction.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_use_item_" + itemType;
    }
}
