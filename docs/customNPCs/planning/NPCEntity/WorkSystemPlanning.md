# NPC 工作与资源管理系统开发计划

## 简介
本计划旨在为 CustomNPCs 模组引入一套完整的 NPC 工作与资源管理系统。该系统将允许玩家通过投入物品积累“材料点数”（Material），并使用这些点数指挥 NPC 制造物品。核心功能将围绕三个主要界面展开：`WorkUI` 作为入口，`ItemToMaterialUI` 处理物品到材料的转化，以及 `WorkCraftUI` 进行物品制造。

## 核心设计理念
*   **数据驱动**: 物品的材料价值将完全由外部 JSON 配置文件驱动，便于配置和扩展。
*   **Owner 资源池**: 玩家（Owner）将拥有与特定 NPC 关联的“材料点数”池。玩家通过投入物品到 `ItemToMaterialUI` 来积累这些点数。
*   **UI 交互**: 系统采用分层级的 UI 设计。`WorkUI` 作为主要入口，提供功能导航；`ItemToMaterialUI` 作为资源补充的入口；`WorkCraftUI` 则作为消耗材料点数以制造物品的出口。

---

## 阶段一：数据层与后端逻辑 (Data & Logic Backend)

### 1. 物品价值数据系统 (Material Values)
*   **目标**: 高效地加载、管理并查询物品的材料价值。
*   **实现细节**:
    *   **类名**: `MaterialValueManager` (或类似名称)。
    *   **功能**:
        *   在模组的初始化事件 (`FMLCommonSetupEvent`) 或数据包重载事件中加载 `@src/main/resources/data/customnpcs/material_values.json`。
        *   构建一个 `Map<Item, Double>` 或类似结构，用于缓存物品及其对应的材料价值。
        *   提供 `double getMaterialValue(ItemStack stack)` 方法，该方法根据 `material_values.json` 中的配置返回物品堆栈的材料价值。
        *   **考虑**: 可选地，实现物品耐久度折损对材料价值的影响，或简单地按固定价值计算。

### 2. NBT 数据存储 (NBT Data Storage)
*   **目标**: 在 NPC 实体中持久化存储玩家的材料点数。
*   **实现细节**:
    *   **存储位置**: 将新的数据字段添加至 `CustomNpcEntity` 的 `AdditionalSaveData` 中，或者作为 `NpcMind` Capability 的一部分。考虑到点数归属于玩家但由 NPC 管理，存储在 `NpcMind` 中并关联 Owner 更合理。
    *   **字段定义**:
        *   `OwnerMaterial` (类型: `Double`): 存储该 NPC 为其当前 Owner 管理的材料点数总额。
        *   `EntityMaterial` (类型: `Double`): （可选/预留）NPC 自身拥有的材料点数，用于未来 NPC 独立工作的扩展。当前阶段主要关注 `OwnerMaterial`。
    *   **持久化**: 确保这些数据字段在 NPC 实体被保存和加载时（通过 `writeAdditional` 和 `readAdditional` 方法）能够正确地写入和读取。
    *   **同步**: 当 `OwnerMaterial` 的值发生变化时，需要通过 NeoForge 的网络事件 (`PacketDistributor`) 将更新后的数据同步给追踪该 NPC 的客户端，以便 UI 能够实时更新。

### 3. 网络通信 (Networking)
*   **目标**: 实现客户端与服务端之间关于材料转化和制造请求、以及材料数据同步的通信。
*   **实现细节**:
    *   **C2S (客户端 -> 服务端) 数据包**:
        *   `RequestMaterialConversionPayload`:
            *   **触发时机**: 玩家点击 `ItemToMaterialUI` 中的“Convert”按钮。
            *   **包含数据**: NPC 实体 ID。
            *   **作用**: 请求服务端处理材料转化逻辑。
        *   `RequestCraftingPayload`:
            *   **触发时机**: 玩家点击 `WorkCraftUI` 中的“Craft!”按钮。
            *   **包含数据**: NPC 实体 ID, 待制造物品的 `ResourceLocation` (Item ID), 制造数量。
            *   **作用**: 请求服务端处理制造逻辑。
    *   **S2C (服务端 -> 客户端) 数据包**:
        *   `SyncMaterialDataPayload`:
            *   **触发时机**: `OwnerMaterial` 余额发生变化时（例如，转化、制造消耗），或客户端打开 Work/Material/Craft UI 时。
            *   **包含数据**: NPC 实体 ID, 当前 `OwnerMaterial` 余额。
            *   **作用**: 更新客户端 UI 上显示的材料点数。

