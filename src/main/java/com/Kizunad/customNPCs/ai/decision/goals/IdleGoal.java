package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 闲置目标 - 默认目标，当没有其他事情做时执行
 * <p>
 * 此目标的优先级总是很低，作为保底目标
 */
public class IdleGoal implements IGoal {

    private static final float IDLE_PRIORITY = 0.1f;
    private static final int IDLE_TICK_INTERVAL = 100; // 每 5 秒打印一次（100 ticks）

    private int idleTicks;

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
}
