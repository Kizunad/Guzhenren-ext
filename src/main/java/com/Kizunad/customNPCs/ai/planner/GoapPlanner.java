package com.Kizunad.customNPCs.ai.planner;

import com.Kizunad.customNPCs.ai.actions.IAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * GOAP 规划器 - 使用 A* 算法生成动作序列
 * <p>
 * GoapPlanner 接收：
 * - 起始世界状态 (start)
 * - 目标世界状态 (goal)
 * - 可用的 GOAP 动作列表 (availableActions)
 * <p>
 * 然后使用 A* 搜索算法，寻找从起始状态到目标状态的动作路径。
 * <p>
 * A* 算法：
 * - f(n) = g(n) + h(n)
 * - g(n): 从起点到当前节点的实际代价
 * - h(n): 从当前节点到目标的启发式估计（未满足的目标状态数量）
 */
public class GoapPlanner {
    
    private static final int MAX_ITERATIONS = 100; // 防止无限循环
    
    /**
     * 规划动作序列
     * <p>
     * 使用 A* 算法搜索从起始状态到目标状态的动作路径。
     * 
     * @param start 起始世界状态
     * @param goal 目标世界状态
     * @param availableActions 可用的 GOAP 动作列表
     * @return 动作序列，如果无法规划则返回 null
     */
    public List<IAction> plan(WorldState start, WorldState goal, List<IGoapAction> availableActions) {
        // 如果起始状态已经满足目标，返回空计划
        if (start.matches(goal)) {
            return new ArrayList<>();
        }
        
        // 优先队列：按 f(n) = g(n) + h(n) 排序
        PriorityQueue<PlanNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(PlanNode::getFScore));
        
        // 已访问的状态（避免重复访问）
        Set<WorldState> closedSet = new HashSet<>();
        
        // 起始节点
        PlanNode startNode = new PlanNode(start, null, null, 0, start.distanceTo(goal));
        openSet.add(startNode);
        
        int iterations = 0;
        
        while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            
            // 取出 f(n) 最小的节点
            PlanNode currentNode = openSet.poll();
            
            // DEBUG
            System.out.println("Exploring node: " + currentNode.state + ", f=" + currentNode.getFScore());
            
            // 如果当前状态满足目标，回溯生成计划
            if (currentNode.state.matches(goal)) {
                return reconstructPlan(currentNode);
            }
            
            // 标记为已访问
            closedSet.add(currentNode.state);
            
            // 遍历所有可用动作
            for (IGoapAction action : availableActions) {
                // 检查前置条件是否满足
                if (!currentNode.state.matches(action.getPreconditions())) {
                    // DEBUG
                    System.out.println("  Action " + action.getClass().getSimpleName() + " preconditions not met");
                    continue; // 前置条件不满足，跳过此动作
                }
                
                // 应用动作效果，生成新状态
                WorldState newState = currentNode.state.apply(action.getEffects());
                
                // 如果新状态已访问过，跳过
                if (closedSet.contains(newState)) {
                    // DEBUG
                    System.out.println("  Action " + action.getClass().getSimpleName() + " leads to closed state");
                    continue;
                }
                
                // 计算新节点的代价
                float newGScore = currentNode.gScore + action.getCost();
                int newHScore = newState.distanceTo(goal);
                
                // 创建新节点
                PlanNode newNode = new PlanNode(newState, currentNode, action, newGScore, newHScore);
                
                // 检查是否已经在 openSet 中
                boolean inOpenSet = false;
                for (PlanNode node : openSet) {
                    if (node.state.equals(newState)) {
                        inOpenSet = true;
                        // 如果新路径更优，更新节点
                        if (newGScore < node.gScore) {
                            openSet.remove(node);
                            openSet.add(newNode);
                        }
                        break;
                    }
                }
                
                // 如果不在 openSet 中，添加
                if (!inOpenSet) {
                    openSet.add(newNode);
                    // DEBUG
                    System.out.println("  Added new node via " + action.getClass().getSimpleName() + ", f=" + newNode.getFScore());
                }
            }
        }
        
        // 规划失败
        if (iterations >= MAX_ITERATIONS) {
            System.err.println("[GoapPlanner] 规划超过最大迭代次数 (" + MAX_ITERATIONS + ")");
        } else {
            System.err.println("[GoapPlanner] 无法找到从起始状态到目标状态的路径");
        }
        
        return null;
    }
    
    /**
     * 回溯生成动作序列
     * @param goalNode 目标节点
     * @return 从起始到目标的动作序列
     */
    private List<IAction> reconstructPlan(PlanNode goalNode) {
        List<IAction> plan = new ArrayList<>();
        PlanNode currentNode = goalNode;
        
        // 从目标节点回溯到起始节点
        while (currentNode.parent != null) {
            plan.add(currentNode.action);
            currentNode = currentNode.parent;
        }
        
        // 反转列表（因为是从目标回溯到起点）
        Collections.reverse(plan);
        
        System.out.println("[GoapPlanner] 规划成功，生成 " + plan.size() + " 个动作");
        return plan;
    }
    
    /**
     * 规划节点 - A* 搜索的节点
     */
    private static class PlanNode {
        final WorldState state;      // 当前状态
        final PlanNode parent;        // 父节点
        final IGoapAction action;     // 到达此节点使用的动作
        final float gScore;           // 从起点到当前节点的实际代价
        final int hScore;             // 从当前节点到目标的启发式估计
        
        PlanNode(WorldState state, PlanNode parent, IGoapAction action, float gScore, int hScore) {
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.gScore = gScore;
            this.hScore = hScore;
        }
        
        /**
         * 获取 f(n) = g(n) + h(n)
         */
        float getFScore() {
            return gScore + hScore;
        }
    }
}
