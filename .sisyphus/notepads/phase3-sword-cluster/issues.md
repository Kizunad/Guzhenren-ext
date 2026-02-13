
[2026-02-13T09:42:46+13:00] 已知风险与注意事项
- 当前 deploy 通过 RecalledSword.displayItemUUID 定位存储项。若历史存档中该字段为空或重复，按 UUID 精确部署会失败（返回 false），需要后续任务补充“稳定主键迁移/回填策略”。
- currentLoad 依赖 activeSwords 与当前世界实体重算；跨维度或异常丢失实体场景下，存在短时状态不一致窗口，但 recall/deploy 路径会在下一次操作时自动收敛。

[2026-02-13T10:14:13+13:00] Task2 caveat
- 当前“同名判定”基于 `displayItem` 的物品注册名（Item ID），不区分同物品下的 NBT/组件差异；这符合基础版最小侵入目标，但若后续需要更细粒度共鸣（如同名且同品质/同词条），需扩展 identifier 解析策略并保持与 UI/同步口径一致。

[2026-02-13T11:12:00+13:00] Task4-子步骤 caveat
- `KEY_CLUSTER` 默认绑定 `C`，与主模块 `GuKeyBindings` 中 `FLYING_SWORD_RECALL_NEAREST` 默认键位冲突；当前按“本子步骤要求 C 开 Cluster”执行，后续需统一按键策略以避免双触发风险。
