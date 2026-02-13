# 计划：仙窍地形采样工具

## 概要

> **简述**：创建两套地形采样工具函数——主世界粗采样（按 Biome 筛选，搬 16×16×16 方块区域）和仙窍细采样（含 NBT 精确复制），并在仙窍初始化时调用粗采样在核心四周各放置一块自然地形。
> 
> **交付物**：
> - `OverworldTerrainSampler`：主世界粗采样工具类
> - `ApertureRegionCopier`：仙窍维度细采样工具类
> - 更新 `ApertureCommand.initializeApertureIfNeeded`：初始化时放置 4 块地形
> - GameTest 覆盖核心逻辑
> 
> **预估工作量**：中等
> **并行执行**：是 — 2 波
> **关键路径**：任务 1（粗采样） → 任务 3（初始化集成） → 任务 5（运行时验证）

---

## 背景

### 原始需求
用户认为之前的噪声模拟方案过于复杂，提出更简单的思路：直接从主世界搬运 16×16×16 方块区域到仙窍，底层流体/重力方块替换为石头。另外需要仙窍维度内的精确采样（含 NBT），用于"吞并其他仙窍"功能。

### 讨论与决策总结
**关键决策**：
- **两套函数**：粗采样（主世界→仙窍，仅 BlockState）和细采样（仙窍→仙窍，含 NBT）
- **Y 轴**：以 `MOTION_BLOCKING_NO_LEAVES` 高度图为地表，地表下 4 格 + 地表上 12 格
- **Biome 筛选**：中心点判定
- **放置位置**：紧贴已有区域拼接；初始化时核心东南西北各偏移 16 格各放一块
- **采样时机**：Phase 2 先同步（主线程），后续优化为异步
- **X/Z 对齐**：对齐到 16 边界（chunk 对齐）
- **放置更新**：默认不触发方块更新，放完后统一刷新
- **粗采样遇 BlockEntity**：允许产生空 BE（不复制 NBT）
- **细采样**：仅 BlockState + BlockEntity NBT，不复制实体/ScheduledTick/Light
- **Biome 搜索失败**：最多尝试 20 次，失败后降级为随机 Biome
- **半结构体截断**（门/床等）：Phase 2 允许截断，不做修复

**Metis 审查（护栏）**：
- 不改 ChunkGenerator / 世界生成管线
- 不复制实体、ScheduledTick、Light
- 不在 server 路径引用 client-only 类
- 放置位置必须在安全半径内
- 函数必须接受显式源锚点参数（可测性）
- 不引入复杂异步任务系统

---

## 工作目标

### 核心目标
实现两套地形采样工具函数，并在仙窍初始化流程中集成粗采样，让新仙窍拥有自然地形而非空虚空。

### 具体交付物
- `src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/OverworldTerrainSampler.java`
- `src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/ApertureRegionCopier.java`
- 更新 `src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java`
- GameTest 文件（至少 2 个测试）

### 完成定义
- [ ] 主世界粗采样能按 Biome 筛选并搬运 16×16×16 区域到仙窍
- [ ] 底层流体/重力方块自动替换为石头
- [ ] 仙窍细采样能精确复制含 NBT 的区域
- [ ] 仙窍初始化时核心四周各有一块自然地形
- [ ] `./gradlew checkstyleMain` 通过
- [ ] `./gradlew build` 通过

### 必须包含
- 确定性入口：函数接受显式源锚点，随机只是上层封装
- Y clamp 到维度 min/max build height
- 放置位置在安全半径内的校验

### 禁止包含（护栏）
- **改动 ChunkGenerator**（`ApertureVoidChunkGenerator` 保持不变）
- **复制实体（Entity）**
- **复制 ScheduledTick / Light 数据**
- **引入异步任务框架**（Phase 2 只做同步）
- **修复半结构体截断**（门/床等跨方块结构）
- **改动 ChaosErosionHandler / FragmentPlacementService 的现有逻辑**
- **新增 UI / 配置文件 / 网络协议**

---

## 验证策略

