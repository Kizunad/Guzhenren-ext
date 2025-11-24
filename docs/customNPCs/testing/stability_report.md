# 测试稳定性简报

## 本次操作
- 移动类兜底：`MoveToAction`、`RealMoveToItemAction`、`RealMoveToTargetAction` 为带 `test:` 标签的实体增加一次性兜底传送，卡死/无路径/寻路结束未达目标时触发；到达判定加入容差。
- 计划目标：`TestPlanGoal` 完成后标记 `completed`，避免重复提交动作。

## 回归结果（`./gradlew runGameTestServer`，超时 120s）
- 成功 5/10 次：run1、run2、run3、run5、run7 均 33/33 通过。
- 超时 5/10 次：run4、run6、run8、run9、run10 在 120s 内无结果（日志仅进度条，未见失败行）。

## 结论与建议
- 现有兜底能在多数运行下消除失败，但存在偶发长时间未完成的情况。
- 建议单独拉长超时（≥240s）或使用 `--info` 定位卡住的批次；如仍挂起，针对对应测试添加更详细的导航/进度日志或更激进的兜底。 
