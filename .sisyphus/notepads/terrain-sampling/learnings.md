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

## 2026-02-13T23:10:37+13:00 任务5阻塞修复：缺失 empty 结构模板

- 当 `runGameTestServer` 报错 `Missing test structure: <namespace>:<suite>.empty` 时，应优先在 `src/main/resources/data/<namespace>/structure/` 下补齐同命名 `.empty.nbt`。
- 本仓库已有多个可复用空模板（如 `examplegametests.empty.nbt`）；直接复制现有空模板并按测试套件重命名即可满足 GameTest 结构加载约定，无需改 Java 代码。
- 本次补齐 `terrainsamplinggametests.empty.nbt` 后，`./gradlew runGameTestServer` 退出码恢复为 `0`，验证该类阻塞已解除。

## 2026-02-14T01:17:07+13:00 采样策略升级：random-hit -> locate 最近 biome

- 将 `OverworldTerrainSampler` 的无显式锚点路径从“随机 chunk 命中 biome（最多 20 次）”升级为“先 locate 最近 biome，再随机兜底”，显著降低四向采样全失败概率。
- 为保持兼容，保留原 `sampleAndPlace(..., @Nullable BlockPos explicitSourceAnchor, RandomSource random)` 签名，并新增可控起点重载 `sampleAndPlace(..., BlockPos searchOrigin, RandomSource random)`；旧调用无需修改即可继续工作。
- `ApertureCommand` 在初始化采样时将 `player.blockPosition()` 作为 `searchOrigin` 传入，使搜索起点可控且贴近玩家上下文。
- 底层替石头、Y 轴 clamp、`Block.UPDATE_CLIENTS` 放置语义保持不变，仅替换 biome 定位策略。

## 2026-02-14T14:39:07+13:00 四角摆放与深度自适应策略

- 四块粗采样目标锚点已由正东/南/西/北改为对角四角：东北、 西北、 西南、 东南（偏移使用 `(+16,-16) / (-16,-16) / (-16,+16) / (+16,+16)`）。
- 新增“低密度补深”规则：先按默认 `surface-4 ~ surface+12` 估算采样盒，若非空气占比低于阈值（`0.18`），则将底层再向下扩展 2 层，以减少薄片地形。
- 新增“底层去基岩”规则：若当前底层含基岩，按层上移到底层不含基岩为止，并设置最大上移次数上限（`MAX_BEDROCK_BOTTOM_SHIFTS`）避免死循环。
- 既有规则保持不变：底层流体/重力方块仍替换为石头，放置仍使用 `Block.UPDATE_CLIENTS`。

## 2026-02-14T15:40:28+13:00 四块采样改为2x2无缝拼接

- 将 `sampleInitialTerrains` 的四块目标锚点调整为 2x2 紧贴布局：
  - 西北 `center.offset(-16, 0, -16)`
  - 东北 `center.offset(0, 0, -16)`
  - 西南 `center.offset(-16, 0, 0)`
  - 东南 `center.offset(0, 0, 0)`
- 保持方向名称与偏移语义一致（西北/东北/西南/东南），用于失败日志定位。

## 2026-02-14T15:57:43+13:00 初始化顺序防覆盖修正

- 在 2x2 无缝采样布局下，中心区域会被粗采样写入；若先建平台再采样，核心与箱子会被覆盖。
- 初始化顺序调整为：`sampleInitialTerrains -> createInitialPlatform -> spawnLandSpirit -> markApertureInitialized`，保证平台最终可见。
- `spawnLandSpirit` 保持在核心创建之后执行，避免核心方块实体未就绪导致绑定失败。

## 2026-02-14T17:50:35+13:00 平台Y对齐采样最高层并持久化center

- 初始化阶段在 2x2 采样完成后，扫描覆盖区 `[-16..15] x [-16..15]` 的 `MOTION_BLOCKING_NO_LEAVES` 高度图，取最高层 `highestY`，并计算 `coreY = clamp(highestY + 1)`。
- 平台底层落在 `highestY`，核心落在 `coreY`，从而避免固定 Y 带来的悬空/埋没。
- 新增 `ApertureWorldData.updateCenter(UUID, BlockPos)`，以不可变 record 替换方式持久化新的核心中心坐标（尤其是 Y）。
- `enterAperture` 在 `initializeApertureIfNeeded` 后重新读取 `apertureInfo`，确保本次传送立即使用更新后的 center。

## 2026-02-14T19:51:48+13:00 采样锚点自然度过滤（防洞穴/矿井口）

- 在 `findBiomeLocation` 中新增候选自然度评估：locate 命中与随机兜底命中都先跑 `evaluateAnchorCandidate`，严格通过才立即接受。
- 评估维度覆盖整块 `16x16` 顶层列，包含：天空可见占比、自然地表占比、上方开放空气占比、石质顶层占比（惩罚项）。
- 若没有候选达到严格阈值，不会直接失败，而是保留评分最高的 best-effort 候选返回，兼顾“更自然”和“不易全失败”。
- 自然地表块集合显式包含草方块/泥土/砂/雪/灰化土/菌丝等，石质集合显式包含 stone/deepslate/tuff/cobble 系，避免纯石头平台感。
