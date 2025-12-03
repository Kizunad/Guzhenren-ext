package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 拾取指定物品的 GOAP 目标。
 * <p>
 * 规划序列：MoveTo(物品) → PickUpItem。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class PickUpItemGoal extends PlanBasedGoal {

    public static final String LLM_USAGE_DESC =
        "PickUpItemGoal: GOAP move to a specific item entity and collect it; distance scales priority.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final double ARRIVE_DISTANCE = 2.0D;
    private static final double PRIORITY_FALLOFF_DISTANCE = 50.0D;
    private static final float MIN_PRIORITY_FACTOR = 0.5F;
    private static final float MAX_PRIORITY_FACTOR = 1.0F;
    private static final float PRIORITY_DISTANCE_SCALE = 0.5F;
    private static final float MOVE_ACTION_COST = 1.0F;

    private final ItemEntity targetItem;
    private final float basePriority;

    public PickUpItemGoal(ItemEntity targetItem, float basePriority) {
        this.targetItem = targetItem;
        this.basePriority = basePriority;
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (targetItem == null || !targetItem.isAlive()) {
            return 0.0F;
        }
        double distance = entity.position().distanceTo(targetItem.position());
        float distanceFactor = (float) Math.max(
            0.0D,
            MAX_PRIORITY_FACTOR - (distance / PRIORITY_FALLOFF_DISTANCE)
        );
        return basePriority *
            (MIN_PRIORITY_FACTOR + distanceFactor * PRIORITY_DISTANCE_SCALE);
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return targetItem != null && targetItem.isAlive();
    }

    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        WorldState desired = new WorldState();
        desired.setState("has_item", true);
        desired.setState("item_picked", true);
        return desired;
    }

    @Override
    protected WorldState getCurrentState(INpcMind mind, LivingEntity entity) {
        WorldState current = new WorldState();
        if (targetItem != null && targetItem.isAlive()) {
            current.setState("item_visible", true);
            Vec3 targetPos = targetItem.position();
            double distance = entity.position().distanceTo(targetPos);
            current.setState("at_item_location", distance <= ARRIVE_DISTANCE);
        } else {
            current.setState("item_visible", false);
            current.setState("at_item_location", false);
        }

        ItemStack held = entity.getMainHandItem();
        current.setState("has_item", !held.isEmpty());
        current.setState("item_picked", false);
        return current;
    }

    @Override
    public List<IGoapAction> getAvailableActions(
        INpcMind mind,
        LivingEntity entity
    ) {
        List<IGoapAction> actions = new ArrayList<>();
        if (targetItem != null) {
            actions.add(
                new com.Kizunad.customNPCs.ai.actions.goap.GoapMoveToAction(
                    targetItem,
                    "at_item_location",
                    MOVE_ACTION_COST
                )
            );
            actions.add(
                new com.Kizunad.customNPCs.ai.actions.goap.GoapPickUpItemAction(
                    targetItem
                )
            );
        }
        return actions;
    }

    @Override
    public String getName() {
        return "pick_up_item";
    }
}
