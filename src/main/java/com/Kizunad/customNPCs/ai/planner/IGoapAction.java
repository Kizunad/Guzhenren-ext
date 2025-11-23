package com.Kizunad.customNPCs.ai.planner;

import com.Kizunad.customNPCs.ai.actions.IAction;

/**
 * GOAP 动作接口 - 扩展 IAction，添加规划所需的前置条件和效果
 * <p>
 * IGoapAction 是可被 GOAP 规划器使用的动作。
 * 除了执行层的 tick/start/stop 方法外，还需要定义：
 * - 前置条件 (Preconditions): 执行此动作需要满足的世界状态
 * - 效果 (Effects): 执行此动作后产生的世界状态变化
 * - 代价 (Cost): 执行此动作的代价，用于 A* 算法权重计算
 * <p>
 * 示例：
 * <pre>
 * ChopWoodAction:
 *   前置条件: {}  // 无前置条件
 *   效果: {has_wood: true}
 *   代价: 2.0
 * 
 * CraftPlanksAction:
 *   前置条件: {has_wood: true}
 *   效果: {has_planks: true}
 *   代价: 1.0
 * </pre>
 */
public interface IGoapAction extends IAction {
    
    /**
     * 获取执行此动作的前置条件
     * <p>
     * 前置条件定义了在执行此动作之前，世界状态必须满足的要求。
     * 规划器会检查当前状态是否满足前置条件，只有满足时才会考虑使用此动作。
     * 
     * @return 前置条件的世界状态
     */
    WorldState getPreconditions();
    
    /**
     * 获取执行此动作后产生的效果
     * <p>
     * 效果定义了执行此动作后，世界状态会发生的变化。
     * 规划器会应用效果来预测执行此动作后的新状态。
     * 
     * @return 效果的世界状态
     */
    WorldState getEffects();
    
    /**
     * 获取执行此动作的代价
     * <p>
     * 代价用于 A* 算法的权重计算 (g(n) 值)。
     * - 代价越高，规划器越倾向于避免使用此动作
     * - 代价越低，规划器越倾向于优先使用此动作
     * <p>
     * 代价可以表示：
     * - 时间消耗（例如移动距离越远代价越高）
     * - 资源消耗（例如使用昂贵材料代价高）
     * - 风险程度（例如危险动作代价高）
     * 
     * @return 动作代价，通常在 1.0 - 10.0 之间
     */
    float getCost();
}
