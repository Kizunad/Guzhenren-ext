## 2026-02-13 任务2：ApertureRegionCopier（仙窍细采样）

- 采用“先采源 BlockEntity NBT、再落目标 BlockState、最后回灌 NBT”的三段式流程，可避免因目标尚未生成方块实体导致的 NBT 丢失。
- 使用 `sourcePos.subtract(normalizedMin)` 计算偏移，再以 `targetAnchor.offset(offset)` 放置，可保证任意 `sourceMin/sourceMax` 输入顺序下的相对结构一致性。
- 复制前调用 `level.removeBlockEntity(targetPos)` 清理目标旧 BE，可降低目标残留数据污染风险；回灌后调用 `setChanged()` 以确保脏标记生效。
- `copyRegionSized` 采用中心点 + 边长推导最小角方案，统一委托给 `copyRegion`，减少重复逻辑并提升后续测试可控性。

## 2026-02-13 Wave1-任务1：OverworldTerrainSampler

- 已新增 `OverworldTerrainSampler.sampleAndPlace(...)`，签名与计划一致，支持显式源锚点与随机 biome 搜索两种入口。
- 随机搜索采用 chunk 对齐（16 边界）+ 中心点 biome 判定，最多 20 次，失败返回 `false`。
- 地表高度使用 `MOTION_BLOCKING_NO_LEAVES`，Y 采样区间为 `surface-4` 到 `surface+12`，并对维度高度进行了 clamp。
- 粗采样仅复制 `BlockState`；采样盒最底层若检测到流体或 `FallingBlock`，替换为 `Blocks.STONE`。
- 目标放置使用 `Block.UPDATE_CLIENTS`，不触发邻居更新，符合计划约束。

## 2026-02-13T21:36 Task1/Task2 规则回填

- Task1（`OverworldTerrainSampler`）规则摘要：
  - 显式源锚点优先；仅当显式锚点为空时才执行随机搜索。
  - Biome 采用采样块中心点判定，随机 chunk 对齐搜索最多 20 次。
  - 地表高度统一取 `MOTION_BLOCKING_NO_LEAVES`。
  - Y 采样区间按 `surface-4 ~ surface+12` 计算，并 clamp 到维度最小/最大可建高度。
  - 采样盒最底层若为流体或重力方块（`FallingBlock`），替换为 `Blocks.STONE`。
  - 目标放置标志使用 `Block.UPDATE_CLIENTS`（仅客户端可见更新，不触发邻居更新）。

- Task2（`ApertureRegionCopier`）规则摘要：
  - 基础复制对象为 `BlockState`。
  - BlockEntity NBT 复制链路为：`saveWithoutMetadata`（源）→ 目标落块 → `loadWithComponents`（目标）。
  - 明确不复制实体（Entity）、`ScheduledTick`、Light 数据。

## 2026-02-13T21:43 working-tree-scope-cleanup

- 已执行 terrain-sampling 交付范围收敛：保留 Task1/Task2 相关实现与本计划 notepad 记录，移除无关任务遗留改动。

## 2026-02-13 任务3：ApertureCommand 初始化接线

- 将四向地形采样接线放在 `initializeApertureIfNeeded` 的初始化收尾阶段，位于平台与地灵创建之后、`markApertureInitialized` 之前，保证不打乱既有初始化主流程。
- 采样目标锚点按核心 `center` 的固定平面偏移布局：东 `(+16,0,0)`、南 `(0,0,+16)`、西 `(-16,0,0)`、北 `(0,0,-16)`。
- 目标 biome 统一采用 `Biomes.PLAINS` 常量并通过 biome registry 获取 `Holder<Biome>`，满足“明确常量”要求且避免 magic 字符串。
- 失败策略采用“主世界不可用整段跳过 + 单方向失败仅告警继续”，确保初始化过程不会因采样失败中断。

## 2026-02-13 任务4：TerrainSampling GameTest 编写

- `OverworldTerrainSampler.sampleAndPlace` 的“底层替换”可通过显式源锚点 + 固定高度基准稳定复现：先在采样中心放置基准方块，再在 `sourceMinY` 预置流体/砂子/普通方块，即可稳定触发底层分支。
- 为避免砂子在同 tick 下落导致断言不稳定，需要在砂子下方预置支撑方块（石头），从而保证“非底层保留原状”断言稳定。
- `ApertureRegionCopier.copyRegion` 的 NBT 保真验证可用“1x1x1 箱子区域 + 槽位0钻石x1”最小闭环；断言目标方块类型与槽位物品即可覆盖 BE 存在性与 NBT 载入链路。
