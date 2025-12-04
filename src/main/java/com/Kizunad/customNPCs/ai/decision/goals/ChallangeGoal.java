package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.actions.util.NavigationUtil;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

/**
 * 挑战周围较弱的敌对实体，鼓励战斗活跃。
 * 条件：自身血量 >95%，目标最大生命值不超过自身的80%，可到达且不在空中/水中/洞穴。
 */
public class ChallangeGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "ChallangeGoal: when very healthy (>95%), seek nearby reachable hostile targets whose max HP <= 80% of self, " +
        "skip targets in air/water/caves, then engage with melee.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.38F; // 提高优先级，高于 Hunt 以主动挑战
    private static final double LOW_VALUE_HP_RATIO = 0.08D; // 低价值目标阈值
    private static final float LOW_VALUE_PRIORITY_FACTOR = 0.35F; // 低价值优先级折减
    private static final double READY_HEALTH_RATIO = 0.95D;
    private static final double TARGET_HEALTH_RATIO = 0.8D;
    private static final double SEARCH_RADIUS = 12.0D;
    private static final int CAVE_LIGHT_THRESHOLD = 3;

    private AttackAction attackAction;
    private UUID targetUuid;
    private boolean finished;

    @Override
    public String getName() {
        return "challange";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!canEngage(mind, entity)) {
            return 0.0F;
        }
        LivingEntity target = ensureTarget(entity);
        if (target == null) {
            return 0.0F;
        }
        float priority = PRIORITY;
        if (isLowValueTarget(target)) {
            priority *= LOW_VALUE_PRIORITY_FACTOR;
        }
        return priority;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return getPriority(mind, entity) > 0.0F;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        finished = false;
        if (!(entity instanceof Mob)) {
            finished = true;
            return;
        }
        LivingEntity target = ensureTarget(entity);
        if (target == null) {
            finished = true;
            return;
        }
        targetUuid = target.getUUID();
        attackAction = new AttackAction(targetUuid);
        attackAction.start(mind, entity);
        MindLog.decision(
            MindLogLevel.INFO,
            "ChallangeGoal 开始挑战 {}",
            target.getName().getString()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof Mob)) {
            finished = true;
            return;
        }
        if (!canEngage(mind, entity)) {
            finished = true;
            stop(mind, entity);
            return;
        }

        LivingEntity target = ensureTarget(entity);
        if (target == null) {
            finished = true;
            stop(mind, entity);
            return;
        }

        if (
            attackAction == null ||
            targetUuid == null ||
            !targetUuid.equals(target.getUUID())
        ) {
            if (attackAction != null) {
                attackAction.stop(mind, entity);
            }
            targetUuid = target.getUUID();
            attackAction = new AttackAction(targetUuid);
            attackAction.start(mind, entity);
            MindLog.decision(
                MindLogLevel.INFO,
                "ChallangeGoal 切换目标为 {}",
                target.getName().getString()
            );
        }

        ActionStatus status = attackAction.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            if (!target.isAlive()) {
                finished = true;
            } else {
                // 命中后继续追击，重置动作便于下次 swing
                attackAction.stop(mind, entity);
                attackAction = null;
            }
        } else if (status == ActionStatus.FAILURE) {
            stop(mind, entity);
            finished = true;
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        if (attackAction != null) {
            attackAction.stop(mind, entity);
        }
        attackAction = null;
        targetUuid = null;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return finished || !canEngage(mind, entity);
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof Mob)) {
            return false;
        }
        if (entity.level().isClientSide()) {
            return false;
        }
        if (entity.isDeadOrDying()) {
            return false;
        }
        double healthRatio = entity.getHealth() / entity.getMaxHealth();
        if (healthRatio < READY_HEALTH_RATIO) {
            return false;
        }
        return (
            !mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER) &&
            !mind.getMemory().hasMemory("is_fleeing") &&
            !mind.getMemory().hasMemory("threat_detected")
        );
    }

    private LivingEntity ensureTarget(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return null;
        }
        LivingEntity resolved = resolveTarget(mob);
        if (resolved != null && isCandidate(mob, resolved)) {
            return resolved;
        }
        LivingEntity selected = selectTarget(mob);
        targetUuid = selected == null ? null : selected.getUUID();
        return selected;
    }

    private LivingEntity resolveTarget(Mob mob) {
        if (
            targetUuid == null ||
            !(mob.level() instanceof ServerLevel serverLevel)
        ) {
            return null;
        }
        var entity = serverLevel.getEntity(targetUuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    private LivingEntity selectTarget(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return null;
        }
        AABB box = mob.getBoundingBox().inflate(SEARCH_RADIUS);
        return level
            .getEntitiesOfClass(LivingEntity.class, box, target ->
                isCandidate(mob, target)
            )
            .stream()
            .min(Comparator.comparingDouble(mob::distanceToSqr))
            .orElse(null);
    }

    /**
     * 过滤掉不可挑战的目标：友方/非敌对、过强、不可达、在水中/空中/洞穴或创造光环。
     */
    private boolean isCandidate(Mob actor, LivingEntity target) {
        if (actor == target || !target.isAlive()) {
            return false;
        }
        if (EntityRelationUtil.isAlly(actor, target)) {
            return false;
        }
        if (!EntityRelationUtil.isHostileTo(actor, target)) {
            return false;
        }
        if (
            target instanceof Player player &&
            (player.isCreative() || player.isSpectator())
        ) {
            return false;
        }
        if (!isWeaker(actor, target)) {
            return false;
        }
        if (!(actor.level() instanceof ServerLevel level)) {
            return false;
        }
        if (target.isInWaterOrBubble()) {
            return false;
        }
        if (!hasGroundSupport(level, target)) {
            return false;
        }
        if (isDeepCave(level, target)) {
            return false;
        }
        return NavigationUtil.canReachEntity(
            actor,
            target,
            SEARCH_RADIUS + 1.0D
        );
    }

    private boolean isWeaker(LivingEntity actor, LivingEntity target) {
        double actorHp = actor.getMaxHealth();
        double targetHp = target.getMaxHealth();
        return targetHp > 0 && targetHp <= actorHp * TARGET_HEALTH_RATIO;
    }

    private boolean isLowValueTarget(LivingEntity target) {
        double max = target.getMaxHealth();
        if (max <= 0.0D) {
            return false;
        }
        double ratio = target.getHealth() / max;
        return ratio <= LOW_VALUE_HP_RATIO;
    }

    private boolean hasGroundSupport(ServerLevel level, LivingEntity target) {
        BlockPos feet = target.blockPosition();
        BlockPos below = feet.below();
        if (!level.hasChunkAt(below)) {
            return false;
        }
        if (!level.getFluidState(feet).isEmpty()) {
            return false;
        }
        return level
            .getBlockState(below)
            .isFaceSturdy(level, below, Direction.UP);
    }

    /** 洞穴判定：无天空且环境昏暗，避免钻入难以到达的洞穴/封闭空间。 */
    private boolean isDeepCave(ServerLevel level, LivingEntity target) {
        BlockPos pos = target.blockPosition();
        if (level.canSeeSkyFromBelowWater(pos)) {
            return false;
        }
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        // 无天空且亮度极低，视为洞穴或封闭区域
        return skyLight <= 0 && blockLight <= CAVE_LIGHT_THRESHOLD;
    }
}