> **通用规则：零人工干预**
> 所有验证均由 Agent 执行。

### 测试决策
- **基础设施是否存在**：是（Gradle + GameTest）
- **自动化测试**：是（GameTest）
- **Agent 执行 QA**：是

### Agent 执行的 QA 场景

```
场景：验证粗采样底层替换规则
  工具：Bash（GameTest via Gradle）
  前置条件：GameTest 环境
  步骤：
    1. 在测试世界预置一个含流体（水）和沙砾的 16×16×16 区域
    2. 调用粗采样函数，指定该区域为源，目标为仙窍坐标
    3. 断言：目标区域最底 1 层原流体/沙砾位置现在是石头
    4. 断言：非底层位置的正常方块保持原样
  预期结果：底层替换生效，其他方块不受影响
  证据：GameTest 通过

场景：验证细采样 NBT 保真
  工具：Bash（GameTest via Gradle）
  前置条件：GameTest 环境
  步骤：
    1. 在仙窍源区域放置一个箱子，箱子内含特定物品（钻石 x1）
    2. 调用细采样函数，复制到另一个位置
    3. 断言：目标箱子存在
    4. 断言：目标箱子内含钻石 x1
  预期结果：BlockEntity NBT 完整保留
  证据：GameTest 通过

场景：验证仙窍初始化后四周有地形
  工具：interactive_bash（tmux）
  前置条件：服务器运行中
  步骤：
    1. 执行 /guzhenren enter_aperture
    2. 等待传送完成（5秒）
    3. 检查核心东侧 16 格处：execute if block ~16 ~0 ~0 stone
    4. 检查核心南侧 16 格处：execute if block ~0 ~0 ~16 stone
    5. 检查核心西侧 16 格处：execute if block ~-16 ~0 ~0 stone
    6. 检查核心北侧 16 格处：execute if block ~0 ~0 ~-16 stone
  预期结果：四个方向都存在非空气方块
  证据：终端输出
```

---

## 执行策略

### 并行执行波次

```
Wave 1（立即开始）：
├── 任务 1：OverworldTerrainSampler（粗采样）
└── 任务 2：ApertureRegionCopier（细采样）

Wave 2（Wave 1 完成后）：
├── 任务 3：初始化集成（修改 ApertureCommand）
└── 任务 4：GameTest

Wave 3（Wave 2 完成后）：
└── 任务 5：运行时验证 + checkstyle + build
```

### 依赖矩阵

| 任务 | 依赖 | 阻塞 | 可并行 |
|------|------|------|--------|
| 1 | 无 | 3, 4 | 2 |
| 2 | 无 | 4 | 1 |
| 3 | 1 | 5 | 4 |
| 4 | 1, 2 | 5 | 3 |
| 5 | 3, 4 | 无 | 无（最终步骤） |

---

## 任务列表