---

## 阶段二：材料转化系统 (ItemToMaterialUI)

### 1. 容器菜单 (Menu) - `NpcMaterialMenu`
*   **目标**: 提供一个可交互的界面，让玩家将物品放入槽位进行转化。
*   **实现细节**:
    *   **继承**: 继承 `AbstractContainerMenu`。
    *   **槽位布局**:
        *   **Input Slots**: 设定一组（例如 3x3 或 1x5）物品槽位，用于玩家放入待转化的物品。
        *   **Player Inventory Slots**: 标准的玩家背包和快捷栏槽位，允许玩家将物品从背包拖入 Input Slots。
    *   **服务端逻辑**: 监听 Input Slots 的变化。当物品放入时，服务器端可实时（或在提交时）验证物品是否在 `material_values.json` 中有定义。

### 2. 界面 (Screen) - `NpcMaterialScreen`
*   **目标**: 可视化地显示材料转化过程和结果。
*   **实现细节**:
    *   **继承**: 继承 `AbstractContainerScreen`，并绑定 `NpcMaterialMenu`。
    *   **UI 元素**:
        *   **标题**: "Input Materials"。
        *   **动态文本显示**:
            *   “当前材料点数（Current Material Amount）”: 显示当前 NPC 处 `OwnerMaterial` 的余额，此数据通过 `SyncMaterialDataPayload` 更新。
            *   “槽位材料预估（Material in slots）”: 实时显示 Input Slots 中所有物品若转化将获得的材料点数总和。
        *   **按钮**: “转化（Convert）”。
    *   **交互逻辑**:
        *   当玩家将物品放入 Input Slots 时，实时更新“槽位材料预估”文本。
        *   点击“转化（Convert）”按钮时，客户端构造并发送 `RequestMaterialConversionPayload` 到服务端。
        *   接收到 `SyncMaterialDataPayload` 时，更新“当前材料点数”显示。

### 3. 转化逻辑 (Conversion Logic)
*   **目标**: 在服务端安全地处理物品到材料点数的转化。
*   **实现细节**:
    *   **服务端 Payload 处理**: 接收 `RequestMaterialConversionPayload`。
    *   **验证与计算**:
        *   获取对应的 NPC 实体。
        *   遍历 `NpcMaterialMenu` 的 Input Slots。
        *   对每个物品堆栈，调用 `MaterialValueManager.getMaterialValue()` 获取其价值。
        *   计算所有物品的总材料价值。
    *   **更新数据**:
        *   清空 Input Slots 中的物品。
        *   将计算出的总价值加到 NPC 的 `OwnerMaterial` 余额上。
    *   **反馈与同步**:
        *   向玩家发送客户端消息（例如，聊天消息）告知转化结果。
        *   发送 `SyncMaterialDataPayload` 给所有追踪该 NPC 的玩家，更新材料点数。

---

## 阶段三：工作与制造系统 (WorkUI & WorkCraftUI)

### 1. 工作主界面 - `NpcWorkScreen`
*   **目标**: 作为 NPC 工作功能的总入口，显示 NPC 当前的工作状态，并提供导航到其他工作相关界面。
*   **实现细节**:
    *   **入口**: NPC 交互菜单中的“Work”选项将打开此界面。
    *   **UI 元素**:
        *   **标题**: "Work Status"。
        *   **状态显示**: 动态文本显示 NPC 当前的工作状态（例如，"Idle", "Crafting: Wooden Sword", "Gathering Wood"）。
        *   **按钮**: “制造（Craft）”: 点击将打开 `NpcCraftScreen`。
        *   **按钮**: “添加材料（Add Material）” / “+”: 点击将打开 `NpcMaterialScreen`。

