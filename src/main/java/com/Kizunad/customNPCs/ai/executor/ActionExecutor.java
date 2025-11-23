package com.Kizunad.customNPCs.ai.executor;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    
    public ActionExecutor() {
        this.actionQueue = new LinkedList<>();
        this.currentAction = null;
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
        
        // 添加新动作
        actionQueue.addAll(actions);
        
        System.out.println("[ActionExecutor] 提交新计划，包含 " + actions.size() + " 个动作");
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
        System.out.println("[ActionExecutor] 当前计划已停止");
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
        // 如果没有当前动作，尝试从队列获取
        if (currentAction == null) {
            if (!actionQueue.isEmpty()) {
                currentAction = actionQueue.poll();
                currentAction.start(mind, entity);
                System.out.println("[ActionExecutor] 开始执行动作: " + currentAction.getName());
            } else {
                // 队列为空，无事可做
                return;
            }
        }
        
        // 执行当前动作
        ActionStatus status = currentAction.tick(mind, entity);
        
        // 根据状态处理
        switch (status) {
            case SUCCESS:
                System.out.println("[ActionExecutor] 动作成功完成: " + currentAction.getName());
                currentAction.stop(mind, entity);
                currentAction = null;
                // 下一个 tick 会自动取出下一个动作
                break;
                
            case FAILURE:
                System.out.println("[ActionExecutor] 动作失败: " + currentAction.getName() + "，清空计划");
                currentAction.stop(mind, entity);
                currentAction = null;
                actionQueue.clear(); // 整个计划失败
                break;
                
            case RUNNING:
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
}