- [x] 1. 创建 OverworldTerrainSampler（主世界粗采样）

  **要做什么**：
  - 创建 `OverworldTerrainSampler` 工具类（final class，私有构造器，纯静态方法）
  - **核心方法 `sampleAndPlace`**：
    - 参数：`ServerLevel overworldLevel, ServerLevel apertureLevel, BlockPos targetAnchor, Holder<Biome> targetBiome, @Nullable BlockPos explicitSourceAnchor, RandomSource random`
    - 如果 `explicitSourceAnchor` 为 null，则随机生成源坐标（X/Z 对齐到 16 边界，范围 ±10000）
    - Biome 筛选：检查源坐标中心点 Biome 是否匹配 `targetBiome`，不匹配则重试，最多 20 次
    - 用 `MOTION_BLOCKING_NO_LEAVES` 高度图获取中心点地表高度
    - 采样范围：X/Z = 16×16（对齐到 chunk），Y = 地表下 4 格 ~ 地表上 12 格（clamp 到维度 min/max）
    - 遍历源区域所有 BlockPos，读取 BlockState
    - **底层处理**：采样盒子最底 1 层，如果方块是流体（`FluidState.isEmpty() == false`）或重力方块（`instanceof FallingBlock`），替换为 `Blocks.STONE`
    - 放置到 `apertureLevel` 的 `targetAnchor` 位置，使用 `Block.UPDATE_CLIENTS`（`= 2`，不触发邻居更新，客户端可见）
    - 返回 boolean 表示成功/失败
  - **辅助方法 `findBiomeLocation`**：
    - 随机生成 chunk 对齐坐标
    - 检查中心点 Biome
    - 重试逻辑（最多 20 次）
    - 失败返回 null
  - 注释中文且详细

  **禁止做的事**：
  - 复制 BlockEntity NBT
  - 触发邻居方块更新（`Block.UPDATE_NEIGHBORS`）
  - 引入异步逻辑
  - 在 server 路径引用 client-only 类

  **推荐 Agent 配置**：
  - **Category**：`unspecified-high`
  - **Skills**：无特殊
  
  **并行化**：
  - **可否并行**：是
  - **并行组**：Wave 1（与任务 2 并行）
  - **阻塞**：任务 3, 4
  - **被阻塞**：无

  **参考文件**：
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/FragmentPlacementService.java` — 现有的方块放置模式（`level.setBlock` + 更新标志）
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/data/ApertureWorldData.java` — `APERTURE_SPACING` 常量和 `ApertureInfo` 结构
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/dimension/ApertureVoidChunkGenerator.java` — 维度高度范围（MIN_Y=-64, GEN_DEPTH=384）

  **验收标准**：
  - [ ] `OverworldTerrainSampler.java` 创建在 `service/` 目录下
  - [ ] `sampleAndPlace` 方法签名包含显式源锚点参数
  - [ ] Biome 筛选使用中心点判定
  - [ ] 地表高度使用 `MOTION_BLOCKING_NO_LEAVES`
  - [ ] Y 范围 clamp 到维度边界
  - [ ] 底层流体/重力方块替换为石头
  - [ ] 放置使用 `Block.UPDATE_CLIENTS`（不触发邻居更新）
  - [ ] 最多 20 次重试，失败后返回 false
  - [ ] 注释中文
  - [ ] `./gradlew checkstyleMain` → 通过

  **提交**：是
  - Message: `feat(xianqiao): 添加主世界地形粗采样工具 OverworldTerrainSampler`
  - Files: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/OverworldTerrainSampler.java`

