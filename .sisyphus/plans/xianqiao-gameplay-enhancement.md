# 仙窍玩法增强补丁计划 (Refined) — xianqiao-gameplay-enhancement

## TL;DR

> **目标**：针对代码审计发现的“地灵绑定缺失”、“地形生成空白”、“资源玩法浅层”三大缺口，以及“GUI居中”的优化需求，执行深度补丁。
>
> **核心交付**：
> 1. **GUI**: 动态居中适配 (Resize 安全)。
> 2. **Spirit**: 核心<->地灵双向强绑定 (UUID + Pos)。
> 3. **Terrain**: 半径 16 格内的 4 象限微地形生成。
> 4. **Resource**: 2 槽位库存 (催化剂输入) + 垂直底座结构检查。
>
> **验收标准**：`./gradlew build` + `./gradlew checkstyleMain` 通过。

---

## Gap Analysis Summary (现状分析)

| 模块 | 现状 (Current) | 目标 (Target) | 补丁动作 (Action) |
| :--- | :--- | :--- | :--- |
| **GUI** | 代码中已有居中计算，但可能在 Resize/Tab 切换时重置。 | **稳健居中**：确保 `initUI` 始终基于当前屏幕尺寸计算，且 Viewport 正确覆盖。 | 审查并加固 `ApertureHubScreen.initUI`。 |
| **地灵** | 仅存 Owner UUID；生成靠“附近搜索”；无唯一性约束。 | **强绑定**：核心存地灵 UUID，地灵存核心 Pos；生成前双重校验。 | 改造 Entity/BlockEntity NBT；重写 Command 生成逻辑。 |
| **地形** | `buildSurface` 为空，全虚空。 | **分区地表**：半径 16 内按象限生成 Grass/Sand/Snow/Mycelium。 | 实现 `ApertureVoidChunkGenerator.buildSurface`。 |
| **资源** | 1 槽 (仅输出)；结构仅数组件数量。 | **深度玩法**：2 槽 (输入+输出)；检查底座方块；消耗/检查催化剂。 | 改造 Inventory；增加 `checkStructure` 逻辑。 |

---

## Execution Plan

### Phase 1: Core Experience (GUI & Binding)

#### Task 1: Robust GUI Centering
**目标**：确保 Hub 界面在各种分辨率和窗口调整下始终居中。
- **Implementation**:
  - 在 `ApertureHubScreen.initUI` 中：
    - 使用 `(this.width - WINDOW_WIDTH) / 2` 计算 `rootX`。
    - 使用 `(this.height - WINDOW_HEIGHT) / 2` 计算 `rootY`。
    - 关键：`main.setFrame(rootX, rootY, ...)` 必须在 `initUI` 每次调用时执行。
    - 检查 `switchTab` 是否会错误重置位置。

#### Task 2: Spirit Unique Binding System
**目标**：建立“一核心一地灵”的严其对应关系。
- **Implementation**:
  1. **LandSpiritEntity**:
     - 新增字段 `BlockPos boundCorePos`。
     - 修改 `addAdditionalSaveData` / `readAdditionalSaveData` 读写此字段。
  2. **ApertureCoreBlockEntity**:
     - 新增字段 `UUID boundSpiritUUID`。
     - 修改 NBT 读写逻辑。
  3. **ApertureCommand**:
     - 修改 `spawnLandSpirit` 逻辑：
       - `if (coreBE.boundSpiritUUID != null)` -> 检查 `serverLevel.getEntity(uuid)`。
       - 若实体存在 -> 提示“地灵已存在”并跳过。
       - 若实体不存在 -> 生成新实体 -> `entity.setBoundCorePos(center)` -> `coreBE.setBoundSpiritUUID(entity.getUUID())`。

---

### Phase 2: World Generation

#### Task 3: 4-Quadrant Visual Biomes
**目标**：在仙窍中心区域生成视觉上区分的 4 种地表。
- **Implementation**:
  - 修改 `ApertureVoidChunkGenerator.buildSurface`。
  - **逻辑**：
    - 遍历 Chunk 内 `x, z` (0-15)。
    - 转换全局坐标 `worldX, worldZ`。
    - **范围检查**：`if (abs(worldX) <= 16 && abs(worldZ) <= 16)`。
    - **高度固定**：`y = 64`。
    - **方块映射**：
      - `(+x, +z)` -> `Blocks.GRASS_BLOCK`
      - `(-x, +z)` -> `Blocks.SAND`
      - `(-x, -z)` -> `Blocks.SNOW_BLOCK`
      - `(+x, -z)` -> `Blocks.MYCELIUM`
    - 调用 `chunk.setBlockState`。

---

### Phase 3: Gameplay Depth

#### Task 4: Resource Controller Upgrade
**目标**：引入输入槽和结构限制，增加自动化与搭建乐趣。
- **Implementation**:
  1. **Inventory**:
     - 将 `INVENTORY_SIZE` 设为 **2**。
     - 定义 `SLOT_OUTPUT = 0`, `SLOT_INPUT = 1`。
     - `Container` 方法适配：`canPlaceItem` 仅允许 Slot 1 放物品。
  2. **Logic**:
     - 在 `tickServer` 中增加前置检查：
       - `items.get(SLOT_INPUT).isEmpty()` -> 暂停进度 (return)。
       - (可选) 检查特定 Tag (如 `c:catalysts`)。
  3. **Structure**:
     - 实现 `checkStructure()`：检查 `pos.below()` 是否为 `XianqiaoBlocks.RESOURCE_COMPONENT`。
     - 在 `validateStructure` 中调用，若失败则 `isFormed = false`。

---

## TODOs

- [x] Task 1: Verify & Fix GUI Centering
- [x] Task 2: Implement Spirit Binding (Entity/BE/Command)
- [x] Task 3: Implement 4-Quadrant Terrain
- [x] Task 4: Upgrade Resource Controller (Inventory/Structure)
- [ ] Task 5: Final Build Verification

**Dependencies**:
- Task 2 需要修改 3 个文件，需同步进行。
- Task 4 涉及 Inventory 扩容，需注意旧存档兼容性 (Load logic 需处理旧数据)。

**Commit Strategy**:
- `fix(xianqiao): enforce gui centering`
- `feat(xianqiao): implement unique spirit binding`
- `feat(xianqiao): add 4-quadrant terrain gen`
- `feat(xianqiao): upgrade resource controller gameplay`
