package com.Kizunad.customNPCs.ai.actions;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 动作接口 - 代表 NPC 可执行的原子动作
 * <p>
 * 动作是最小的执行单位，具有明确的开始、执行和结束状态。
 * 动作由 ActionExecutor 管理，可以组合成动作序列（计划）。
 * <p>
 * 生命周期：
 * 1. start() - 动作开始时调用一次
 * 2. tick() - 每个游戏 tick 调用，直到返回 SUCCESS 或 FAILURE
 * 3. stop() - 动作结束时调用一次（无论成功或失败）
 */
public interface IAction {
    /**
     * 执行动作逻辑 - 每个 tick 调用
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return 动作状态（RUNNING/SUCCESS/FAILURE）
     */
    ActionStatus tick(INpcMind mind, LivingEntity entity);

    /**
     * 执行动作逻辑，返回包含原因的结果。
     * 默认实现沿用 {@link #tick(INpcMind, LivingEntity)}，可在子类中重写以提供更丰富的失败/成功原因。
     *
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return 动作结果（含状态与原因）
     */
    default ActionResult tickWithReason(INpcMind mind, LivingEntity entity) {
        return new ActionResult(tick(mind, entity));
    }

    /**
     * 动作开始时调用 - 初始化动作状态
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    void start(INpcMind mind, LivingEntity entity);

    /**
     * 动作停止时调用 - 清理动作状态
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    void stop(INpcMind mind, LivingEntity entity);

    /**
     * 动作是否可被中断
     * <p>
     * 如果返回 true，则此动作可以被更高优先级的目标打断。
     * 如果返回 false，则必须等待动作完成（SUCCESS/FAILURE）。
     *
     * @return true 如果可被中断
     */
    boolean canInterrupt();

    /**
     * 获取动作名称
     * <p>
     * 用于调试和日志输出
     *
     * @return 动作的唯一标识符
     */
    String getName();
}