### 2. 制造界面 - `NpcCraftScreen`
*   **目标**: 提供一个界面，让玩家选择要制造的物品，并提交制造请求。
*   **实现细节**:
    *   **继承**: 继承 `AbstractContainerScreen` (如果需要与背包交互) 或 `Screen`。
    *   **UI 元素**:
        *   **可滚动列表**: “可用物品列表（Available Item List）”。
            *   列表内容从 `material_values.json` 中提取，显示所有可制造物品的图标、名称和所需材料点数。
            *   点击列表项以选择物品。
        *   **选中物品详情**:
            *   显示选中物品的大图标和名称。
            *   **数量输入框/滑块**: “数量（Amount）”，允许玩家指定制造数量。
            *   **文本显示**: “总成本（Total Cost）”，根据选择的物品和数量动态计算所需材料点数。
            *   **文本显示**: “当前材料点数（Current Material Amount）”，显示 `OwnerMaterial` 余额。
            *   **颜色高亮**: 如果 `OwnerMaterial` 不足，将“总成本”文本显示为红色。
        *   **按钮**: “制造！（Craft!）”。
    *   **交互逻辑**:
        *   选择物品或调整数量时，实时更新“总成本”。
        *   点击“制造！（Craft!）”按钮时，客户端验证 `OwnerMaterial` 是否足够，如果足够，则构造并发送 `RequestCraftingPayload` 到服务端。

### 3. 制造执行逻辑 (Crafting Logic)
*   **目标**: 在服务端处理制造请求，扣除材料，并产出物品。
*   **实现细节**:
    *   **服务端 Payload 处理**: 接收 `RequestCraftingPayload`。
    *   **验证**: 验证 NPC 是否存在、请求的物品和数量是否有效，以及 NPC 的 `OwnerMaterial` 是否足以支付总成本。
    *   **扣除材料**: 从 NPC 的 `OwnerMaterial` 余额中扣除相应点数。
    *   **产出物品**:
        *   **即时模式 (初期建议)**: 立刻生成指定数量的物品，并尝试放入 NPC 的内部物品栏。如果 NPC 物品栏已满，则将多余的物品掉落在 NPC 附近。
        *   **延时模式 (进阶)**:
            *   将制造任务添加到 NPC 的一个内部工作队列 (`WorkQueue`) 中。
            *   将 NPC 的工作状态设置为 `WORK_CRAFTING`，并开始一个计时器。
            *   当计时器结束时，生成物品。
            *   NPC 可能会通过邮件系统、或放入一个可交互的容器中，或直接与玩家对话交付物品。
    *   **反馈与同步**:
        *   向玩家发送客户端消息告知制造结果。
        *   发送 `SyncMaterialDataPayload` 更新客户端的材料点数显示。
        *   更新 NPC 的工作状态。

---

## 总结：开发路线图
1.  **Backend & Data**:
    *   实现 `MaterialValueManager` (加载 JSON)。
    *   在 NPC 的 `NpcMind` 或 `CustomNpcEntity` 中添加和持久化 `OwnerMaterial` 数据字段。
    *   定义和注册 C2S 和 S2C 网络 Payload。
2.  **UI - ItemToMaterial**:
    *   实现 `NpcMaterialMenu` (容器逻辑)。
    *   实现 `NpcMaterialScreen` (UI 显示与交互)。
    *   实现服务端的材料转化逻辑。
3.  **UI - WorkCraft**:
    *   实现 `NpcCraftScreen` (UI 显示与交互)。
    *   实现服务端的制造逻辑（初期可采用即时模式）。
4.  **UI - Work**:
    *   实现 `NpcWorkScreen` (主入口和导航)。
5.  **测试**:
    *   对所有新增的数据持久化、网络同步和 UI 交互进行单元测试和集成测试，确保功能正确和数据一致性。
