package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 逃跑Goal - 当遇到危险且无法应对时逃跑
 * <p>
 * 使用标准动作: {@link MoveToAction}
 * <p>
 * 优先级: 极高（生存第一）
 * 触发条件: 血量 < 30% 且附近有威胁
 */
public class FleeGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        FleeGoal.class
    );

    private static final float DANGER_THRESHOLD = 0.3f; // 30%血量认为危险
    private static final double FLEE_DISTANCE = 20.0; // 逃跑距离
    private static final double THREAT_DETECTION_RANGE = 10.0; // 威胁检测范围
    private static final int FLEE_MEMORY_DURATION = 100; // 5秒
    private static final int FLEE_COOLDOWN_TICKS = 80; // 避免立即反复逃跑
    private static final int FLEE_LOCK_DURATION = 80; // 逃跑期间维持“危险”记忆，避免视野丢失导致提前停
    private static final float BASE_PRIORITY = 0.9f;
    private static final float PRIORITY_SCALE = 0.1f;
    private static final double FLEE_SPEED = 1.5;
    private static final double MIN_DIRECTION_LENGTH_SQR = 0.0001d;
    private static final double SAFE_PADDING = 4.0d;

    private MoveToAction fleeAction = null;
    private Vec3 safeLocation = null;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (isInDanger(mind, entity)) {
            float healthPercentage = entity.getHealth() / entity.getMaxHealth();
            // 血量越低，优先级越高（但总是保持高优先级）
            return BASE_PRIORITY + (1.0f - healthPercentage) * PRIORITY_SCALE;
        }
        return 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return !isOnCooldown(mind) && isInDanger(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        mind
            .getMemory()
            .rememberShortTerm("is_fleeing", true, FLEE_MEMORY_DURATION);
        mind
            .getMemory()
            .rememberShortTerm("flee_lock", true, FLEE_LOCK_DURATION);

        LOGGER.info(
            "[FleeGoal] {} 开始逃跑 | 血量: {}/{}",
            entity.getName().getString(),
            entity.getHealth(),
            entity.getMaxHealth()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 如果还没有逃跑动作或安全位置失效，重新计算
        if (fleeAction == null || safeLocation == null) {
            safeLocation = calculateSafeLocation(mind, entity);
            if (safeLocation != null) {
                fleeAction = new MoveToAction(safeLocation, FLEE_SPEED); // 快速移动
                fleeAction.start(mind, entity);
                LOGGER.debug(
                    "[FleeGoal] 目标安全位置: ({}, {}, {})",
                    safeLocation.x,
                    safeLocation.y,
                    safeLocation.z
                );
            }
        }

        // 执行逃跑动作
        if (fleeAction != null) {
            // 逃跑中保持“危险”记忆，避免短期记忆过期导致提前停
            refreshThreatMemory(mind, entity);

            ActionStatus status = fleeAction.tick(mind, entity);
            if (status == ActionStatus.SUCCESS) {
                LOGGER.info("[FleeGoal] 已到达安全位置");
                fleeAction = null;
                safeLocation = null;
                // 立即退出逃跑状态，进入短冷却，防止原地反复“到达安全位置”刷屏
                mind.getMemory().forget("is_fleeing");
                mind.getMemory().forget("flee_lock");
                mind
                    .getMemory()
                    .rememberShortTerm("flee_cooldown", true, FLEE_COOLDOWN_TICKS);
                clearThreatMemory(mind);
            } else if (status == ActionStatus.FAILURE) {
                LOGGER.warn("[FleeGoal] 逃跑失败，重新计算路径");
                fleeAction = null;
                safeLocation = null;
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("is_fleeing");
        mind
            .getMemory()
            .rememberShortTerm("flee_cooldown", true, FLEE_COOLDOWN_TICKS);
        mind.getMemory().forget("flee_lock");

        if (fleeAction != null) {
            fleeAction.stop(mind, entity);
            fleeAction = null;
        }
        safeLocation = null;

        // 逃跑结束时清理威胁相关短期记忆，防止遗留状态影响后续决策
        if (!isInDanger(mind, entity)) {
            clearThreatMemory(mind);
        }

        LOGGER.info("[FleeGoal] {} 停止逃跑", entity.getName().getString());
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 不再处于危险中或已到达安全位置
        return (
            !isInDanger(mind, entity) ||
            (safeLocation != null &&
                entity.position().distanceTo(safeLocation) < 2.0)
        );
    }

    @Override
    public String getName() {
        return "flee";
    }

    /**
     * 检查是否处于危险中
     */
    private boolean isInDanger(INpcMind mind, LivingEntity entity) {
        if (mind.getMemory().hasMemory("flee_lock")) {
            return true;
        }
        if (mind.getMemory().hasMemory("hazard_detected")) {
            return true;
        }

        // 血量低
        boolean lowHealth =
            entity.getHealth() < entity.getMaxHealth() * DANGER_THRESHOLD;

        // 检查Memory中是否有威胁记录
        boolean hasThreat = mind.getMemory().hasMemory("threat_detected");

        // 或者检查最近是否受到伤害
        boolean recentlyHurt = entity.hurtTime > 0;

        return lowHealth && (hasThreat || recentlyHurt);
    }

    private boolean isOnCooldown(INpcMind mind) {
        return mind.getMemory().hasMemory("flee_cooldown");
    }

    private void clearThreatMemory(INpcMind mind) {
        mind.getMemory().forget("threat_detected");
        mind.getMemory().forget("current_threat_id");
        mind.getMemory().forget("last_attacker");
        mind
            .getMemory()
            .forget(com.Kizunad.customNPCs.ai.WorldStateKeys.TARGET_VISIBLE);
        mind
            .getMemory()
            .forget(com.Kizunad.customNPCs.ai.WorldStateKeys.TARGET_IN_RANGE);
        mind
            .getMemory()
            .forget(
                com.Kizunad.customNPCs.ai.WorldStateKeys.DISTANCE_TO_TARGET
            );
        mind
            .getMemory()
            .forget(com.Kizunad.customNPCs.ai.WorldStateKeys.HOSTILE_NEARBY);
    }

    /**
     * 计算安全位置（远离当前位置）
     */
    private Vec3 calculateSafeLocation(INpcMind mind, LivingEntity entity) {
        Vec3 threatPos = resolveThreatPosition(mind, entity);
        if (threatPos != null) {
            Vec3 away = entity.position().subtract(threatPos);
            if (away.lengthSqr() > MIN_DIRECTION_LENGTH_SQR) {
                Vec3 normalized = away.normalize();
                return entity
                    .position()
                    .add(normalized.scale(FLEE_DISTANCE + SAFE_PADDING));
            }
        }

        Vec3 currentPos = entity.position();

        // 简单策略：随机选择一个远离当前位置的方向
        double angle = Math.random() * 2 * Math.PI;
        double dx = Math.cos(angle) * FLEE_DISTANCE;
        double dz = Math.sin(angle) * FLEE_DISTANCE;

        return new Vec3(currentPos.x + dx, currentPos.y, currentPos.z + dz);
    }

    private Vec3 resolveThreatPosition(INpcMind mind, LivingEntity entity) {
        Object hazardPos = mind.getMemory().getMemory("nearest_hazard_pos");
        if (hazardPos instanceof net.minecraft.core.BlockPos pos) {
            return Vec3.atCenterOf(pos);
        }

        Object threatId = mind.getMemory().getMemory("current_threat_id");
        UUID uuid = threatId instanceof UUID u ? u : null;
        if (uuid == null) {
            Object attacker = mind.getMemory().getMemory("last_attacker");
            if (attacker instanceof UUID a) {
                uuid = a;
            }
        }

        if (uuid != null && entity.level() instanceof ServerLevel serverLevel) {
            net.minecraft.world.entity.Entity threat = serverLevel.getEntity(
                uuid
            );
            if (threat != null) {
                return threat.position();
            }
        }

        return null;
    }

    /**
     * 逃跑过程中刷新威胁记忆，避免传感器短暂失联导致提前停下。
     */
    private void refreshThreatMemory(INpcMind mind, LivingEntity entity) {
        mind
            .getMemory()
            .rememberShortTerm("flee_lock", true, FLEE_LOCK_DURATION);
        // 如果仍能解析到威胁位置，刷新 threat_detected
        if (resolveThreatPosition(mind, entity) != null) {
            mind
                .getMemory()
                .rememberShortTerm("threat_detected", true, FLEE_MEMORY_DURATION);
        }
    }
}