- [x] 2. 创建 ApertureRegionCopier（仙窍细采样）

  **要做什么**：
  - 创建 `ApertureRegionCopier` 工具类（final class，私有构造器，纯静态方法）
  - **核心方法 `copyRegion`**：
    - 参数：`ServerLevel level, BlockPos sourceMin, BlockPos sourceMax, BlockPos targetAnchor`
    - 遍历源区域 `sourceMin` 到 `sourceMax` 的所有 BlockPos
    - 读取 BlockState 并放置到目标位置（相对偏移保持不变）
    - **BlockEntity NBT 复制**：如果源方块有 BlockEntity，用 `blockEntity.saveWithoutMetadata(registries)` 保存 NBT，在目标位置放置方块后用 `targetBlockEntity.loadWithComponents(savedTag, registries)` 恢复
    - 放置使用 `Block.UPDATE_CLIENTS`
    - 不复制实体（Entity）、ScheduledTick、Light
  - **辅助方法 `copyRegionSized`**：
    - 参数：`ServerLevel level, BlockPos sourceCenter, int sizeX, int sizeY, int sizeZ, BlockPos targetCenter`
    - 便利封装，计算 min/max 后调用 `copyRegion`
  - 注释中文且详细

  **禁止做的事**：
  - 复制实体（Entity）
  - 复制 ScheduledTick / Light 数据
  - 跨维度操作（这个函数只在仙窍维度内部使用）
  - 引入异步逻辑

  **推荐 Agent 配置**：
  - **Category**：`unspecified-high`
  - **Skills**：无特殊

  **并行化**：
  - **可否并行**：是
  - **并行组**：Wave 1（与任务 1 并行）
  - **阻塞**：任务 4
  - **被阻塞**：无

  **参考文件**：
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/FragmentPlacementService.java` — 方块放置模式
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/ApertureCoreBlockEntity.java` — BlockEntity 的 `saveAdditional` / `loadAdditional` 模式

  **验收标准**：
  - [ ] `ApertureRegionCopier.java` 创建在 `service/` 目录下
  - [ ] `copyRegion` 正确复制 BlockState
  - [ ] BlockEntity NBT 通过 `saveWithoutMetadata` / `loadWithComponents` 完整传递
  - [ ] 不复制实体 / ScheduledTick / Light
  - [ ] 放置使用 `Block.UPDATE_CLIENTS`
  - [ ] 注释中文
  - [ ] `./gradlew checkstyleMain` → 通过

  **提交**：是
  - Message: `feat(xianqiao): 添加仙窍区域精确复制工具 ApertureRegionCopier`
  - Files: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/ApertureRegionCopier.java`

- [ ] 3. 集成初始化流程（ApertureCommand）

  **要做什么**：
  - 修改 `ApertureCommand.initializeApertureIfNeeded`，在现有平台/核心/箱子之后，调用 `OverworldTerrainSampler.sampleAndPlace` 放置 4 块地形
  - **4 块地形位置**（相对于仙窍中心 `center`）：
    - 东：`center.offset(16, 0, 0)` 为目标锚点
    - 南：`center.offset(0, 0, 16)` 为目标锚点
    - 西：`center.offset(-16, 0, 0)` 为目标锚点（注意锚点是区域西北角还是中心——需与采样函数对齐）
    - 北：`center.offset(0, 0, -16)` 为目标锚点
  - **Biome 选择**（测试阶段）：可以使用 4 种不同 Biome（plains, forest, desert, taiga）或全部 plains，作为常量定义
  - 获取主世界 `ServerLevel`：`player.server.getLevel(Level.OVERWORLD)`
  - 如果粗采样失败（返回 false），打印警告日志但不阻止初始化完成
  - 注释中文且详细

  **禁止做的事**：
  - 改动现有的平台/核心/箱子/地灵生成逻辑
  - 如果采样失败就阻止仙窍初始化

  **推荐 Agent 配置**：
  - **Category**：`quick`
  - **Skills**：无特殊

  **并行化**：
  - **可否并行**：否
  - **并行组**：Wave 2（依赖任务 1）
  - **阻塞**：任务 5
  - **被阻塞**：任务 1

  **参考文件**：
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java:177-194` — `initializeApertureIfNeeded` 方法（在此末尾添加采样调用）
  - `src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java:196-212` — `createInitialPlatform` 方法（理解现有放置模式）
  - 任务 1 交付的 `OverworldTerrainSampler.java` — 调用 `sampleAndPlace`

  **验收标准**：
  - [ ] `initializeApertureIfNeeded` 在现有逻辑之后调用粗采样
  - [ ] 4 个方向各放置 1 块地形（东/南/西/北 偏移 16）
  - [ ] 采样失败时打印 WARN 日志但不中断初始化
  - [ ] 现有平台/核心/箱子/地灵逻辑不受影响
  - [ ] 注释中文
  - [ ] `./gradlew checkstyleMain` → 通过

  **提交**：是（与任务 4 合并提交）
  - Message: `feat(xianqiao): 仙窍初始化时从主世界采样放置四块自然地形`
  - Files: `ApertureCommand.java`

