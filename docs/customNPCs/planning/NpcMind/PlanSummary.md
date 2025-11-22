# 自主 NPC 思维架构（精简版）

## 1. 核心理念

NPC 自主行为可分为：

* **长期目标（Long-Term Goals）**：人生方向与持久动机
* **短期目标（Short-Term Goals）**：可执行计划
* **具体行为（Actions）**：原子动作

此结构符合 GOAP、HTN、UtilityAI 等成熟 AI 模型。

---

## 2. 长期目标（Long-Term Goals）

NPC 的高层追求，例如：

* 生存
* 修炼突破
* 收集资源
* 增强实力
* 探索世界
* 社交与互动

长期目标具有持久性，并会因外界事件自动重新排序（如受伤 → 生存优先）。

---

## 3. 短期目标（Short-Term Goals）

根据长期目标推导出的具体计划，如：

* 为突破寻找灵气浓郁地点
* 逃跑、治疗、寻找掩体
* 冥想恢复灵力
* 收集附近的资源

短期目标可持续数秒到数分钟，由决策系统动态切换。

---

## 4. 行为层（Actions）

NPC 可执行的最小单位动作：

* 移动、攻击、施法
* 搜索、采集、合成
* 冥想、休息
* 对话、交易

行为需要：可中断、可失败、可继续、带前置条件。

---

## 5. NPC 思考循环（Decision Loop）

1. **观察（Sense）**：获取世界状态
2. **评估（Evaluate）**：计算长期目标权重
3. **决策（Select）**：挑选当前主目标
4. **规划（Plan）**：生成短期目标序列
5. **执行（Act）**：行为树/任务系统运行
6. **中断判断（Interrupt）**：情况变化时重新评估

---

## 6. 示例（修仙 NPC）

长期目标与权重示例：

* 生存：0.9
* 修炼突破：0.7
* 搜寻灵物：0.5
* 打坐恢复灵力：0.4

状态变化会影响权重，如“灵力不足 → 冥想权重升至 0.9”。

短期目标如：

* 找高灵气点冥想
* 使用保命技能
* 寻找突破安全地点

---

## 7. 推荐的 NPC 架构

```
NPC → Mind
  LongTermGoalController
  ShortTermPlanner
  BehaviorExecutor

NPC → Sensors
NPC → Memory
NPC → PersonalityTags
```

* **LongTermGoalController**：目标优先级/权重决策（Utility AI）
* **ShortTermPlanner**：任务规划（GOAP / HTN）
* **BehaviorExecutor**：具体行为执行器

---

## 8. 一句话总结

> **长期目标决定“为什么做”，短期目标决定“做什么”，行为系统决定“怎么做”。**
