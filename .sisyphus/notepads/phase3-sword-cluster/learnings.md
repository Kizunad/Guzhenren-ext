
[2026-02-13T09:42:46+13:00] Task1 后端实现要点
- Cluster 附件采用独立 AttachmentType（copyOnDeath），不混入既有 FlyingSwordStorageAttachment，避免职责耦合。
- activeSwords 仅保存“已出战实体 UUID”，并与 currentLoad 配合，作为集群服务的权威运行态。
- deploy 先做“已存在实体”分支判定：命中时只同步状态并返回成功，严格避免重复生成实体。
- deploy 的“从存储恢复”分支通过 RecalledSword.displayItemUUID 作为目标键，恢复成功后从 storage 移除并增加算力负载。
- recall 统一走 FlyingSwordController.finishRecall 负责落库存储，随后刷新 activeSwords/currentLoad，减少重复存储逻辑。
- 算力规则落地为常量：Max = 10 + 魂道*0.1 + 智道*0.2；Cost = 1 + 品质阶*2，避免魔法数裸露。