- [ ] 4. 编写 GameTest

  **要做什么**：
  - 创建 GameTest 测试类（位置参考现有 GameTest 文件结构）
  - **测试 1：粗采样底层替换**
    - 在测试结构中预放含水/沙砾的方块区域
    - 调用 `OverworldTerrainSampler.sampleAndPlace`（使用显式源锚点）
    - 断言目标区域最底 1 层的原流体/重力方块位置为石头
    - 断言非底层位置的正常方块保持原样
  - **测试 2：细采样 NBT 保真**
    - 在源区域放置带物品的箱子
    - 调用 `ApertureRegionCopier.copyRegion`
    - 断言目标箱子存在且内含相同物品
  - 参考 `docs/HowToTest.md` 了解 GameTest 编写规范
  - 注释中文

  **禁止做的事**：
  - 依赖随机性（所有测试使用固定坐标和显式参数）
  - 依赖人工操作

  **推荐 Agent 配置**：
  - **Category**：`unspecified-high`
  - **Skills**：无特殊

  **并行化**：
  - **可否并行**：否（严格说可以与任务 3 并行，但建议在 Wave 2 一起做）
  - **并行组**：Wave 2
  - **阻塞**：任务 5
  - **被阻塞**：任务 1, 2

  **参考文件**：
  - `docs/HowToTest.md` — GameTest 编写规范和结构
  - 现有 GameTest 文件（在 `src/main/java/com/Kizunad/` 下搜索 `*_test*` 或 `*Test*`）— 参考命名、注册、结构文件模式
  - 任务 1 交付的 `OverworldTerrainSampler.java`
  - 任务 2 交付的 `ApertureRegionCopier.java`

  **验收标准**：
  - [ ] 至少 2 个 GameTest（底层替换 + NBT 保真）
  - [ ] 使用固定坐标，无随机性
  - [ ] `./gradlew runGameTestServer` → 全部通过
  - [ ] 注释中文
  - [ ] `./gradlew checkstyleMain` → 通过

  **提交**：是（与任务 3 合并提交）
  - Message: `test(xianqiao): 添加地形采样 GameTest（底层替换 + NBT 保真）`
  - Files: 测试类文件 + 测试结构 .nbt（如需要）

- [ ] 5. 运行时验证 + 规范检查

  **要做什么**：
  - 运行 `./gradlew checkstyleMain` → 必须通过
  - 运行 `./gradlew build` → 必须通过
  - 运行 `./gradlew runGameTestServer` → 所有 GameTest 通过
  - 启动服务器，执行 `/guzhenren enter_aperture`
  - 验证仙窍四周有地形（非空气方块存在）
  - 检查服务器日志无异常警告

  **推荐 Agent 配置**：
  - **Category**：`quick`
  - **Skills**：`interactive_bash`

  **并行化**：
  - **可否并行**：否（最终步骤）
  - **并行组**：Wave 3
  - **阻塞**：无
  - **被阻塞**：任务 3, 4

  **验收标准**：
  - [ ] `./gradlew checkstyleMain` → 退出码 0
  - [ ] `./gradlew build` → 退出码 0
  - [ ] `./gradlew runGameTestServer` → 全部通过
  - [ ] 仙窍核心四周存在非空气方块
  - [ ] 服务器日志无 "Cascading worldgen" / "Deadlock" 警告

  **提交**：否（如果需要修复则单独提交）

---

## 提交策略

| 完成任务后 | 提交信息 | 文件 | 验证 |
|-----------|---------|------|------|
| 1 | `feat(xianqiao): 添加主世界地形粗采样工具 OverworldTerrainSampler` | OverworldTerrainSampler.java | checkstyleMain |
| 2 | `feat(xianqiao): 添加仙窍区域精确复制工具 ApertureRegionCopier` | ApertureRegionCopier.java | checkstyleMain |
| 3+4 | `feat(xianqiao): 仙窍初始化时从主世界采样放置四块自然地形` | ApertureCommand.java + 测试文件 | checkstyleMain + runGameTestServer |

---

## 成功标准

### 验证命令
```bash
./gradlew checkstyleMain   # 代码规范
./gradlew build            # 编译
./gradlew runGameTestServer # GameTest
./gradlew runServer        # 运行时验证
```

### 最终检查清单
- [ ] 仙窍初始化后核心四周有自然地形（非虚空）
- [ ] 底层无流体/重力方块（已替换为石头）
- [ ] 细采样 NBT 保真（箱子物品保留）
- [ ] 不改动 ChunkGenerator
- [ ] 代码注释中文
- [ ] 无 `@SuppressWarnings`
