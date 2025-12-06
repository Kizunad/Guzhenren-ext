package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.goap.GoapMountAction;
import com.Kizunad.customNPCs.ai.actions.goap.GoapMoveToAction;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 自主寻找坐骑的目标：使用 GOAP 规划靠近并骑乘附近的载具。
 */
public class FindMountGoal extends PlanBasedGoal {

    public static final String LLM_USAGE_DESC =
        "FindMountGoal: acquire nearest free mount, move to it, and ride when travel is needed.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float BASE_PRIORITY = 0.45F;
    private static final float DISTANCE_PRIORITY_BOOST = 0.25F;
    private static final double LONG_TRAVEL_THRESHOLD = 30.0D;
    private static final String MOUNT_APPROACH_STATE = "at_mount_location";
    private static final float MOVE_COST_DISTANCE_SCALE = 16.0F;
    private static final float MOUNT_ACTION_COST = 1.5F;

    @Override
    public String getName() {
        return "find_mount";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!shouldAttempt(mind, entity)) {
            return 0.0F;
        }
        Entity mount = resolveMount(entity, resolveMountUuid(mind));
        if (mount == null) {
            return 0.0F;
        }
        float priority = BASE_PRIORITY;
        double distance = entity.distanceTo(mount);
        if (distance >= LONG_TRAVEL_THRESHOLD) {
            priority += DISTANCE_PRIORITY_BOOST;
        }
        return priority;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return shouldAttempt(mind, entity);
    }

    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        WorldState desired = new WorldState();
        desired.setState(WorldStateKeys.IS_RIDING, true);
        UUID mountId = resolveMountUuid(mind);
        if (mountId != null) {
            desired.setState(WorldStateKeys.MOUNT_UUID, mountId);
        }
        return desired;
    }

    @Override
    public List<IGoapAction> getAvailableActions(
        INpcMind mind,
        LivingEntity entity
    ) {
        UUID mountId = resolveMountUuid(mind);
        Entity mount = resolveMount(entity, mountId);
        if (mount == null) {
            return Collections.emptyList();
        }
        double distance = entity.distanceTo(mount);
        float moveCost = 1.0F + (float) (distance / MOVE_COST_DISTANCE_SCALE);

        List<IGoapAction> actions = new ArrayList<>();
        actions.add(new GoapMoveToAction(mount, MOUNT_APPROACH_STATE, moveCost));
        actions.add(new GoapMountAction(mountId, MOUNT_APPROACH_STATE, MOUNT_ACTION_COST));
        return actions;
    }

    private boolean shouldAttempt(INpcMind mind, LivingEntity entity) {
        if (entity.isPassenger()) {
            return false;
        }
        WorldState current = mind.getCurrentWorldState(entity);
        Object hasMountNearby = current.getState(WorldStateKeys.HAS_MOUNT_NEARBY);
        if (!Boolean.TRUE.equals(hasMountNearby)) {
            return false;
        }
        UUID mountId = resolveMountUuid(mind);
        return mountId != null && resolveMount(entity, mountId) != null;
    }

    private UUID resolveMountUuid(INpcMind mind) {
        return mind.getMemory().getMemory(WorldStateKeys.MOUNT_UUID, UUID.class);
    }

    private Entity resolveMount(LivingEntity entity, UUID mountId) {
        if (mountId == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity mount = serverLevel.getEntity(mountId);
        if (mount == null || !mount.isAlive() || !mount.getPassengers().isEmpty()) {
            return null;
        }
        return mount;
    }
}
