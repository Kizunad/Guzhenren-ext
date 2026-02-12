
[2026-02-13T09:42:46+13:00] 已知风险与注意事项
- 当前 deploy 通过 RecalledSword.displayItemUUID 定位存储项。若历史存档中该字段为空或重复，按 UUID 精确部署会失败（返回 false），需要后续任务补充“稳定主键迁移/回填策略”。
- currentLoad 依赖 activeSwords 与当前世界实体重算；跨维度或异常丢失实体场景下，存在短时状态不一致窗口，但 recall/deploy 路径会在下一次操作时自动收敛。
