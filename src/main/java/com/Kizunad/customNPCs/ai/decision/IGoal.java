package com.Kizunad.customNPCs.ai.decision;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 目标接口 - 代表 NPC 的一个长期目标
 * <p>
 * 使用 Utility AI 模式：每个目标计算自己的优先级分数，
 * 目标选择器会选择分数最高的目标执行。
 */
public interface IGoal {
    
    /**
     * 计算此目标的优先级分数
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return 优先级分数（0.0 - 1.0），越高越优先
     */
    float getPriority(INpcMind mind, LivingEntity entity);
    
    /**
     * 目标是否可以运行
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return true 如果目标可以运行
     */
    boolean canRun(INpcMind mind, LivingEntity entity);
    
    /**
     * 目标开始时调用
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    void start(INpcMind mind, LivingEntity entity);
    
    /**
     * 每个 tick 调用（如果此目标是当前活动目标）
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    void tick(INpcMind mind, LivingEntity entity);
    
    /**
     * 目标停止时调用
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    void stop(INpcMind mind, LivingEntity entity);
    
    /**
     * 目标是否已完成
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return true 如果目标已完成
     */
    boolean isFinished(INpcMind mind, LivingEntity entity);
    
    /**
     * 获取目标的唯一标识符
     * @return 目标名称
     */
    String getName();
    
    /**
     * 获取目标期望达成的世界状态
     * <p>
     * 此方法用于基于 GOAP 规划的目标。
     * 如果此目标不使用 GOAP，返回 null。
     * 
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return 目标状态，如果不是基于规划的目标则返回 null
     */
    default com.Kizunad.customNPCs.ai.planner.WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        return null;
    }
    
    /**
     * 获取可用的 GOAP 动作列表
     * <p>
     * 此方法用于基于 GOAP 规划的目标。
     * 如果此目标不使用 GOAP，返回空列表。
     * 
     * @param mind NPC 的思维
     * @param entity NPC 实体
     * @return GOAP 动作列表，如果不是基于规划的目标则返回空列表
     */
    default java.util.List<com.Kizunad.customNPCs.ai.planner.IGoapAction> getAvailableActions(
            INpcMind mind, LivingEntity entity) {
        return java.util.Collections.emptyList();
    }
}
