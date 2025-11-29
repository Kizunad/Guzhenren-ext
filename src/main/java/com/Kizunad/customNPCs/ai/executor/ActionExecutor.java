package com.Kizunad.customNPCs.ai.executor;

import com.Kizunad.customNPCs.ai.actions.ActionResult;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;

/**
 * 动作执行器 - 管理和执行动作队列
 * <p>
 * 执行器负责：
 * - 维护动作队列（FIFO）
 * - 按顺序执行动作
 * - 处理动作状态转换（RUNNING → SUCCESS/FAILURE）
 * - 在失败时清空队列
 */
public class ActionExecutor {

    private final Queue<IAction> actionQueue;
    private IAction currentAction;
    private UUID boundEntityId;
    private ActionStatus lastActionStatus = ActionStatus.RUNNING;
    private String lastActionReason = "";

    public ActionExecutor() {
        this.actionQueue = new LinkedList<>();
        this.currentAction = null;
        this.boundEntityId = null;
    }

    /**
     * 将执行器绑定到当前实体上下文，防止不同测试/实体间的计划相互污染。
     */
    public void bindToEntity(LivingEntity entity) {
        UUID entityId = entity.getUUID();
        if (boundEntityId == null || !boundEntityId.equals(entityId)) {
            // 新的实体上下文，重置执行器状态
            stopCurrentPlan();
            this.boundEntityId = entityId;
            MindLog.execution(
                MindLogLevel.DEBUG,
                "绑定到实体 {} (id={})",
                entity.getName().getString(),
                entity.getId()
            );
        }
    }

    private boolean isContextValid(LivingEntity entity) {
        if (boundEntityId == null) {
            return true;
        }
        if (!boundEntityId.equals(entity.getUUID())) {
            MindLog.execution(
                MindLogLevel.WARN,
                "上下文实体变化，丢弃计划"
            );
            stopCurrentPlan();
            return false;
        }
        return true;
    }

    /**
     * 提交新的动作序列（计划）
     * <p>
     * 此方法会：
     * 1. 停止当前正在执行的动作
     * 2. 清空现有队列
     * 3. 添加新的动作序列
     *
     * @param actions 要执行的动作列表
     */
    public void submitPlan(List<IAction> actions) {
        // 停止当前动作
        if (currentAction != null) {
            currentAction.stop(null, null); // 注意：这里传 null 是安全的，因为 stop 通常只用于清理内部状态
            currentAction = null;
        }

        // 清空队列
        actionQueue.clear();

        // 重置状态，避免上一计划的失败标记影响新计划
        lastActionStatus = ActionStatus.RUNNING;

        // 添加新动作
        actionQueue.addAll(actions);

        MindLog.execution(
            MindLogLevel.INFO,
            "提交新计划，包含 {} 个动作",
            actions.size()
        );
    }

    /**
     * 追加单个动作到队列末尾
     * @param action 要添加的动作
     */
    public void addAction(IAction action) {
        actionQueue.add(action);
    }

    /**
     * 停止当前计划
     * <p>
     * 清空队列并停止当前动作
     */
    public void stopCurrentPlan() {
        if (currentAction != null) {
            currentAction.stop(null, null);
            currentAction = null;
        }
        actionQueue.clear();
        lastActionStatus = ActionStatus.RUNNING;
        MindLog.execution(MindLogLevel.INFO, "当前计划已停止");
    }

    /**
     * 执行器的 tick 方法 - 每个游戏 tick 调用
     * <p>
     * 执行逻辑：
     * 1. 如果没有当前动作，从队列取出下一个并启动
     * 2. 执行当前动作的 tick()
     * 3. 根据返回状态决定下一步：
     *    - RUNNING: 继续执行
     *    - SUCCESS: 停止当前动作，进入下一个
     *    - FAILURE: 停止当前动作，清空队列（整个计划失败）
     *
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    public void tick(INpcMind mind, LivingEntity entity) {
        if (!isContextValid(entity)) {
            return;
        }
        // 如果没有当前动作，尝试从队列获取
        if (currentAction == null) {
            if (!actionQueue.isEmpty()) {
                currentAction = actionQueue.poll();
                currentAction.start(mind, entity);
                MindLog.execution(
                    MindLogLevel.INFO,
                    "开始执行动作: {}",
                    currentAction.getName()
                );
            } else {
                // 队列为空，无事可做
                return;
            }
        }

        // 执行当前动作
        ActionResult result = currentAction.tickWithReason(mind, entity);
        ActionStatus status = result.status();
        lastActionReason = result.reason();

        // 根据状态处理
        switch (status) {
            case SUCCESS:
                lastActionStatus = ActionStatus.SUCCESS;
                MindLog.execution(
                    MindLogLevel.INFO,
                    "动作成功完成: {}",
                    currentAction.getName()
                );
                currentAction.stop(mind, entity);
                currentAction = null;
                // 下一个 tick 会自动取出下一个动作
                break;
            case FAILURE:
                lastActionStatus = ActionStatus.FAILURE;
                if (lastActionReason != null && !lastActionReason.isEmpty()) {
                    MindLog.execution(
                        MindLogLevel.WARN,
                        "动作失败: {}，原因: {}，清空计划",
                        currentAction.getName(),
                        lastActionReason
                    );
                } else {
                    MindLog.execution(
                        MindLogLevel.WARN,
                        "动作失败: {}，清空计划",
                        currentAction.getName()
                    );
                }
                currentAction.stop(mind, entity);
                currentAction = null;
                actionQueue.clear(); // 整个计划失败
                break;
            case RUNNING:
                lastActionStatus = ActionStatus.RUNNING;
                // 继续执行，无需操作
                break;
        }
    }

    /**
     * 获取当前正在执行的动作
     * @return 当前动作，如果没有则返回 null
     */
    public IAction getCurrentAction() {
        return currentAction;
    }

    /**
     * 检查执行器是否空闲
     * @return true 如果没有当前动作且队列为空
     */
    public boolean isIdle() {
        return currentAction == null && actionQueue.isEmpty();
    }

    /**
     * 获取队列中剩余的动作数量
     * @return 剩余动作数量（不包括当前正在执行的动作）
     */
    public int getRemainingActionCount() {
        return actionQueue.size();
    }

    /**
     * 获取最后一个动作的状态
     * <p>
     * 用于 PlanBasedGoal 检测动作失败并触发重规划
     *
     * @return 最后的动作状态
     */
    public ActionStatus getLastActionStatus() {
        return lastActionStatus;
    }

    /**
     * 获取最后一个动作的原因（若实现提供）。
     * @return 原因描述，可能为空字符串
     */
    public String getLastActionReason() {
        return lastActionReason;
    }
}
