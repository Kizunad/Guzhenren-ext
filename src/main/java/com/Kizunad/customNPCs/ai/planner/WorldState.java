package com.Kizunad.customNPCs.ai.planner;

import java.util.HashMap;
import java.util.Map;

/**
 * 世界状态 - 描述世界的当前状态或目标状态
 * <p>
 * WorldState 使用键值对 (String -> Object) 存储状态原子，例如：
 * - "has_apple": true
 * - "health_low": false
 * - "distance_to_home": 10.5
 * <p>
 * 用于 GOAP 规划器进行前置条件检查和效果应用。
 */
public class WorldState {

    private final Map<String, Object> states;

    /**
     * 创建一个空的世界状态
     */
    public WorldState() {
        this.states = new HashMap<>();
    }

    /**
     * 从现有状态创建副本
     * @param other 要复制的状态
     */
    public WorldState(WorldState other) {
        this.states = new HashMap<>(other.states);
    }

    /**
     * 设置单个状态值
     * @param key 状态键
     * @param value 状态值
     */
    public void setState(String key, Object value) {
        states.put(key, value);
    }

    /**
     * 获取单个状态值
     * @param key 状态键
     * @return 状态值，如果不存在则返回 null
     */
    public Object getState(String key) {
        return states.get(key);
    }

    /**
     * 获取单个状态值，带默认值
     * @param key 状态键
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 状态值，如果不存在则返回默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getState(String key, T defaultValue) {
        return (T) states.getOrDefault(key, defaultValue);
    }

    /**
     * 检查是否包含某个状态键
     * @param key 状态键
     * @return true 如果存在
     */
    public boolean hasState(String key) {
        return states.containsKey(key);
    }

    /**
     * 移除一个状态
     * @param key 状态键
     */
    public void removeState(String key) {
        states.remove(key);
    }

    /**
     * 检查当前状态是否满足目标状态的所有要求
     * <p>
     * 匹配规则：
     * - 目标状态中的每个键值对在当前状态中都必须存在且相等
     * - 当前状态可以有额外的键值对（不影响匹配）
     *
     * @param goal 目标状态
     * @return true 如果当前状态满足目标状态的所有要求
     */
    public boolean matches(WorldState goal) {
        for (Map.Entry<String, Object> entry : goal.states.entrySet()) {
            String key = entry.getKey();
            Object goalValue = entry.getValue();

            // 如果当前状态不包含此键，则不匹配
            if (!this.states.containsKey(key)) {
                return false;
            }

            // 如果值不相等，则不匹配
            Object currentValue = this.states.get(key);
            if (!areEqual(currentValue, goalValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 应用效果到当前状态，生成新的状态
     * <p>
     * 此方法创建当前状态的副本，然后应用效果中的所有键值对。
     *
     * @param effects 要应用的效果
     * @return 应用效果后的新状态
     */
    public WorldState apply(WorldState effects) {
        WorldState newState = new WorldState(this);
        newState.states.putAll(effects.states);
        return newState;
    }

    /**
     * 创建此状态的深拷贝
     * @return 状态副本
     */
    public WorldState copy() {
        return new WorldState(this);
    }

    /**
     * 清空所有状态
     */
    public void clear() {
        states.clear();
    }

    /**
     * 获取状态数量
     * @return 状态键值对数量
     */
    public int size() {
        return states.size();
    }

    /**
     * 检查是否为空
     * @return true 如果没有任何状态
     */
    public boolean isEmpty() {
        return states.isEmpty();
    }

    /**
     * 计算与目标状态的差异数量（启发式函数）
     * <p>
     * 用于 A* 算法的启发式估计。
     * 返回目标状态中有多少个键值对在当前状态中不满足。
     *
     * @param goal 目标状态
     * @return 不满足的状态项数量
     */
    public int distanceTo(WorldState goal) {
        int distance = 0;

        for (Map.Entry<String, Object> entry : goal.states.entrySet()) {
            String key = entry.getKey();
            Object goalValue = entry.getValue();

            if (
                !this.states.containsKey(key) ||
                !areEqual(this.states.get(key), goalValue)
            ) {
                distance++;
            }
        }

        return distance;
    }

    /**
     * 比较两个对象是否相等
     * @param a 对象 A
     * @param b 对象 B
     * @return true 如果相等
     */
    private boolean areEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WorldState{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : states.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WorldState)) {
            return false;
        }
        WorldState other = (WorldState) obj;
        return states.equals(other.states);
    }

    @Override
    public int hashCode() {
        return states.hashCode();
    }
}
