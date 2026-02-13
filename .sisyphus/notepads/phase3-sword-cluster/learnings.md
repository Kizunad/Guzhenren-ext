
[2026-02-13T09:42:46+13:00] Task1 后端实现要点
- Cluster 附件采用独立 AttachmentType（copyOnDeath），不混入既有 FlyingSwordStorageAttachment，避免职责耦合。
- activeSwords 仅保存“已出战实体 UUID”，并与 currentLoad 配合，作为集群服务的权威运行态。
- deploy 先做“已存在实体”分支判定：命中时只同步状态并返回成功，严格避免重复生成实体。
- deploy 的“从存储恢复”分支通过 RecalledSword.displayItemUUID 作为目标键，恢复成功后从 storage 移除并增加算力负载。
- recall 统一走 FlyingSwordController.finishRecall 负责落库存储，随后刷新 activeSwords/currentLoad，减少重复存储逻辑。
- 算力规则落地为常量：Max = 10 + 魂道*0.1 + 智道*0.2；Cost = 1 + 品质阶*2，避免魔法数裸露。

[2026-02-13T10:14:13+13:00] Task2 Synergy 基础实现总结
- evaluate 规则：在 `ClusterSynergyHelper.evaluate(List<FlyingSwordEntity> activeSwords)` 中按“同名标识（当前取展示物品注册名）”计数，任一标识数量达到 `3` 即触发基础共鸣，产出 `+10%` 攻击增益（`SYNERGY_ATTACK_BONUS = 0.10F`）。
- 结果结构采用可扩展设计：返回 `ClusterSynergyResult(attackMultiplier, effects, triggers)`，其中 `effects`（效果键值映射）与 `triggers`（触发明细）为后续多共鸣规则、UI 展示、网络同步预留稳定消费接口，避免未来反复改调用方签名。
- 战斗链路接线点：在 `SwordCombatOps.calculateDamage(...)` 中新增 `resolveClusterSynergyAttackMultiplier(...)`，通过玩家 `FlyingSwordClusterAttachment.activeSwords` 过滤当前活跃飞剑后调用 `ClusterSynergyHelper.evaluate(...)`，最终将倍率并入伤害计算：`baseDamage * speedBonus * synergyAttackMultiplier`。

[2026-02-13T10:45:00+13:00] Task3 Screen 构造函数适配要点
- Screen 注册接口 `RegisterMenuScreensEvent.register` 要求构造函数签名必须是 `(M menu, Inventory inv, Component title)`，不能包含额外自定义参数（如 `Theme`）。
- 解决方案：在 Screen 构造函数内部自行初始化依赖（如 `Theme.vanilla()`），移除外部注入的 `Theme` 参数，确保 `::new` 引用与接口签名匹配。
- 此外，TinyUI 的 `TinyUIContainerScreen` 内部虽然设计了 Theme 注入，但在 NeoForge 标准注册流程下，通常需要通过此方式适配或使用工厂方法。

[2026-02-13T11:12:00+13:00] Task4-子步骤（开屏链路）实现要点
- 复用 `ServerboundOpenTrainingGuiPayload` 模式可快速落地“按键请求服务端开 GUI”：`StreamCodec.unit(new Payload()) + context.enqueueWork + ServerPlayer 判定`。
- 空窍侧按键建议集中在 `KongqiaoKeyMappings`，并在 `KongqiaoClientEvents.registerKeys` 显式注册，避免仅声明不生效。
- `KongqiaoService` 作为菜单打开入口可保持一致职责：payload 不直接构建 menu，而是调用 service 的 `openFlyingSwordClusterMenu`。
- 本子步骤只做 open 链路，避免提前引入 action/state payload，可显著降低一次改动面和 checkstyle/build 风险。
