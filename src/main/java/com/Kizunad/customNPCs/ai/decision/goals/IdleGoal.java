package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.decision.UtilityGoalSelector;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.sensors.SensorEventType;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 闲置目标 - 默认目标，当没有其他事情做时执行
 * <p>
 * 此目标的优先级总是很低，作为保底目标
 */
public class IdleGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "IdleGoal: lowest-priority fallback, periodically teleport to a random surface spot then re-evaluate goals.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float IDLE_PRIORITY = 0.1f;
    private static final int IDLE_TICK_INTERVAL = 100; // 每 5 秒打印一次（100 ticks）
    private static final int TELEPORT_INTERVAL = 40; // 传送尝试间隔
    private static final double TELEPORT_MIN_DISTANCE = 8.0D; // 最小水平距离
    private static final double TELEPORT_MAX_DISTANCE = 36.0D; // 最大水平距离
    private static final int TELEPORT_ATTEMPTS = 8;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final int MAX_IDLE_TICKS = 20 * 30; // 最长闲置 30s 后强制重评估
    private static final double TWO_PI = Math.PI * 2;

    private int idleTicks;
    private int teleportCooldown;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 最低优先级，只有当没有其他目标时才会被选中
        return IDLE_PRIORITY;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 总是可以运行
        return true;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        idleTicks = 0;
        teleportCooldown = 0;
        mind.getMemory().rememberShortTerm("is_idle", true, -1);
        MindLog.decision(
            MindLogLevel.INFO,
            "NPC {} 开始闲逛",
            entity.getName().getString()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        idleTicks++;

        // 若执行器空闲，周期性随机传送到地表位置并触发重评估
        if (mind.getActionExecutor().isIdle()) {
            teleportCooldown--;
            if (teleportCooldown <= 0) {
                BlockPos target = pickTeleportTarget(entity);
                teleportCooldown = TELEPORT_INTERVAL;

                if (target != null) {
                    entity.teleportTo(
                        target.getX() + BLOCK_CENTER_OFFSET,
                        target.getY(),
                        target.getZ() + BLOCK_CENTER_OFFSET
                    );
                    MindLog.decision(
                        MindLogLevel.INFO,
                        "NPC {} 闲置随机传送到 ({}, {}, {})，触发重评估",
                        entity.getName().getString(),
                        target.getX(),
                        target.getY(),
                        target.getZ()
                    );
                    idleTicks = 0;
                    mind
                        .getGoalSelector()
                        .forceReevaluate(
                            mind,
                            entity,
                            SensorEventType.CRITICAL
                        );
                    return;
                }
            }
        } else {
            teleportCooldown = TELEPORT_INTERVAL;
        }

        // 简单演示：每 100 ticks（5 秒）打印一次
        if (idleTicks % IDLE_TICK_INTERVAL == 0) {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "NPC {} 正在闲逛... ({} ticks)",
                entity.getName().getString(),
                idleTicks
            );
        }

        // 闲置过久时强制重评估一次，避免持续不活跃
        if (idleTicks >= MAX_IDLE_TICKS) {
            MindLog.decision(
                MindLogLevel.INFO,
                "NPC {} 闲置超过阈值，强制重评估目标",
                entity.getName().getString()
            );
            UtilityGoalSelector selector = mind.getGoalSelector();
            selector.forceReevaluate(mind, entity, SensorEventType.CRITICAL);
            idleTicks = 0;
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("is_idle");
        MindLog.decision(
            MindLogLevel.INFO,
            "NPC {} 停止闲逛",
            entity.getName().getString()
        );
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 闲置目标永不自动完成，只能被更高优先级的目标打断
        return false;
    }

    @Override
    public String getName() {
        return "idle";
    }

    /**
     * 选取附近可站立的随机传送点（地表）。
     */
    private BlockPos pickTeleportTarget(LivingEntity entity) {
        RandomSource random = entity.level().getRandom();
        BlockPos origin = entity.blockPosition();
        for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
            double angle = random.nextDouble() * TWO_PI;
            double distance =
                TELEPORT_MIN_DISTANCE +
                (TELEPORT_MAX_DISTANCE - TELEPORT_MIN_DISTANCE) *
                random.nextDouble();

            int x =
                origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z =
                origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos sample = new BlockPos(x, origin.getY(), z);
            if (!entity.level().hasChunkAt(sample)) {
                continue;
            }

            int y = entity
                .level()
                .getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (
                y < entity.level().getMinBuildHeight() ||
                y > entity.level().getMaxBuildHeight()
            ) {
                continue;
            }
            return new BlockPos(x, y, z);
        }
        return null;
    }
}
