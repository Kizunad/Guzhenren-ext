package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * 闲置目标 - 默认目标，当没有其他事情做时执行
 * <p>
 * 此目标的优先级总是很低，作为保底目标
 */
public class IdleGoal implements IGoal {

    private static final float IDLE_PRIORITY = 0.1f;
    private static final int IDLE_TICK_INTERVAL = 100; // 每 5 秒打印一次（100 ticks）
    private static final int WANDER_INTERVAL = 40; // 闲逛尝试间隔
    private static final int WANDER_RANGE = 6; // 横向随机范围
    private static final double WANDER_SPEED = 1.0D;
    private static final double WANDER_ACCEPT_DIST = 1.5D;
    private static final int WANDER_TIMEOUT = 200;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final int WANDER_ATTEMPTS = 8;

    private int idleTicks;
    private int wanderCooldown;

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
        wanderCooldown = 0;
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

        // 若执行器空闲，周期性提交一次随机闲逛移动
        if (mind.getActionExecutor().isIdle()) {
            wanderCooldown--;
            if (wanderCooldown <= 0) {
                BlockPos target = pickWanderTarget(entity);
                if (target != null) {
                    mind
                        .getActionExecutor()
                        .addAction(
                            new MoveToAction(
                                new Vec3(
                                    target.getX() + BLOCK_CENTER_OFFSET,
                                    target.getY(),
                                    target.getZ() + BLOCK_CENTER_OFFSET
                                ),
                                WANDER_SPEED,
                                WANDER_ACCEPT_DIST,
                                WANDER_TIMEOUT
                            )
                        );
                }
                wanderCooldown = WANDER_INTERVAL;
            }
        } else {
            wanderCooldown = WANDER_INTERVAL;
        }

        // FUTURE: 实际的闲置行为
        // - 随机走动
        // - 观察周围
        // - 播放闲置动画

        // 简单演示：每 100 ticks（5 秒）打印一次
        if (idleTicks % IDLE_TICK_INTERVAL == 0) {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "NPC {} 正在闲逛... ({} ticks)",
                entity.getName().getString(),
                idleTicks
            );
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
     * 选取附近可站立的随机目标点。
     */
    private BlockPos pickWanderTarget(LivingEntity entity) {
        RandomSource random = entity.level().getRandom();
        BlockPos origin = entity.blockPosition();
        for (int i = 0; i < WANDER_ATTEMPTS; i++) {
            int dx = random.nextIntBetweenInclusive(-WANDER_RANGE, WANDER_RANGE);
            int dz = random.nextIntBetweenInclusive(-WANDER_RANGE, WANDER_RANGE);
            if (dx == 0 && dz == 0) {
                continue;
            }
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
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
