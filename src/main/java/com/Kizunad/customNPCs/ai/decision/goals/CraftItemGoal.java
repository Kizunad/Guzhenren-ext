package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 制作物品目标（手动计划驱动）。
 * <p>
 * 设计目的：
 * - 不参与 Utility/GOAP 的自动优先级评估，避免干扰现有决策。
 * - 供后续 LLM/外部 Plan 工具注入具体制作计划后，通过命令或 forceSwitch 手动触发。
 */
public class CraftItemGoal implements IGoal {

    /** 手动注入的计划步骤 */
    private final List<IAction> assignedPlan = new ArrayList<>();
    private boolean started;
    private boolean completed;
    private String planLabel = "craft_item";

    /**
     * 注入制作计划。
     * @param actions 需要执行的动作序列
     * @param label 计划标签（可选，用于日志）
     */
    public void assignPlan(List<IAction> actions, String label) {
        assignedPlan.clear();
        if (actions != null) {
            assignedPlan.addAll(actions);
        }
        planLabel =
            label == null || label.isBlank() ? "craft_item" : label.trim();
        started = false;
        completed = false;
    }

    /**
     * 仅用于调试查看当前计划。
     * @return 不可修改的计划列表
     */
    public List<IAction> getAssignedPlan() {
        return Collections.unmodifiableList(assignedPlan);
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 不参与 Utility 评分，默认返回 0
        return 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 始终不参与自动评估，需通过外部命令/工具显式启动
        return false;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        started = true;
        if (assignedPlan.isEmpty()) {
            completed = true;
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftItemGoal 启动失败：未注入制作计划"
            );
            return;
        }
        mind.getActionExecutor().submitPlan(new ArrayList<>(assignedPlan));
        MindLog.decision(
            MindLogLevel.INFO,
            "CraftItemGoal 已提交 {} 个步骤的计划: {}",
            assignedPlan.size(),
            planLabel
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 计划执行完全由 ActionExecutor 驱动，无需额外 tick 逻辑
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        started = false;
        completed = false;
        assignedPlan.clear();
        mind.getActionExecutor().stopCurrentPlan();
        MindLog.decision(
            MindLogLevel.INFO,
            "CraftItemGoal 停止，清理现有计划"
        );
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (started && mind.getActionExecutor().isIdle()) {
            completed = true;
        }
        return completed;
    }

    @Override
    public String getName() {
        return "craft_item";
    }
}
