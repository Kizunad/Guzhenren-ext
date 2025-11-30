# TriggerInterrupt 频繁触发与抖动问题

## 背景
当前 `NpcMind.triggerInterrupt` 仅基于事件类型 + 10 tick 冷却进行节流，未考虑来源实体或状态变化。当视觉/伤害传感器在同一目标上持续触发时，会出现以下问题：
- 同一实体被重复看到/命中会在冷却结束后持续打断，导致目标频繁重评估，行为抖动。
- 无状态变化情况下仍触发中断，增加日志与决策开销。

## 影响
- 性能：额外的中断日志与目标重评估，浪费 tick 时间。
- 行为：目标切换/计划重置过于频繁，威胁响应不稳定。

## 预期解决方案
- **事件去重**：在传感器层缓存 `(sourceId, eventType, distanceBucket)` 与时间戳；窗口内重复事件跳过。CRITICAL 窗口短（≈10 tick），IMPORTANT 窗口长（≈20–30 tick）。
- **状态变更驱动**：仅在 `current_threat_id` 变化或距离桶变化（远→近、近→极近）时触发中断；同目标同距离不触发。
- **记忆一致性**：在 Memory 写入/更新 `current_threat_id` 与 `target_distance_bucket`，供传感器比对；触发中断后写入最新威胁信息。
- **日志采样**：保留调试计数或采样日志，验证去重效果，默认 INFO 级别保持简洁。

## 完成标准
- 传感器实现去重与状态变更触发，CRITICAL 与 IMPORTANT 分级冷却生效。
- 重复看到同一目标时不再频繁触发中断；更换目标或距离段变化能及时触发。
- 新增或更新测试验证去重逻辑，或通过现有 GameTest 覆盖中断频率行为。

## 实施记录
- 提取通用 `InterruptThrottle`（按 targetId + eventLevel + distanceBucket 去重），窗口：CRITICAL 10 tick，IMPORTANT/INFO 25 tick。
- VisionSensor：基于距离桶/目标去重，目标或距离变化立即触发，否则窗口内忽略。
- DamageSensor：同一攻击者 CRITICAL 事件 10 tick 内去重，避免持续受击抖动。
- SafetySensor：按危险距离桶去重，近危/远危分别对应 CRITICAL/IMPORTANT。
- 日志维持 INFO 采样输出用于诊断。

完成后可重命名为 `[Resolved]interrupt-chatter.md`。

<!-- 解决后重命名为 [Resolved]interrupt-chatter.md -->
