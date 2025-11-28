package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试计划目标 - 用于测试动作队列功能
 * <p>
 * 此目标允许手动控制优先级，并在启动时提交预定义的动作序列。
 * 主要用于 GameTest 验证动作系统的正确性。
 */
public class TestPlanGoal implements IGoal {

    private final float priority;
    private final List<IAction> actionPlan;
    private boolean started;
    private boolean completed;

    /**
     * 创建测试计划目标
     * @param priority 优先级（0.0 - 1.0）
     * @param actionPlan 要执行的动作序列
     */
    public TestPlanGoal(float priority, List<IAction> actionPlan) {
        this.priority = priority;
        this.actionPlan = actionPlan;
        this.started = false;
        this.completed = false;
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return priority;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return !completed;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        started = true;
        // 提交动作序列到执行器
        mind.getActionExecutor().submitPlan(actionPlan);
        MindLog.decision(
            MindLogLevel.INFO,
            "测试计划目标开始，提交 {} 个动作",
            actionPlan.size()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 目标本身不需要做任何事，动作由 ActionExecutor 执行
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        MindLog.decision(MindLogLevel.INFO, "测试计划目标停止");
        started = false;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (started && mind.getActionExecutor().isIdle()) {
            completed = true;
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "test_plan";
    }
}
