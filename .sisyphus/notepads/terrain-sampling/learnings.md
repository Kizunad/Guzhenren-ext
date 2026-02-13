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
